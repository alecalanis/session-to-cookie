/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.CookieGenerator;

import com.google.common.collect.Lists;

/**
 * Main servlet filter that puts information from an existent session into a
 * session data cookie and vice versa. Put this filter in front of your
 * filterchain to make your application stateless (if you have a stateful
 * application using sessions)
 * 
 * <b>Be aware that to be able to do this, the response is wrapped into a
 * {@link ContentCachingResponseWrapper} which will buffer the output inmemory.
 * This may eat up a lot of memory, depending on what your application does!</b>
 * 
 * Details: 
 * - on incoming requests, if a session data cookie is present, the
 *   information is put into a new or existent session 
 * - on outgoing requests, if
 *   a session exists and it has data, the information is put into a session data
 *   cookie (encrypted, compressed and encoded)
 * 
 * @see ContentCachingResponseWrapper
 * @author Alejandro Alanis
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SessionToCookieFilter extends OncePerRequestFilter {

	public static final String SESSIONDATACOOKIENAME = "SESSIONDATA";

	private static final Logger LOG = LoggerFactory.getLogger(SessionToCookieFilter.class);

	@Autowired
	private Compressor compressor;

	@Autowired
	private Encryptor encrypter;

	@Autowired
	private ChecksumHelper checksumHelper;

	@Autowired
	private CookieValueEncoder cookieValueEncoder;
	
	@Autowired
	private MapToByteArraySerializer mapToByteArraySerializer;
	
	private CookieGenerator cookieGenerator;
	
	@Value("#{'${sessiontocookie.excludeurlpatterns:}'.split(',')}")
	private List<String> excludeUrlPatterns = Lists.newArrayList();

	private AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			addSessionCookieDataToSessionIfCookieExists(httpServletRequest);
		} catch (Exception e) {
			LOG.warn("could not retrieve or decode cookie into session, will not restore session data from session data cookie", e);
		}
		
		// once content is written to the response stream, it is too late to add
		// cookies, so we need to use a buffered response, using springs
		// ContentCachingResponseWrapper in our case, since this already
		// implements that kind of wrapper.
		ContentCachingResponseWrapper responseWrapper = wrapResponseIfNeeded(httpServletResponse);
		
		try {
			// continue filter chain
			filterChain.doFilter(httpServletRequest, responseWrapper);
		} finally {
			try {
				addCookieToResponseIfSessionDataExists(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				LOG.warn("could not add cookie to response, session data may get lost!", e);
			}
			// make sure the wrapper now commits the response to the underlying response.
			responseWrapper.copyBodyToResponse();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.filter.OncePerRequestFilter#shouldNotFilter(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String servletPath = request.getServletPath();
		for (String pattern : excludeUrlPatterns) {
			if (antPathMatcher.match(pattern, servletPath)) {
				return true;
			}
		}
		return super.shouldNotFilter(request);
	}

	/**
	 * wrap the {@link HttpServletResponse} in a {@link ContentCachingResponseWrapper} if it is not already one.
	 * 
	 * @param httpServletResponse
	 * @return
	 */
	protected ContentCachingResponseWrapper wrapResponseIfNeeded(HttpServletResponse httpServletResponse) {
		if (!(httpServletResponse instanceof ContentCachingResponseWrapper)) {
			return new ContentCachingResponseWrapper(httpServletResponse);
		} else {
			return (ContentCachingResponseWrapper) httpServletResponse;
		}
	}

	/**
	 * retrieve session data cookie, and if it exists, decompose and put data into session
	 * 
	 * @param httpServletRequest
	 * @throws IOException
	 */
	protected void addSessionCookieDataToSessionIfCookieExists(HttpServletRequest httpServletRequest) throws IOException {
		LOG.debug("IN : SessionToCookieFilter was called. request: '" + httpServletRequest.getRequestURL()
		+ "' cookies given: " + httpServletRequest.getCookies());
		Cookie cookie = retrieveSessionDataCookie(httpServletRequest);
		if (cookie != null) {
			LOG.debug("IN : cookie '" + getSessionDataCookieName() + "' exists, will put content into session.");
			putCookieContentIntoSession(httpServletRequest, cookie);
		}
	}

	/**
	 * checks if a session with data exists and puts all its data into a session data cookie which is added to the response.  
	 * 
	 * @param httpServletRequest
	 * @param httpServletResponse
	 * @throws IOException
	 */
	protected void addCookieToResponseIfSessionDataExists(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException {
		if (sessionExists(httpServletRequest)) {
			LOG.debug("OUT: session exists.");
			Map<String, Object> sessionData = retrieveSessionDataFromSession(httpServletRequest.getSession(false));
			if (!sessionData.isEmpty()) {
				LOG.debug("OUT: sessiondata exists: " + sessionData);
				byte[] cookiePayload = getCookiePayload(sessionData);
				CookieGenerator cookieGenerator = getSessionDataCookieGenerator();
				LOG.debug("OUT: will add session data cookie to response. value: " + new String(cookiePayload));
				cookieGenerator.addCookie(httpServletResponse, new String(cookiePayload));
			}
		}
	}

	/**
	 * puts the cookie data into a session. If no session exists, one will be
	 * created, since we got the cookie and thus, have session data
	 * 
	 * @param httpServletRequest
	 * @param cookie
	 * @throws IOException
	 */
	protected void putCookieContentIntoSession(HttpServletRequest httpServletRequest, Cookie cookie)
			throws IOException {
		byte[] payload = cookie.getValue().getBytes();
		byte[] validated = decompose(payload);
		Map<String, Object> sessionData;
		try {
			sessionData = mapToByteArraySerializer.deserializeStringToMap(validated);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(
					"Could not deserialize string back to map. This may be caused if the version of the software serializing and deserializing is different!",
					e);
		} catch (IOException e) {
			throw new RuntimeException("Could not deserialize string back to map.", e);
		}
		putDataIntoSession(httpServletRequest, sessionData);
	}
	
	/**
	 * add checksum, compress, encrypt and encode given payload
	 * 
	 * @param sessionData
	 * @return
	 * @throws IOException
	 */
	protected byte[] compose(byte[] sessionData) throws IOException {
		byte[] withChecksum = checksumHelper.addChecksum(sessionData);
		byte[] compressed = compressor.compress(withChecksum);
		byte[] encrypted = encrypter.encrypt(compressed);
		byte[] base64encoded = cookieValueEncoder.encode(encrypted);
		return base64encoded;
	}

	/**
	 * decode, decrypt, decompress and validate checksum on payload. 
	 * 
	 * @param payload
	 * @return
	 * @throws IOException
	 */
	protected byte[] decompose(byte[] payload) throws IOException {
		byte[] decoded = cookieValueEncoder.decode(payload);
		byte[] decrypted = encrypter.decrypt(decoded);
		byte[] decompressed = compressor.decompress(decrypted);
		byte[] validated = checksumHelper.validateAndStripChecksum(decompressed);
		return validated;
	}

	/**
	 * retrieves the session data as map. May be an empty map if the
	 * session contains no data.
	 * 
	 * @param session
	 * @return
	 */
	protected Map<String, Object> retrieveSessionDataFromSession(HttpSession session) {
		Enumeration<String> attributeNames = session.getAttributeNames();
		Map<String, Object> data = new HashMap<>();
		while (attributeNames.hasMoreElements()) {
			String attributeName = attributeNames.nextElement();
			Object value = session.getAttribute(attributeName);
			data.put(attributeName, value);
		}
		return data;
	}

	/**
	 * puts the given session data into the existing or a newly created session.
	 * 
	 * @param httpServletRequest
	 * @param sessionData
	 */
	protected void putDataIntoSession(HttpServletRequest httpServletRequest, Map<String, Object> sessionData) {
		HttpSession session;
		if (sessionExists(httpServletRequest)) {
			session = httpServletRequest.getSession(false);
		} else {
			LOG.debug("creating session, since it didnt exist and a sessiondata cookie was set.");
			session = httpServletRequest.getSession(true);
		}
		for (String key : sessionData.keySet()) {
			session.setAttribute(key, sessionData.get(key));
		}
	}

	/**
	 * returns the cookie corresponding to {@link #getSessionDataCookieName()},
	 * if it exists. Null otherwise.
	 * 
	 * @param httpServletRequest
	 * @return
	 */
	protected Cookie retrieveSessionDataCookie(HttpServletRequest httpServletRequest) {
		Cookie[] cookies = httpServletRequest.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (getSessionDataCookieName().equals(cookie.getName())) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	protected CookieGenerator getSessionDataCookieGenerator() {
		if (cookieGenerator == null) {
			cookieGenerator = new CookieGenerator();
			customizeSessionDataCookie(cookieGenerator);
		}
		return cookieGenerator;
	}

	/**
	 * if you need to customize the cookie (timeout, http-only and so on), you
	 * may overwrite this. It makes sense to make this cookie a session cookie
	 * btw, since it contains session information :)
	 * 
	 * @param cookieGenerator
	 */
	protected void customizeSessionDataCookie(CookieGenerator cookieGenerator) {
		cookieGenerator.setCookieName(SESSIONDATACOOKIENAME);
		// since the cookie contains encrypted data, it is not supposed to be
		// read by scripts.
		cookieGenerator.setCookieHttpOnly(true);
		// set this cookie to be a session cookie. If you think you need more
		// here, rather use proper cookies for persisting things to the client.
		cookieGenerator.setCookieMaxAge(-1);
		// this kind of cookies should be on a https endpoint...
		// TODO: think on how we could make sure this is enabled for production, maybe use profiles?
		// cookieGenerator.setCookieSecure(true);
	}

	/**
	 * create the cookie payload from given data. The data will be serialized with
	 * {@link #serializeMapToString(Map)}, then composed calling {@link #compose(byte[])}
	 * 
	 * @param map
	 * @return
	 * @throws IOException
	 */
	protected byte[] getCookiePayload(Map<String, Object> map) throws IOException {
		byte[] sessionData;
		try {
			sessionData = mapToByteArraySerializer.serializeMapToString(map);
		} catch (IOException e) {
			throw new RuntimeException("could not serialize given data into a map :(", e);
		}
		byte[] base64encoded = compose(sessionData);
		return base64encoded;
	}

	/**
	 * Returns the name to use for the cookie. The name should be a constant.
	 * 
	 * @return
	 */
	protected String getSessionDataCookieName() {
		return SESSIONDATACOOKIENAME;
	}

	/**
	 * returns true if a session exists for the given {@link HttpServletRequest}
	 * 
	 * @param httpServletRequest
	 * @return
	 */
	protected boolean sessionExists(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		return null != session;
	}

}
