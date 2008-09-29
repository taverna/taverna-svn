package net.sourceforge.taverna.scuflworkers.ncbi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

import net.sourceforge.taverna.io.StreamProcessor;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

/**
 * This class cleans up NCBI pseudoXML embedded in HTML pages, and turns it into
 * valid XML.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author mfortner
 * @version $Revision: 1.3 $
 */
public class NCBIXMLStreamProcessor extends ReplacementStreamProcessor implements StreamProcessor {
	
	private static Logger logger = Logger.getLogger(NCBIXMLStreamProcessor.class);

	protected String startTag;

	protected String endTag;

	protected Map outputMap = null;

	protected String outputFile = null;

	protected String ext = null;

	public NCBIXMLStreamProcessor() {

	}

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            The output filename
	 */
	public NCBIXMLStreamProcessor(Map outputMap, String startTag, String endTag, String outputFile, String ext) {
		this.oldText = new String[] { "&lt;", "&gt;", "&quot;", "<b>", "</b>", "<font color=\"#C00000\">", "</font>",
				"<font color=\"#4040FF\">", "<font color=\"#C00000\">", "<font color=\"#C04000\">",
				"<font color=\"#FF0000\">", "&amp;amp;" };
		this.replacementText = new String[] { "<", ">", "\"", "", "", "", "", "", "", "", "", "&amp;" };
		this.setOldText(oldText);
		this.setReplacementText(replacementText);
		this.startTag = startTag;
		this.endTag = endTag;
		this.outputMap = outputMap;
		this.outputFile = outputFile;
		this.ext = ext;
	}

	/**
	 * This method processes a stream of text from a URL endpoint.
	 * 
	 * @param stream
	 */
	public Map processStream(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream), this.bufferSize);
		String str;
		String lineEnding = System.getProperty("line.separator");
		StringWriter sw = new StringWriter();
		BufferedWriter out = new BufferedWriter(sw, this.bufferSize);
		String[] oldText = this.getOldText();
		String[] replacementText = this.getReplacementText();
		out.write("<?xml version=\"1.0\"?>");
		out.write("<searchResults>");
		boolean startTagFound = false;
		boolean endTagFound = false;
		FileWriter fw = null;
		logger.debug("EXT: " + this.ext);
		String filename = (this.ext == null || this.ext.equals("")) ? this.outputFile : this.outputFile + this.ext;

		logger.debug("\n\n\n**** NCBIXMLStreamProcessor: outputFile: " + filename);

		if (this.outputFile != null) {
			fw = new FileWriter(filename);
		}

		while ((str = in.readLine()) != null) {
			int startIndex = str.indexOf(startTag);
			int endIndex = str.indexOf(endTag);

			startTagFound = (startIndex != -1) || startTagFound;

			if (!startTagFound) {
				continue;
			}
			endTagFound = (endIndex != -1);
			if (startTagFound) {

				if (startIndex != -1 && str.length() > startTag.length()) {
					str = str.substring(startIndex);
				}

			}

			if (endTagFound) {
				if (endIndex == 0) {
					str = endTag;
				} else if (endIndex > 0) {
					str = str.substring(0, endIndex);
				}
				startTagFound = false;
				endTagFound = false;
			}

			if (this.outputFile != null) {
				fw.write(str);
			}

			out.write(str);
			out.write(lineEnding);
		}
		out.write("</searchResults>");
		out.close();
		String outStr = sw.toString();

		// now that we have all of the raw text,
		// replace all of the escaped characters with real characters
		// in order to make the XML parseable
		for (int i = 0; i < oldText.length; i++) {
			outStr = outStr.replaceAll(oldText[i], replacementText[i]);
		}

		if (this.outputFile != null) {
			fw.flush();
			fw.close();
		}

		outputMap.put("resultsXml", new DataThing(outStr));
		return this.outputMap;
	}

	/**
	 * @return Returns the endTag.
	 */
	public String getEndTag() {
		return endTag;
	}

	/**
	 * @param endTag
	 *            The endTag to set.
	 */
	public void setEndTag(String endTag) {
		this.endTag = endTag;
	}

	/**
	 * @return Returns the outputMap.
	 */
	public Map getOutputMap() {
		return outputMap;
	}

	/**
	 * @param outputMap
	 *            The outputMap to set.
	 */
	public void setOutputMap(Map outputMap) {
		this.outputMap = outputMap;
	}

	/**
	 * @return Returns the startTag.
	 */
	public String getStartTag() {
		return startTag;
	}

	/**
	 * @param startTag
	 *            The startTag to set.
	 */
	public void setStartTag(String startTag) {
		this.startTag = startTag;
	}

}
