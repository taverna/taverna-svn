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
 * Filename           $RCSfile: NewWSDLConfig.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-19 16:30:15 $
 *               by   $Author: sowen70 $
 * Created on 5 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

/**
 * An implementation of WSDLConfig for a new wsdl. It allows access to provide new values to the 
 * settings.
 * 
 * @see uk.org.mygrid.dataproxy.configuration.WSDLConfig
 * 
 * @author Stuart Owen
 */

public class NewWSDLConfig implements WSDLConfig {
	
	private String name;
	private String id;	
	private List<String> endpoints = new ArrayList<String>();
	private String address;
	private List<ElementDefinition> elements = new ArrayList<ElementDefinition>();
	public List<ElementDefinition> getElements() {
		return elements;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setElements(List<ElementDefinition> elements) {
		this.elements = elements;
	}
	public List<String> getEndpoints() {
		return endpoints;
	}
	public void addEndpoint(String endpoint) {
		this.endpoints.add(endpoint);
	}
	
	public String getWSDLID() {
		return id;
	}
	public void setWSDLID(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}
