/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: SOAPResponsePrimitiveParser.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-05-19 10:09:17 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

/**
 * SOAPResponseParser responsible for parsing soap responses that map to outputs
 * that can be directly represented with Primitive types (i.e. int, String,
 * String[]).
 * 
 * @author sowen
 * 
 */

public class SOAPResponsePrimitiveParser implements SOAPResponseParser {

	private static Logger logger = Logger.getLogger(SOAPResponsePrimitiveParser.class);

	private List outputNames;

	public SOAPResponsePrimitiveParser(List outputNames) {
		this.outputNames = outputNames;
	}

	/**
	 * Parses each SOAPBodyElement for the primitive type, and places it in the
	 * output Map
	 */
	public Map parse(List response) throws Exception {
		Map result = new HashMap();
		int c = 0;

		if (response.size() > 1)
			logger.error("More than one element to the response Vector when parsing for primitive types:" + response);
		RPCElement responseElement = (RPCElement) response.get(0);
		List params = responseElement.getParams();

		if (params.size() != outputNames.size())
			logger.error("Different number of output parameters to outputs expected.");

		for (Iterator paramIterator = params.iterator(); paramIterator.hasNext();) {
			RPCParam param = (RPCParam) paramIterator.next();
			Object value = param.getObjectValue();
			// use the param name if it matches the outputname list,
			// otherwise use the defined output name.
			// Outputs should come back in the order of the outputNames
			// as this is specified in the WSDL (only an issue for multiple
			// outputs which is very rare, and is going to be documented as
			// unrecommended for Taverna).
			if (outputNames.contains(param.getName())) {
				result.put(param.getName(), DataThingFactory.bake(value));
			} else {
				result.put(outputNames.get(c), DataThingFactory.bake(value));
			}
			c++;
		}

		return result;
	}

}
