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
 * Filename           $RCSfile: XMLWSDLConfig.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-15 14:34:22 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDef;

public class XMLWSDLConfig implements WSDLConfig {
	
	private static Logger logger = Logger.getLogger(XMLWSDLConfig.class);
	
	private String ID;
	private String address;
	private String endpoint;
	private List<ElementDef> elements = new ArrayList<ElementDef>();	
		
	@SuppressWarnings("unchecked")
	public XMLWSDLConfig(Element element) throws WSDLConfigException {		
		
		Element child = element.element("address");
		if (child==null) throw new WSDLConfigException("No element 'address' defined");
		address=child.getTextTrim();
		
		child = element.element("id");
		if (child==null) throw new WSDLConfigException("No element 'id' defined");
		ID=child.getTextTrim();
		
		child = element.element("endpoint");
		if (child==null) throw new WSDLConfigException("No element 'endpont' defined");
		endpoint=child.getTextTrim();		
		
		child = element.element("elements");
		if (child!=null) {
			List<Element> elements = child.elements("element");
			logger.info(elements.size()+" elements defined in config for WSDLID:"+ID);
			for (Element el : elements) {
				Element name=el.element("name");
				Element namespaceURI=el.element("namespaceURI");				
				
				if (name == null) throw new WSDLConfigException("No element 'name' defined within the element block");				
				if (namespaceURI == null) logger.warn("No namespace defined for Element name='"+name.getText()+"' for WSDLID:"+ID);
				ElementDef def = new ElementDef(name.getTextTrim(),namespaceURI!=null ? namespaceURI.getTextTrim() : "");
				this.elements.add(def);						
			}
		}				
	}

	public String getAddress() {
		return address;
	}

	public List<ElementDef> getElements() {
		return elements;
	}

	public String getEndpoint() {
		return endpoint;
	}	

	public String getWSDLID() {
		return ID;
	}

}
