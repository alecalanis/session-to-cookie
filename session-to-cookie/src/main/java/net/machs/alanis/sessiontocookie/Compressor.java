/**
 * 
 */
package net.machs.alanis.sessiontocookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;

/**
 * Simple helper class for compression of strings using the algorithms provided by java.util.zip.*
 * Supports deflate and gzip compression using streams.
 * 
 * @author Alejandro Alanis
 */
@Component
public class Compressor {
	
	private static final CompressionType DEFAULTCOMPRESSIONTYPE = CompressionType.DEFLATE;

	/**
	 * different types of compression algorithms supported by this class
	 * 
	 * @author Alejandro Alanis
	 */
	protected enum CompressionType {
		NONE,
		DEFLATE, 
		GZIP;
	}
	
	/**
	 * compress the source using the default {@link CompressionType} defined in {@link #DEFAULTCOMPRESSIONTYPE}
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public byte[] compress(byte[] source) throws IOException {
		return compress(DEFAULTCOMPRESSIONTYPE, source);
	}
	
	/**
	 * compress the source using given {@link CompressionType}. This uses ByteStreams and stuff of java.util.zip* 
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public byte[] compress(CompressionType type, byte[] source) throws IOException {
		if (CompressionType.NONE.equals(type)) {
			return source;
		} else {
			ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length / 2);
			OutputStream compressor = null;
			try {
				compressor = getCompressingOutputStream(type, outputStream);
				ByteStreams.copy(sourceStream, compressor);
			} finally {
				compressor.flush();
				compressor.close();
			}
			return outputStream.toByteArray();
		}
	}
	
	/**
	 * decompress source using the default {@link CompressionType} defined in {@link #DEFAULTCOMPRESSIONTYPE}
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public byte[] decompress(byte[] source) throws IOException {
		return decompress(DEFAULTCOMPRESSIONTYPE, source);
	}
	
	/**
	 * decompress the source using the given {@link CompressionType}. This uses ByteStreams and stuff of java.util.zip*
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public byte[] decompress(CompressionType type, byte[] source) throws IOException {
		ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length * 2);
		InputStream decompressor = getDecompressingInputStream(type, sourceStream);
		try {
			ByteStreams.copy(decompressor, outputStream);
		} finally {
			decompressor.close();
		}
		return outputStream.toByteArray();
	}
	
	/**
	 * returns the {@link OutputStream} corresponding to given {@link CompressionType}
	 * 
	 * @param type 
	 * @param outputStream
	 * @return
	 * @throws IOException
	 */
	protected OutputStream getCompressingOutputStream(CompressionType type, ByteArrayOutputStream outputStream) throws IOException {
		if (CompressionType.DEFLATE.equals(type)) {
			return new DeflaterOutputStream(outputStream);
		} else if (CompressionType.GZIP.equals(type)) {
			return new GZIPOutputStream(outputStream);
		} else {
			throw new UnsupportedOperationException("unsupported compressiontype: " + type);
		}
	}
	
	/**
	 * returns the {@link InputStream} corresponding to given {@link CompressionType}
	 * 
	 * @param type 
	 * @param sourceStream
	 * @return
	 * @throws IOException 
	 */
	protected InputStream getDecompressingInputStream(CompressionType type, ByteArrayInputStream inputStream) throws IOException {
		if (CompressionType.DEFLATE.equals(type)) {
			return new InflaterInputStream(inputStream);
		} else if (CompressionType.GZIP.equals(type)) {
			return new GZIPInputStream(inputStream);
		} else {
			throw new UnsupportedOperationException("unsupported compressiontype: " + type);
		}
	}
	
}
