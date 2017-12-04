package net.machs.alanis;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Stopwatch;

/**
 * Controller that simulates stateful responses, holding everything in
 * spring-session
 * 
 * @author Alejandro Alanis
 */
@Controller
public class WebController {

	private static final Logger log = LoggerFactory.getLogger(WebController.class);

	private static final String NAME = "name";

	/**
	 * simply check if there is a session on the server or not for the client
	 * 
	 * @param servletRequest
	 * @return
	 */
	@RequestMapping("/sessioncheck")
	@ResponseBody
	public String handleSessioncheck(HttpServletRequest servletRequest) {
		HttpSession session = servletRequest.getSession(false);
		if (session == null) {
			return "hello, you have no session\n" + getHostInfo();

		} else {
			return "hello, your session is: " + session + ", your name is: " + session.getAttribute(NAME) + "\n" + getHostInfo();
		}
	}

	/**
	 * sets a new name, if no session existed before, otherwise, returns the
	 * name saved in the session.
	 * 
	 * @param servletRequest
	 * @param name
	 * @return
	 */
	@RequestMapping("/createemptysession")
	@ResponseBody
	public String handleCreateEmptySession(HttpServletRequest servletRequest) {
		HttpSession session = servletRequest.getSession(false);
		if (session == null) {
			log.debug("created new empty session");
			session = servletRequest.getSession(true);
		}
		return "hello, your session is: " + session + "\n" + getHostInfo();
	}

	/**
	 * sets a new name, if no session existed before, otherwise, returns the
	 * name saved in the session.
	 * 
	 * @param servletRequest
	 * @param name
	 * @return
	 */
	@RequestMapping("/{name}")
	@ResponseBody
	public String handleIndex(HttpServletRequest servletRequest, @PathVariable("name") String name) {
		HttpSession session = servletRequest.getSession(false);
		if (session == null) {
			log.debug("created new session to save name '" + name + "'");
			// no session was created yet, create a new one and set some session
			// data specific to this user/request
			session = servletRequest.getSession(true);
			session.setAttribute(NAME, name);
		}
		return "hello, your session is: " + session + ", your name is: " + session.getAttribute(NAME) + "\n" + getHostInfo();
	}

	/**
	 * sets a new name, if no session existed before, otherwise, returns the
	 * name saved in the session.
	 * 
	 * @param servletRequest
	 * @param name
	 * @return
	 */
	@RequestMapping("/complex/{name}")
	@ResponseBody
	public String handleComplex(HttpServletRequest servletRequest, @PathVariable("name") String name) {
		HttpSession session = servletRequest.getSession(false);
		if (session == null) {
			log.debug("created new session to save name '" + name + "'");
			// no session was created yet, create a new one and set some session
			// data specific to this user/request
			session = servletRequest.getSession(true);
			session.setAttribute("complex", new SessionObject(name));
		}
		return "hello, your session is: " + session + ", your name is: "
				+ ((SessionObject) session.getAttribute("complex")).getName() + "\n" + getHostInfo();
	}

	@RequestMapping("/slowmethod/{name}")
	@ResponseBody
	public String handleSlowMethod(HttpServletRequest servletRequest, @PathVariable("name") String name) {
		HttpSession session = servletRequest.getSession(false);
		if (session == null) {
			log.debug("created new session to save name '" + name + "'");
			// no session was created yet, create a new one and set some session
			// data specific to this user/request
			session = servletRequest.getSession(true);
			session.setAttribute("complex", new SessionObject(name));
		}
		Stopwatch stopwatch = Stopwatch.createStarted();
		// simulate a slow method by calculating some stuff that should take
		// some hundreds milliseconds. We do NOT use Thread.sleep because we
		// want to measure cpu perfomance differences on heavy load (takes ~100ms on my machine). 
		long count = 0;
		for (long i = 0; i < 250000000l; i++) {
			count++;
		}
		return "hello, your session is: " + session + ", count is: " + count + " elapsed time is: "+stopwatch.elapsed(TimeUnit.MILLISECONDS)+", your name is: "
				+ ((SessionObject) session.getAttribute("complex")).getName() + "\n" + getHostInfo();
	}

	@RequestMapping("/unfilteredslowmethod/{name}")
	@ResponseBody
	public String handleSlowMethodUnfiltered(HttpServletRequest servletRequest, @PathVariable("name") String name) {
		return handleSlowMethod(servletRequest, name);
	}
	
	protected String getHostInfo() {
		String string = "";
		try {
			string += " (host: " + InetAddress.getLocalHost().getHostName() + ", ip: "
					+ InetAddress.getLocalHost().getHostAddress() + ", date: " + new Date().toString() + ")";
		} catch (UnknownHostException e) {
			log.error("", e);
		}
		return string;
	}

}
