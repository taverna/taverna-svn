package net.sf.taverna.service.rest.utils;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class XmlBeansRepresentation extends OutputRepresentation {

	// private static Logger logger =
	// Logger.getLogger(XmlBeansRepresentation.class);

	private URIFactory uriFactory;

	private XmlObject xmlObject;

	public XmlBeansRepresentation(XmlObject xmlObject, MediaType mediaType, URIFactory uriFactory)
		throws IllegalArgumentException {
		super(mediaType);
		this.xmlObject = xmlObject;
		this.uriFactory = uriFactory;
		// Strict in what we send..
		XMLBeansUtils.validate(xmlObject);
		// Since we serialize on write we don't know the size yet :-(
		setSize(UNKNOWN_SIZE);
		setCharacterSet(CharacterSet.valueOf(XMLBeansUtils.ENCODING));
		if (MediaType.TEXT_XML.includes(mediaType)) {
			insertStylesheet();
		}
	}

	public void insertStylesheet() {
		XmlCursor cursor = xmlObject.newCursor();
		cursor.toFirstChild();
		cursor.insertProcInst("xml-stylesheet", "type='text/xsl' href='"
			+ uriFactory.getHTMLRoot() + "/rest.xsl'");
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		xmlObject.save(outputStream, xmlOptions);
	}

}
