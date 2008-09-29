/**
 * 
 */
package net.sourceforge.taverna.util;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

/**
 * @author Mark
 * 
 */
public class XmlUtil {

	/**
	 * This method transforms an XML document.
	 * 
	 * @param xslt
	 * @param xml
	 * @return
	 */
	public static String transform(String xslt, String xml) {
		String result = null;
		try {
			// Create transformer factory
			TransformerFactory factory = TransformerFactory.newInstance();

			// Use the factory to create a template containing the xsl file
			InputStream xsltStream = XmlUtil.class.getResourceAsStream(xslt);

			Templates template = factory.newTemplates(new StreamSource(xsltStream));

			// Use the template to create a transformer
			Transformer xformer = template.newTransformer();

			// Prepare the input file
			Source source = new StreamSource(new StringReader(xml));

			// Create a new document to hold the results
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.newDocument();
			
			StringWriter resultStr = new StringWriter();
			Result resultObj = new StreamResult(resultStr);

			// Apply the xsl file to the source file and write the result to the
			// output file
			xformer.transform(source, resultObj);
			
			result = resultStr.toString();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return result;
	}

}
