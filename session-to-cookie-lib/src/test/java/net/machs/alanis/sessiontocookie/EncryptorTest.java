/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.machs.alanis.sessiontocookie.Encryptor;

/**
 * @author Alejandro Alanis
 */
public class EncryptorTest {
	
	private static final Logger log = LoggerFactory.getLogger(EncryptorTest.class);

	@Test
	public void testRoundtrip() {
		Encryptor encrypter = new Encryptor("somepassword");
		String original = new String("foobar");
		log.debug("original : " + original);
		byte[] source = original.getBytes();
		byte[] encrypted = encrypter.encrypt(source);
		log.debug("encrypted: " + new String(encrypted));
		assertNotEquals(source, encrypted);
		byte[] decrypted = encrypter.decrypt(encrypted);
		log.debug("decrypted: " + new String(decrypted));
		assertEquals(new String(source), new String(decrypted));
	}
	
	@Test
	public void testRoundtripWithBinaryData() {
		Encryptor encrypter = new Encryptor("somepassword");
		byte[] original = "foobar\t\n".getBytes();
		log.debug("original : " + new String(original));
		byte[] source = original;
		byte[] encrypted = encrypter.encrypt(source);
		log.debug("encrypted: " + new String(encrypted));
		assertNotEquals(source, encrypted);
		byte[] decrypted = encrypter.decrypt(encrypted);
		log.debug("decrypted: " + new String(decrypted));
		assertEquals(new String(source), new String(decrypted));
	}
	

}
