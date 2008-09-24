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
 * Revision           $Revision: 1.15 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-23 10:31:17 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

/**
 * An XML based implementation of WSDLConfig
 * 
 * @see uk.org.mygrid.dataproxy.configuration.WSDLConfig
 * @author Stuart Owen
 */

public class XMLWSDLConfig implements WSDLConfig {
	
	private static Logger logger = Logger.getLogger(XMLWSDLConfig.class);
	
	private String ID;
	private String address;
	private List<String> endpoints=new ArrayList<String>();
	private String name;	
	private List<ElementDefinition> elements = new ArrayList<ElementDefinition>();	
		
	@SuppressWarnings("unchecked")
	public XMLWSDLConfig(Element element) throws WSDLConfigException {		
		
		Element child = element.element("address");
		if (child==null) throw new WSDLConfigException("No element 'address' defined");
		address=child.getTextTrim();
		
		child = element.element("id");
		if (child==null) throw new WSDLConfigException("No element 'id' defined");
		ID=child.getTextTrim();		
		
		child = element.element("name");
		if (child==null) throw new WSDLConfigException("No element 'name' defined");
		name=child.getTextTrim();
		
		child = element.element("endpoints");
		if (child==null) throw new WSDLConfigException("No element 'endpoints' defined");
		for (Element endp : (List<Element>)child.elements("endpoint")) {			
			endpoints.add(endp.getTextTrim());
		}
		
		child = element.element("elements");
		if (child!=null) {
			List<Element> elements = child.elements("element");
			logger.info(elements.size()+" elements defined in config for WSDLID:"+ID);
			for (Element el : elements) {
				Element name=el.element("name");
				Element namespaceURI=el.element("namespaceURI");
				Element path = el.element("path");
				Element operation = el.element("operation");
				Element typeElement = el.element("type");
				
				if (name == null) throw new WSDLConfigException("No element 'name' defined within the element block");
				if (path == null) throw new WSDLConfigException("No element 'path' defined within the element block");
				if (operation == null) throw new WSDLConfigException("No element 'operation' defined within the element block");
				
				if (namespaceURI == null) logger.warn("No namespace defined for Element name='"+name.getText()+"' for WSDLID:"+ID);
				
				if (typeElement!=null) {
					Element typeChild = (Element)typeElement.elements().get(0);
					typeElement = (Element)typeChild.clone(); //to seperate from existing parent.
				}
				
				ElementDefinition def = new ElementDefinition(name.getTextTrim(),namespaceURI!=null ? namespaceURI.getTextTrim() : "",path.getTextTrim(),operation.getTextTrim());
				this.elements.add(def);						
			}
		}				
	}	

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public List<ElementDefinition> getElements() {
		return elements;
	}

	public List<String> getEndpoints() {
		return endpoints;
	}	

	public String getWSDLID() {
		return ID;
	}
	
	public Element toElement() {
		Document doc = DocumentFactory.getInstance().createDocument();
		Element wsdlChild = doc.addElement("wsdl");
		wsdlChild.addElement("id").setText(getWSDLID());
		wsdlChild.addElement("name").setText(getName());
		wsdlChild.addElement("address").setText(getAddress());			
		Element endpoints=wsdlChild.addElement("endpoints");
		for (String endpoint : getEndpoints()) {
			endpoints.addElement("endpoint").setText(endpoint);
		}
		
		if (elements.size()>0) {
			Element elChild = wsdlChild.addElement("elements");
			for (ElementDefinition elDef : elements) {
				Element element = elChild.addElement("element");
				element.addElement("name").setText(elDef.getElementName());
				element.addElement("namespaceURI").setText(elDef.getNamespaceURI());
				element.addElement("path").setText(elDef.getPath());
				element.addElement("operation").setText(elDef.getOperation());					
			}
		}
		return doc.getRootElement();
	}

}
