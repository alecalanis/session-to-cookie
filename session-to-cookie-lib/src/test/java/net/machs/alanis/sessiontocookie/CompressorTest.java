/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.machs.alanis.sessiontocookie.Compressor;

/**
 * @author Alejandro Alanis
 */
public class CompressorTest {
	
	private static final Logger log = LoggerFactory.getLogger(CompressorTest.class);

	/**
	 * Test method for {@link net.machs.alanis.sessiontocookie.Compressor#compress(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testCompressionDoesCompress() throws IOException {
		Compressor compressor = new Compressor();
		String uncompressed = "foobarbazamassivelylongstringofsomethingthatshouldcompressprettyfineeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
		log.info("uncompressed: " + uncompressed);
		byte[] compressed = compressor.compress(uncompressed.getBytes());
		log.info("compressed: " + new String(compressed));
		assertTrue(compressed.length <= uncompressed.length());
	}

	@Test
	public void testCompressionAndDecompression() throws IOException {
		Compressor compressor = new Compressor();
		String uncompressed = "foobarbazamassivelylongstringofsomethingthatshouldcompressprettyfineeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee";
		log.info("uncompressed: " + uncompressed);
		byte[] compressed = compressor.compress(uncompressed.getBytes());
		log.info("compressed: " + new String(compressed));
		String decompressed = new String(compressor.decompress(compressed));
		log.info("decompressed: " + decompressed);
		assertNotNull(compressed);
		assertTrue(compressed.length <= uncompressed.length());
		assertEquals(uncompressed, decompressed);
	}
	
}
