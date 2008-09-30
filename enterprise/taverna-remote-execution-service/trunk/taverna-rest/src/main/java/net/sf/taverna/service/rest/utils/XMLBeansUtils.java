package net.sf.taverna.service.rest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class XMLBeansUtils {
	public static final String ENCODING = "utf-8";

	private static Logger logger = Logger.getLogger(XMLBeansUtils.class);

	private static XmlOptions makeXMLOptions() {
		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setLoadStripWhitespace();
		xmlOptions.setSavePrettyPrint();
		xmlOptions.setSavePrettyPrintIndent(4);
		xmlOptions.setSaveOuter();
		xmlOptions.setUseDefaultNamespace();
		xmlOptions.setSaveAggressiveNamespaces();
		xmlOptions.setCharacterEncoding(ENCODING);
		Map<String, String> ns = new HashMap<String, String>();
		ns.put("http://www.w3.org/1999/xlink", "xlink");
		ns.put("http://purl.org/dc/terms/", "dcterms");
		ns.put("http://purl.org/dc/elements/1.1/", "dc");
		xmlOptions.setSaveSuggestedPrefixes(ns);
		return xmlOptions;
	}

	public static XmlOptions xmlOptions = makeXMLOptions();
	

	/**
	 * Validate the XML object against the XML schema.
	 * XML warnings and errors are logged.
	 * 
	 * @throws IllegalArgumentException if the object didn't validate
	 */
	public static void validate(XmlObject xmlObject) throws IllegalArgumentException {
		XmlOptions validateOptions = new XmlOptions();
		List<Object> errors = new ArrayList<Object>(); 
		validateOptions.setErrorListener(errors);
		if (!xmlObject.validate(validateOptions)) {
			for (Object error: errors) {
				logger.warn(error);
			}
			throw new IllegalArgumentException("xml object does not validate");
		}
	}
	
}
