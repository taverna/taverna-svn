package net.sf.taverna.service.rest.utils;

import static net.sf.taverna.service.rest.utils.XMLBeansUtils.xmlOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;

public class XmlBeansRepresentation extends OutputRepresentation {
	private static Logger logger =
		Logger.getLogger(XmlBeansRepresentation.class);

	private XmlObject xmlObject;

	public XmlBeansRepresentation(XmlObject xmlObject, MediaType mediaType) throws IllegalArgumentException {
		super(mediaType);
		this.xmlObject = xmlObject;
		// Strict in what we send..
		XMLBeansUtils.validate(xmlObject);
		// Since we serialize on write we don't know the size yet :-(
		setSize(UNKNOWN_SIZE);
		setCharacterSet(CharacterSet.valueOf(XMLBeansUtils.ENCODING));
	}


	@Override
	public void write(OutputStream outputStream) throws IOException {
		xmlObject.save(outputStream, xmlOptions);
	}

}
