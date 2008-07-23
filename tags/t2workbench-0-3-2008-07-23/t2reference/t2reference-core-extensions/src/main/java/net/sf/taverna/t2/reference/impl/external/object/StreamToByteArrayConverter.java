package net.sf.taverna.t2.reference.impl.external.object;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.t2.reference.StreamToValueConverterSPI;

/**
 * Build a byte[] from an InputStream
 * 
 * @author Tom Oinn
 * 
 */
public class StreamToByteArrayConverter implements
		StreamToValueConverterSPI<byte[]> {

	private static final int CHUNK_SIZE = 4096;

	static byte[] readFile(InputStream reader) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[CHUNK_SIZE];
		int len;
		while ((len = reader.read(buf)) > 0) {
			bos.write(buf, 0, len);
		}
		return bos.toByteArray();
	}

	public Class<byte[]> getPojoClass() {
		return byte[].class;
	}

	public byte[] renderFrom(InputStream stream) {
		try {
			return readFile(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
