/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.machs.alanis.SessionObject;
import net.machs.alanis.sessiontocookie.MapToByteArraySerializer;

/**
 * @author Alejandro Alanis
 */
public class MapToByteArraySerializerTest {
	
	private static final Logger log = LoggerFactory.getLogger(MapToByteArraySerializerTest.class);

	/**
	 * Test method for {@link net.machs.alanis.sessiontocookie.MapToByteArraySerializer#deserializeStringToMap(java.lang.String)}.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testSerializationDeserialization() throws IOException, ClassNotFoundException {
		Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		log.debug("map  : " + map);
		MapToByteArraySerializer serializer = new MapToByteArraySerializer();
		byte[] bytes = serializer.serializeMapToString(map);
		log.debug("bytes: " + new String(bytes));
		Map<String, Object> deserializedMap = serializer.deserializeStringToMap(bytes);
		log.debug("map  : " + deserializedMap);
		assertEquals(map, deserializedMap);
	}
	
	/**
	 * Test method for {@link net.machs.alanis.sessiontocookie.MapToByteArraySerializer#deserializeStringToMap(java.lang.String)}.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testSerializationDeserializationWithComplexType() throws IOException, ClassNotFoundException {
		Map<String, Object> map = new HashMap<>();
		map.put("foo", "bar");
		map.put("gna", new SessionObject("moep"));
		log.debug("map  : " + map);
		MapToByteArraySerializer serializer = new MapToByteArraySerializer();
		byte[] bytes = serializer.serializeMapToString(map);
		log.debug("bytes: " + new String(bytes));
		Map<String, Object> deserializedMap = serializer.deserializeStringToMap(bytes);
		log.debug("map  : " + deserializedMap);
		assertEquals(map, deserializedMap);
	}
	

}
