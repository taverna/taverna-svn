package net.sf.taverna.t2.reference.impl.external.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sf.taverna.t2.reference.StreamToValueConverterSPI;

/**
 * Build a String from an InputStream
 * 
 * @author Tom Oinn
 * 
 */
public class StreamToStringConverter implements
		StreamToValueConverterSPI<String> {

	private static final int END_OF_FILE = -1;
	private static final int CHUNK_SIZE = 4096;

	/***************************************************************************
	 * = readFile(): reads a text file and returns a string *
	 **************************************************************************/
	static String readFile(Reader reader) throws IOException {
		StringBuffer buffer = new StringBuffer();
		char[] chunk = new char[CHUNK_SIZE];
		int character;
		while ((character = reader.read(chunk)) != END_OF_FILE) {
			buffer.append(chunk, 0, character);
		}
		return buffer.toString();
	}

	public Class<String> getPojoClass() {
		return String.class;
	}

	public String renderFrom(InputStream stream) {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		try {
			return readFile(in);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
