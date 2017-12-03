package net.machs.alanis;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.base.Stopwatch;
import com.jayway.restassured.response.Cookies;
import com.jayway.restassured.response.Response;

import net.machs.alanis.sessiontocookie.SessionToCookieFilter;


@RunWith(SpringRunner.class)
@PropertySource("classpath:application.yml")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT, properties={"server.port=9875", "spring.profiles.active=perfomancetest"})
public class SpringSessionCookiePerfomanceTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpringSessionCookiePerfomanceTest.class);
	
	private final String HOST = "http://localhost:9875";
//	private final String HOST = "http://localhost:8080";

	@Test
	public void comparePerfomanceonSlowMethodWithAndWithoutFilter() throws InterruptedException {
		// first requests are always slower, so lets "warm up" first
		stressEndpoint(2, 2, "/unfilteredslowmethod/foo", "your name is: foo", false);
		stressEndpoint(2, 2, "/slowmethod/foo", "your name is: foo", true);
		// cool down a bit ;)
		Thread.sleep(250);
		
		long iterations = 10;
		int threads = 5;
		
		LOG.info("starting test on unfiltered method");
		Duration unfilteredDuration = stressEndpoint(iterations, threads, "/unfilteredslowmethod/foo", "your name is: foo", false);
		LOG.info("starting test on filtered method");
		Duration filteredDuration = stressEndpoint(iterations, threads, "/slowmethod/foo", "your name is: foo", true);
		
		long unfilteredMillis = unfilteredDuration.toMillis();
		long filteredMillis = filteredDuration.toMillis();
		LOG.info("unfiltered slowmethod took: " + unfilteredMillis + "ms for " + iterations + " iterations with "+threads+" threads.");
		LOG.info("slowmethod (filtered) took: " + filteredMillis + "ms for " + iterations + " iterations with "+threads+" threads.");
		
		// make sure the filtered duration is not slower than 25% compared to the unfiltered.
		float factor = (float) unfilteredMillis / filteredMillis;
		LOG.info("filter makes the method slower in a factor of: " + factor);
		assertTrue("filtered method is too slow compared to the unfiltered. did you introduce some costy algorithm somewhere? :p", factor > 0.75f );
	}

	private Duration stressEndpoint(long iterations, int threads, String path, String expectedString, boolean checkForSessionDataCookie) throws InterruptedException {
		final Stopwatch stopWatch = Stopwatch.createStarted();
		final ExecutorService threadPool = Executors.newFixedThreadPool(threads);
		final Runnable command = new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < iterations; i++) {
					final Response response = given().get(HOST + path);
					response.then().assertThat().body(containsString(expectedString));
					final Cookies cookies = response.getDetailedCookies();
					if (checkForSessionDataCookie) {
						assertTrue("no sessiondata cookie exists", cookies.hasCookieWithName(SessionToCookieFilter.SESSIONDATACOOKIENAME));
					}
				}
			}
		};
		for (int i = 0; i < threads; i++) {
			threadPool.execute(command);
		}
		threadPool.shutdown();
		final boolean finished = threadPool.awaitTermination(10, TimeUnit.SECONDS);
		stopWatch.stop();
		if (!finished) {
			LOG.debug("wuaaaah, tasks took longer than 10 seconds. aborted execution since this test should not run that long");
		}
		return stopWatch.elapsed();
	}
}
