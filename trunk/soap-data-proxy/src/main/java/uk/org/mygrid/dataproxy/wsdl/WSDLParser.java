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
 * Filename           $RCSfile: WSDLParser.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 6 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.wsdl;

import java.util.List;

import org.dom4j.Element;

/**
 * An interface defining the ability to parse a WSDL, providing a list of the available operations
 * and also the ability to examine the types declared within the WSDL schema.
 * 
 * @author Stuart Owen
 */

public interface WSDLParser {
	
	/**
	 * Flushes any cached wsdl definitions.
	 * @param wsdlUrl
	 */
	public void flush(String wsdlUrl);
	
	/**
	 * Expands a type into its nested elements, represented as XML.
	 * 
	 * @param wsdlUrl
	 * @param type
	 * @return
	 * @throws WSDLParsingException
	 */
	public Element expandType(String wsdlUrl,Element type) throws WSDLParsingException;
	
	/**
	 * Provides a list of the available operations, and also the response types (unexpanded).
	 * @param wsdlUrl
	 * @return
	 * @throws WSDLParsingException
	 */
	public List<Element> parseOperations(String wsdlUrl) throws WSDLParsingException;
}

