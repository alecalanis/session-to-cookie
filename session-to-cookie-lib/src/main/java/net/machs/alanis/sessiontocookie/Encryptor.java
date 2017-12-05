/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import java.io.UnsupportedEncodingException;

import org.jasypt.util.binary.BasicBinaryEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simple helper class for encrypting and decrypting strings using jasypt and its default encryption algorithm.
 * 
 * TODO: find out which algorithm to use for best perfomance and best security
 * 
 * @see https://stackoverflow.com/a/43779197/6184468
 * @author Alejandro Alanis
 */
@Component
public class Encryptor {
	
	private BasicBinaryEncryptor encryptor;
	
	public Encryptor(@Value("${sessiontocookie.sharedsecret}") String password) {
		encryptor = new BasicBinaryEncryptor();
		encryptor.setPassword(password);
	}
	
	/**
	 * 
	 * @param source
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	protected byte[] decrypt(byte[] source) {
		byte[] decrypt = encryptor.decrypt(source);
		return decrypt;
	}

	/**
	 * 
	 * @param source
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	protected byte[] encrypt(byte[] source) {
		byte[] encrypt = encryptor.encrypt(source);
		return encrypt;
	}

}
