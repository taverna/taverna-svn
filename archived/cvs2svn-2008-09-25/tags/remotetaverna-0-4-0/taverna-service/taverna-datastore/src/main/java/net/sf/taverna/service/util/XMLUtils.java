package net.sf.taverna.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;

import net.sf.taverna.service.interfaces.ParseException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Utility methods to parse and generate XML documents such as Baclava data
 * documents and XSCUFL.
 * 
 * @author Stian Soiland
 */
public class XMLUtils {

	private static Logger logger = Logger.getLogger(XMLUtils.class);

	// FIXME: Use Format.getCompactFormat()
	public static XMLOutputter xmlOutputter =
		new XMLOutputter(Format.getPrettyFormat());

	/**
	 * Create a Baclava data document from a map of names to {@link DataThing}s
	 * 
	 * @see #parseDataDoc(String)
	 * @param values
	 *            A {@link Map} with {@link String} keys and {@link DataThing}
	 *            values.
	 * @return A Baclava data document as XML
	 */
	public static String makeDataDocument(Map<String, DataThing> values) {
		org.jdom.Document doc = DataThingXMLFactory.getDataDocument(values);
		String xmlString = xmlOutputter.outputString(doc);
		return xmlString;
	}

	/**
	 * Create a XScufl document from a {@link ScuflModel}.
	 * 
	 * @see #parseXScufl(String)
	 * @param model
	 *            A {@link ScuflModel} of the workflow.
	 * @return A XScufl XML representation of the workflow.
	 */
	public static String makeScufl(ScuflModel model) {
		return XScuflView.getXMLText(model);
	}

	/**
	 * Parse a XScufl document from XML and return a {@link ScuflModel}.
	 * 
	 * @see #makeScufl(ScuflModel)
	 * @param xscufl
	 *            A XScufl XML representation of the workflow.
	 * @return A {@link ScuflModel} of the workflow.
	 * @throws ParseException
	 *             If the XML was not valid XML or not a XScufl document
	 */
	public static ScuflModel parseXScufl(String xscufl) throws ParseException {
		ScuflModel model = new ScuflModel();
		try {
			XScuflParser.populate(xscufl, model, null);
		} catch (ScuflException ex) {
			logger.warn("Could not load workflow", ex);
			throw new ParseException("Could not load workflow", ex);
		}
		return model;
	}
	
	/**
	 * Parse a Baclava data document and return a map of names to
	 * {@link DataThing}s.
	 * 
	 * @see #makeDataDocument(Map)
	 * @param A
	 *            Baclava data document as XML
	 * @return A {@link Map} with {@link String} keys and {@link DataThing}
	 *         values.
	 * @throws ParseException
	 *             If the XML was not valid XML or not a Baclava data document
	 */
	public static Map<String, DataThing> parseDataDoc(String xml)
		throws ParseException {
		Document doc = parseXML(xml);
		return DataThingXMLFactory.parseDataDocument(doc);
	}

	public static Document parseXML(InputStream stream) throws ParseException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(stream);
		} catch (JDOMException e) {
			throw new ParseException("Could not parse XML document", e);
		}
		return doc;
	}
	
	public static Document parseXML(String xml) throws ParseException {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			try {
				doc = builder.build(new StringReader(xml));
			} catch (JDOMException e) {
				throw new ParseException("Could not parse XML document", e);
			}
		} catch (IOException e) {
			// Unexpected with StringReader
			logger.error("Could not read XML with StringReader", e);
			throw new RuntimeException(e);
		}
		return doc;
	}
	
	public static String makeXML(Element elem) {
		Document doc = new Document(elem);
		return makeXML(doc);
	}
	
	public static String makeXML(Document doc) {
		return xmlOutputter.outputString(doc);
	}

}
