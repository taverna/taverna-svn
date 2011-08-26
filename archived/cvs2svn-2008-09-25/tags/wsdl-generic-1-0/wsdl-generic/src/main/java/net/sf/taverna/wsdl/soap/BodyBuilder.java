package net.sf.taverna.wsdl.soap;

import java.io.IOException;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import net.sf.taverna.wsdl.parser.UnknownOperationException;

import org.apache.axis.message.SOAPBodyElement;
import org.xml.sax.SAXException;

/**
 * Interface to a class that is responsible for creating the SOAP body elements from the provided inputs
 * for invoking a SOAP based Web-service.
 * 
 * @author Stuart Owen
 */
@SuppressWarnings("unchecked")
public interface BodyBuilder {
	
	public SOAPBodyElement build(Map inputMap)
			throws WSDLException, ParserConfigurationException, SOAPException,
			IOException, SAXException, UnknownOperationException;
	
}

