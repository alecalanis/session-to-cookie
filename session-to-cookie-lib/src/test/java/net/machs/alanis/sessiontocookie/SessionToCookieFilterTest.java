/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import net.machs.alanis.sessiontocookie.SessionToCookieFilter;

/**
 * @author Alejandro Alanis
 */
@RunWith(SpringRunner.class)
@PropertySource("classpath:application.properties")
@SpringBootTest
public class SessionToCookieFilterTest {
	
	private static final Logger log = LoggerFactory.getLogger(SessionToCookieFilterTest.class);
	
	@Autowired 
	private SessionToCookieFilter sessionToCookieFilter; 

	@Test
	public void testComposeDecompose() throws IOException {
		String original = "foobar";
		log.debug("original: " + original);
		byte[] composed = sessionToCookieFilter.compose(original.getBytes());
		log.debug("composed: " + composed);
		byte[] decomposed = sessionToCookieFilter.decompose(composed);
		log.debug("decomposed: " + decomposed);
		assertEquals(original, new String(decomposed));
	}

}
