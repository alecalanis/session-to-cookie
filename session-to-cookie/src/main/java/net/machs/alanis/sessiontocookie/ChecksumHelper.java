/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import java.util.Arrays;
import java.util.zip.CRC32;

import org.springframework.stereotype.Component;

import com.google.common.primitives.Bytes;

/**
 * simple helper class that adds and validates checksums on given byte arrays, using {@link CRC32}.
 * The checksum itself is appended at the end of the given byte array, using a '#' as separator, and cut off when validating.
 * 
 * @author Alejandro Alanis
 */
@Component
public class ChecksumHelper {
	
	private static final char SEPARATOR = '#';

	/**
	 * extracts the checksum of the source, calculates a new one on the cut off source and compares them.
	 * returns the cut off data only if the checksums matched
	 * 
	 * @param source
	 * @return
	 */
	protected byte[] validateAndStripChecksum(byte[] source) {
		// retrieve checksum
		int lastIndexOf = Bytes.lastIndexOf(source, (byte) SEPARATOR);
		if (lastIndexOf < 0) {
			throw new RuntimeException("no checksum was found on source: " + new String(source));
		}
		long checkSum = Long.parseLong(new String(Arrays.copyOfRange(source, lastIndexOf + 1, source.length)));
		byte[] data = Arrays.copyOfRange(source, 0, lastIndexOf);
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		long calculatedCheckSum = crc32.getValue();
		// validate checksum
		if (Long.compare(checkSum, calculatedCheckSum) != 0) {
			throw new RuntimeException("checksums do not match! calculated: " + calculatedCheckSum + ", provided: " + checkSum);
		}
		return data;
	}
	
	/**
	 * calculates the checksum of the source and returns a byte array with appended checksum.
	 * 
	 * @param string
	 * @return
	 */
	protected byte[] addChecksum(byte[] source) {
		CRC32 crc32 = new CRC32();
		crc32.update(source);
		long checkSum = crc32.getValue();
		byte[] checkSumBytes = String.valueOf(checkSum).getBytes();
		byte[] concat = Bytes.concat(source, (SEPARATOR+"").getBytes(), checkSumBytes);
		return concat;
	}

}
