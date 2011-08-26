package net.sourceforge.taverna.scuflworkers.ncbi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

/**
 * This class is capable of downloading, cleaning up and transforming NCBI XML.
 * The transformed XML is placed in the outputMap using the "resultsXml" key.
 * 
 * If the outputFile parameter is specified, then the class will write the
 * transformed xml out to the specified file.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class NCBIXSLTStreamProcessor extends NCBIXMLStreamProcessor {
	
	private static Logger logger = Logger.getLogger(NCBIXSLTStreamProcessor.class);

	private String xslt;

	public NCBIXSLTStreamProcessor(Map outputMap, String startTag, String endTag, String xslt, String outputFile) {
		this.xslt = xslt;
		this.outputFile = outputFile;
		this.oldText = new String[] { "&lt;", "&gt;", "&quot;", "<b>", "</b>", "<font color=\"#C00000\">", "</font>",
				"<font color=\"#4040FF\">", "<font color=\"#C00000\">", "<font color=\"#C04000\">", "&amp;amp;" };
		this.replacementText = new String[] { "<", ">", "\"", "", "", "", "", "", "", "", "&amp;" };
		this.setOldText(oldText);
		this.setReplacementText(replacementText);
		this.startTag = startTag;
		this.endTag = endTag;
		this.outputMap = outputMap;
	}

	/**
	 * This method processes the input stream and places the results in the
	 * output map.
	 * 
	 * @see net.sourceforge.taverna.io.StreamProcessor#processStream(java.io.InputStream)
	 */
	public Map processStream(InputStream stream) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream), this.bufferSize);
		String str;
		String lineEnding = System.getProperty("line.separator");
		// StringWriter sw = new StringWriter();
		// BufferedWriter out = new BufferedWriter(sw, this.bufferSize);
		String[] oldText = this.getOldText();
		String[] replacementText = this.getReplacementText();
		FileWriter fw = null;
		if (this.outputFile != null) {
			fw = new FileWriter(this.outputFile);
		}

		boolean startTagFound = false;
		boolean endTagFound = false;

		// Create transformer factory
		TransformerFactory factory = TransformerFactory.newInstance();

		// Use the factory to create a template containing the xsl file
		Templates template;
		StringWriter sw = new StringWriter();

		logger.info("Reading xml & starting transform");

		try {
			template = factory.newTemplates(new StreamSource(new FileInputStream(this.xslt)));

			// Use the template to create a transformer
			Transformer xformer = template.newTransformer();

			StringReader reader = new StringReader("");

			StreamSource source = new StreamSource(reader);

			Result result = new StreamResult(sw);

			sw.write("<?xml version=\"1.0\"?>");
			sw.write("<searchResults>");
			int count = 0;

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

				for (int i = 0; i < oldText.length; i++) {
					str = str.replaceAll(oldText[i], replacementText[i]);
				}

				// Apply the xsl file to the source file and write the result to
				// the
				// output file
				reader.read(str.toCharArray());

				xformer.transform(source, result);
				logger.debug("count: " + count);
				count++;
			}
			sw.write("</searchResults>");
			sw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			// An error occurred while applying the XSL file
			// Get location of error in input file
			SourceLocator locator = e.getLocator();
			int col = locator.getColumnNumber();
			int line = locator.getLineNumber();
			String publicId = locator.getPublicId();
			String systemId = locator.getSystemId();
		}

		if (this.outputFile != null) {
			fw.write(sw.toString());
			fw.flush();
			fw.close();
		}
		outputMap.put("resultsXml", new DataThing(sw.toString()));
		return this.outputMap;
	}

}