/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.machs.alanis.sessiontocookie.ChecksumHelper;

/**
 * @author Alejandro Alanis
 */
public class ChecksumHelperTest {
	
	private static final Logger log = LoggerFactory.getLogger(ChecksumHelperTest.class);

	@Test
	public void testAddChecksumAndValidateChecksum() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] source = "foobarbaz".getBytes();
		log.debug("source      : " + new String(source));
		byte[] withChecksum = checksumHelper.addChecksum(source);
		log.debug("withChecksum: " + new String(withChecksum));
		assertNotNull(withChecksum);
		byte[] stripped = checksumHelper.validateAndStripChecksum(withChecksum);
		log.debug("stripped    : " + new String(stripped));
		assertEquals(new String(source), new String(stripped));
	}
	
	@Test
	public void testAddChecksumAndValidateChecksumWithLongString() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] source = "foobarbaz14351985alkjgfölkj12435öljgkjöl35j13489ufdgsg##1345gq3423#2345143tasgafg".getBytes();
		log.debug("source      : " + new String(source));
		byte[] withChecksum = checksumHelper.addChecksum(source);
		log.debug("withChecksum: " + new String(withChecksum));
		assertNotNull(withChecksum);
		byte[] stripped = checksumHelper.validateAndStripChecksum(withChecksum);
		log.debug("stripped    : " + new String(stripped));
		assertEquals(new String(source), new String(stripped));
	}
	
	
	@Test
	public void testAddChecksum() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] source = "foobarbaz".getBytes();
		log.debug("source      : " + new String(source));
		byte[] withChecksum = checksumHelper.addChecksum(source);
		log.debug("withChecksum: " + new String(withChecksum));
		assertEquals("foobarbaz#444082090", new String(withChecksum));
	}
	
	@Test
	public void testValidateChecksum() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] withChecksum = "foobarbaz#444082090".getBytes(); 
		log.debug("withChecksum: " + new String(withChecksum));
		assertNotNull(withChecksum);
		byte[] stripped = checksumHelper.validateAndStripChecksum(withChecksum);
		log.debug("stripped    : " + new String(stripped));
		assertEquals("foobarbaz", new String(stripped));
	}
	
	@Test(expected = RuntimeException.class)
	public void testValidateChecksumFailsOnMissingChecksum() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] withChecksum = "foobarbaz".getBytes(); 
		log.debug("withChecksum: " + new String(withChecksum));
		assertNotNull(withChecksum);
		checksumHelper.validateAndStripChecksum(withChecksum);
		fail("should have thrown an exception by now");
	}
	
	@Test(expected = RuntimeException.class)
	public void testValidateChecksumFailsOnFalseChecksum() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] withChecksum = "foobarbaz#444082092".getBytes(); 
		log.debug("withChecksum: " + new String(withChecksum));
		assertNotNull(withChecksum);
		checksumHelper.validateAndStripChecksum(withChecksum);
		fail("should have thrown an exception by now");
	}
	
	@Test(expected = RuntimeException.class)
	public void testValidateChecksumFailsOnFalseChecksum2() {
		ChecksumHelper checksumHelper = new ChecksumHelper();
		byte[] withChecksum = "foobarbal#444082090".getBytes(); 
		log.debug("withChecksum: " + new String(withChecksum));
		assertNotNull(withChecksum);
		checksumHelper.validateAndStripChecksum(withChecksum);
		fail("should have thrown an exception by now");
	}
	
}
