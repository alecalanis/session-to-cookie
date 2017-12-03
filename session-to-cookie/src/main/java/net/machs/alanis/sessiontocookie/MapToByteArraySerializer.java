/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * simple serializer to serialize a {@link Map<String, Object>} to a byte array and back using {@link ObjectInputStream}s
 * 
 * @author Alejandro Alanis
 */
@Component
public class MapToByteArraySerializer {
	
	/**
	 * Deserializes from a string serialized with
	 * {@link #serializeMapToString(Map)} back to a {@link Map}, using
	 * {@link ObjectInputStream} and {@link ByteArrayInputStream} as the
	 * serializing method does.
	 * 
	 * @param string
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	protected Map<String, Object> deserializeStringToMap(final byte[] source)
			throws IOException, ClassNotFoundException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(source));
		@SuppressWarnings("unchecked")
		final Map<String, Object> map = (Map<String, Object>) objectInputStream.readObject();
		return map;
	}
	
	/**
	 * Serialize the given data using {@link ObjectOutputStream} and
	 * {@link ByteArrayOutputStream}. <b>This requires all content in the data
	 * to be serializable (!)</b>
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	protected byte[] serializeMapToString(final Map<String, Object> data) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream outputStream;
		outputStream = new ObjectOutputStream(byteArrayOutputStream);
		outputStream.writeObject(data);
		outputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

}
