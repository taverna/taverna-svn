package net.sourceforge.taverna.scuflworkers.ncbi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import net.sourceforge.taverna.io.StreamProcessor;

/**
 * This class takes an input stream and writes the contents to a file.
 * 
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class FileStreamProcessor implements StreamProcessor {

	protected int bufferSize = 2000;

	/**
	 * Constructor
	 * 
	 * @param filename
	 */
	public FileStreamProcessor(String filename) {
		this.file = new File(filename);
	}

	/**
	 * Constructor
	 * 
	 * @param file
	 *            The output file
	 */
	public FileStreamProcessor(File file) {
		this.file = file;
	}

	/**
	 * Constructor
	 * 
	 * @param file
	 *            The output file.
	 * @param bufferSize
	 *            The buffer size used when reading/writing files.
	 */
	public FileStreamProcessor(File file, int bufferSize) {
		this.file = file;
		this.bufferSize = bufferSize;
	}

	/**
	 * This method processes an input stream and outputs the result to a file.
	 * Note that no Map is actually returned.
	 */
	public Map processStream(InputStream stream) throws IOException {

		BufferedReader in = new BufferedReader(new InputStreamReader(stream), this.bufferSize);
		String str;
		String lineEnding = System.getProperty("line.separator");
		BufferedWriter out = new BufferedWriter(new FileWriter(this.file), this.bufferSize);
		while ((str = in.readLine()) != null) {
			out.write(str);
			out.write(lineEnding);
		}
		out.close();

		return null;
	}

	protected File file;
}
