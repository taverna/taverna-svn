package net.sourceforge.taverna.scuflworkers.ncbi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.io.StreamProcessor;

/**
 * This class replaces text in the stream
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class ReplacementStreamProcessor implements StreamProcessor {

	public ReplacementStreamProcessor() {

	}

	/**
	 * Constructor
	 * 
	 * @param filename
	 * @param _oldText
	 * @param _replacementText
	 */
	public ReplacementStreamProcessor(String _oldText, String _replacementText) {

		this.oldText = new String[] { _oldText };
		this.replacementText = new String[] { _replacementText };
	}

	/**
	 * Constructor
	 * 
	 * @param filename
	 * @param oldText
	 * @param replacementText
	 */
	public ReplacementStreamProcessor(String[] oldText, String[] replacementText) {

		this.oldText = oldText;
		this.replacementText = replacementText;
		if (oldText.length != replacementText.length) {
			throw new IllegalArgumentException(
					"The length of the oldText array must be equal to the length of the replacmentText array.");
		}
	}

	/**
	 * This method iterates through a series of oldText and replacementText
	 * pairs, replacing all occurrences of the oldText with the replacementText.
	 * The results are then written out to the file supplied in the constructor.
	 * 
	 * @param stream
	 */
	public Map processStream(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream), this.bufferSize);
		String str;
		String lineEnding = System.getProperty("line.ending");
		StringWriter sw = new StringWriter();
		BufferedWriter out = new BufferedWriter(sw, this.bufferSize);
		Map outputMap = new HashMap();
		while ((str = in.readLine()) != null) {
			for (int i = 0; i < oldText.length; i++) {

				str = str.replaceAll(oldText[i], replacementText[i]);
			}
			out.write(str);
			out.write(lineEnding);
		}
		out.close();
		outputMap.put("resultXml", sw.toString());
		return outputMap;
	}

	/**
	 * @return Returns the oldText.
	 */
	public String[] getOldText() {
		return oldText;
	}

	/**
	 * @param oldText
	 *            The oldText to set.
	 */
	public void setOldText(String[] oldText) {
		this.oldText = oldText;
	}

	/**
	 * @return Returns the replacementText.
	 */
	public String[] getReplacementText() {
		return replacementText;
	}

	/**
	 * @param replacementText
	 *            The replacementText to set.
	 */
	public void setReplacementText(String[] replacementText) {
		this.replacementText = replacementText;
	}

	String[] oldText;

	String[] replacementText;

	int bufferSize = 2000;
}
