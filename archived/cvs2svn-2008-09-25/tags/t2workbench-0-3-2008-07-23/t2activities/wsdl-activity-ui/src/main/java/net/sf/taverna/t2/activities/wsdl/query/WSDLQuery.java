package net.sf.taverna.t2.activities.wsdl.query;

import java.io.IOException;
import java.util.List;

import javax.wsdl.Operation;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class WSDLQuery extends ActivityQuery {
	
	private static Logger logger = Logger.getLogger(WSDLQuery.class);
	
	public WSDLQuery(String property) {
		super(property);
	}

	@Override
	public void doQuery() {
		String wsdl = getProperty();
		logger.info("About to parse wsdl:"+wsdl);
		WSDLParser parser = null;
		try {
			parser = new WSDLParser(wsdl);
		} catch (ParserConfigurationException e) {
			logger.error("Error configuring the WSDL parser",e);
		} catch (WSDLException e) {
			logger.error("There was an error with the wsdl:"+wsdl,e);
		} catch (IOException e) {
			logger.error("There was an IO error parsing the wsdl:"+wsdl,e);
		} catch (SAXException e) {
			logger.error("There was an error with the XML in the wsdl:"+wsdl,e);
		}
		
		if (parser!=null) {
			List<Operation> operations = parser.getOperations();
			for (Operation op : operations) {
				WSDLActivityItem item = new WSDLActivityItem();
				try {
					item.setOperation(op.getName());
					item.setUse(parser.getUse(item.getOperation()));
					item.setStyle(parser.getStyle());
					item.setUrl(wsdl);
					add(item);
				} catch (UnknownOperationException e) {
					logger.error("Encountered an unexpected operation name:"+item.getOperation(),e);
				}
			}
		}
		logger.info("Finished parsing WSDL:"+wsdl);

	}

}
