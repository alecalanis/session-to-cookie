/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import java.util.Base64;
import java.util.Base64.Encoder;

import org.springframework.stereotype.Component;

/**
 * Simple en-/decoder for cookie values. Uses simple {@link Encoder} from {@link Base64#getEncoder()}
 * 
 * @author Alejandro Alanis
 */
@Component
public class CookieValueEncoder {
	
	/**
	 * 
	 * @param source
	 * @return
	 */
	public byte[] encode(byte[] source) {
		return Base64.getUrlEncoder().withoutPadding().encode(source);
	}
	
	/**
	 * 
	 * @param source
	 * @return
	 */
	public byte[] decode(byte[] source) {
		return Base64.getUrlDecoder().decode(source);
	}

}
