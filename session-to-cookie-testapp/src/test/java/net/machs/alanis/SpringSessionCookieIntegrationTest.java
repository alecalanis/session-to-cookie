package net.machs.alanis;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.jayway.restassured.response.Cookies;
import com.jayway.restassured.response.Response;

import net.machs.alanis.sessiontocookie.SessionToCookieFilter;


@RunWith(SpringRunner.class)
@PropertySource("classpath:application.yml")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT, properties={"server.port=9876"})
public class SpringSessionCookieIntegrationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpringSessionCookieIntegrationTest.class);
	
	private final String HOST = "http://localhost:9876"; 

	@Test
	public void cleanRequestOnStatelessEndpointCreatesNoSession() {
		Response response = given().get(HOST + "/sessioncheck");
		response.then().assertThat().body(containsString("you have no session"));
		Cookies cookies = response.getDetailedCookies();
		LOG.debug("cookies are: " + cookies);
		assertFalse(cookies.exist());
	}
	
	@Test
	public void cleanRequestCreatingEmptySessionReturnsNoSessionDataCookie() {
		Response response = given().get(HOST + "/createemptysession");
		response.then().assertThat().body(containsString("hello, your session is"));
		Cookies cookies = response.getDetailedCookies();
		LOG.debug("cookies are: " + cookies);
		if (cookies != null) {
			assertFalse(cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));
		}
	}
	
	@Test
	public void cleanRequestOnStatefulEndpointCreatesSessionWithStateCookie() {
		Response response = given().get(HOST + "/foo");
		response.then().assertThat().body(containsString("your name is: foo"));
		Cookies cookies = response.getDetailedCookies();
		assertNotNull(cookies);
		LOG.debug("cookies are: " + cookies);
		assertTrue(cookies.exist());
		assertTrue(cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));
	}
	
	@Test
	public void secondRequestOnStatefulEndpointWithCookiesGivesSessionResult() {
		// first request should give us the proper name and a cookie
		Response response = given().get(HOST + "/foo");
		response.then().assertThat().body(containsString("your name is: foo"));
		Cookies cookies = response.getDetailedCookies();
		assertNotNull(cookies);
		LOG.debug("cookies are: " + cookies);
		assertTrue(cookies.exist());
		assertTrue(cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));
		
		// second request with given cookie and ANOTHER name should still give us "foo" instead of "bar" since this info is stored in the session data cookie
		// we also expect the same cookie as before 
		response = given().with().cookies(cookies).get(HOST + "/bar");
		response.then().assertThat().body(containsString("your name is: foo"));
		cookies = response.getDetailedCookies();
		LOG.debug("cookies are: " + cookies);
		assertTrue(cookies.exist());
		assertTrue(cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));		
	}
	
	@Test
	public void secondRequestOnStatefulComplexEndpointWithCookiesGivesSessionResult() {
		// first request should give us the proper name and a cookie
		Response response = given().get(HOST + "/complex/foo");
		response.then().assertThat().body(containsString("your name is: foo"));
		Cookies cookies = response.getDetailedCookies();
		assertNotNull(cookies);
		LOG.debug("cookies are: " + cookies);
		assertTrue(cookies.exist());
		assertTrue(cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));
		
		// second request with given cookie and ANOTHER name should still give us "foo" instead of "bar" since this info is stored in the session data cookie
		// we also expect the same cookie as before 
		response = given().with().cookies(cookies).get(HOST + "/complex/bar");
		response.then().assertThat().body(containsString("your name is: foo"));
		cookies = response.getDetailedCookies();
		LOG.debug("cookies are: " + cookies);
		assertTrue(cookies.exist());
		assertTrue(cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));		
	}	
	
	
}
