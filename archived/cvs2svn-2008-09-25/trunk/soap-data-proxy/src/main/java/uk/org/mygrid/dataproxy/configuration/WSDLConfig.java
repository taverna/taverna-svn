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
 * Filename           $RCSfile: WSDLConfig.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 16:30:16 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration;

import java.util.List;

import uk.org.mygrid.dataproxy.xml.ElementDefinition;

/**
 * An interface to the configuration details for a single WSDL.
 * @author Stuart Owen
 *
 */

public interface WSDLConfig {
	
	/**
	 * 
	 * @return the ID of the WSDL
	 */
	public String getWSDLID();
	
	/**
	 * 
	 * @return the original address of the WSDL
	 */
	public String getAddress();
	
	/**
	 * 
	 * @return the name for this WSDL as provided by the user.
	 */
	public String getName();	
	
	/**
	 * 
	 * @return a list of Element definitions for the WSDL. These define which elements should be stored and replaced with a reference.
	 */
	public List<ElementDefinition> getElements();	
	
	/**
	 * @return a list the original endpoints for the WSDL.
	 */
	public List<String> getEndpoints();		
}
