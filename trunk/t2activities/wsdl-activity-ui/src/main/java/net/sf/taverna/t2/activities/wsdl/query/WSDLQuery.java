/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
