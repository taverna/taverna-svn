package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A response parser specifically for literal use services that return primative types.
 * It extends the SOAPReponseLiteralParser, but unwraps the result from the enclosing XML
 * to expose the primitive result.
 * 
 * This is specially designed for unwrapped/literal type services, and RPC/literal services (untested). 
 * @author Stuart
 *
 */

public class SOAPResponsePrimitiveLiteralParser extends
		SOAPResponseLiteralParser {

	private static Logger logger = Logger
			.getLogger(SOAPResponsePrimitiveLiteralParser.class);
	
	public SOAPResponsePrimitiveLiteralParser(List outputNames) {
		super(outputNames);
	}

	@Override
	public Map parse(List response) throws Exception {
		Map result = super.parse(response);
		DataThing thing = (DataThing)result.get(getOutputName());
		if (thing!=null) {
			String xml = thing.getDataObject().toString();
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
				Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
			
				Node node = doc.getFirstChild();
				DataThing newThing=DataThingFactory.bake(node.getFirstChild().getNodeValue());
				result.put(getOutputName(), newThing);
			}
			catch(Exception e) {
				logger.error("Exception unwrapping xml response",e);
			}
		}
		return result;
	}
	
	
}

	
