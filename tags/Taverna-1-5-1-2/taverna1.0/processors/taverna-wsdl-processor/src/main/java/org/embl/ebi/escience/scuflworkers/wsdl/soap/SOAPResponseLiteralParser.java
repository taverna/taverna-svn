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
 * Filename           $RCSfile: SOAPResponseLiteralParser.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-08-25 13:56:59 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.wsdl.soap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.w3c.dom.Element;

/**
 * Responsible for parsing the SOAP response from calling a Literal based
 * service.
 * 
 * @author sowen
 * 
 */

public class SOAPResponseLiteralParser implements SOAPResponseParser {

	private static Logger logger = Logger
			.getLogger(SOAPResponseLiteralParser.class);

	List outputNames;

	public SOAPResponseLiteralParser(List outputNames) {
		this.outputNames = outputNames;
	}

	/**
	 * Expects a list containing a single SOAPBodyElement, the contents of which
	 * are transferred directly to the output, converted to a String, and placed
	 * into the outputMaP which is returned
	 * 
	 * @return Map of the outputs
	 */
	public Map parse(List response) throws Exception {
		Map result = new HashMap();

		if (response.size() > 1)
			logger
					.warn("Document style response unexpectedly contained more than one RPCElement, number of elements="
							+ response.size());

		SOAPBodyElement rpcElement = (SOAPBodyElement) response.get(0);

		Element dom = rpcElement.getAsDOM();

		String outputName = getOutputName();
		String xml = XMLUtils.ElementToString(dom);

		result.put(outputName, new DataThing(xml));

		return result;
	}

	private String getOutputName() {
		String result = "";
		for (Iterator iterator = outputNames.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			if (!name.equals("attachmentList")) {
				result = name;
				break;
			}
		}
		return result;
	}
}
