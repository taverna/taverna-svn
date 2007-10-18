package net.sf.taverna.t2.activities.wsdl.soap;

import java.io.IOException;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.SOAPBodyElement;
import net.sf.taverna.t2.activities.wsdl.parser.UnknownOperationException;
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

