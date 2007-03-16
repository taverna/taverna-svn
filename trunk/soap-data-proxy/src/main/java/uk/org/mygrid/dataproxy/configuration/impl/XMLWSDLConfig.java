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
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-16 10:00:31 $
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
import uk.org.mygrid.dataproxy.xml.ElementDef;

public class XMLWSDLConfig implements WSDLConfig {
	
	private static Logger logger = Logger.getLogger(XMLWSDLConfig.class);
	
	private String ID;
	private String address;
	private String endpoint;
	private String name;
	private String filename;
	private List<ElementDef> elements = new ArrayList<ElementDef>();	
		
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
		
		child = element.element("endpoint");
		if (child==null) throw new WSDLConfigException("No element 'endpiont' defined");
		endpoint=child.getTextTrim();	
		
		child = element.element("filename");
		if (child==null) throw new WSDLConfigException("No element 'filename' defined");
		filename=child.getTextTrim();
		
		child = element.element("elements");
		if (child!=null) {
			List<Element> elements = child.elements("element");
			logger.info(elements.size()+" elements defined in config for WSDLID:"+ID);
			for (Element el : elements) {
				Element name=el.element("name");
				Element namespaceURI=el.element("namespaceURI");
				Element path = el.element("path");
				Element operation = el.element("operation");
				
				if (name == null) throw new WSDLConfigException("No element 'name' defined within the element block");
				if (path == null) throw new WSDLConfigException("No element 'path' defined within the element block");
				if (operation == null) throw new WSDLConfigException("No element 'operation' defined within the element block");
				
				if (namespaceURI == null) logger.warn("No namespace defined for Element name='"+name.getText()+"' for WSDLID:"+ID);
				
				ElementDef def = new ElementDef(name.getTextTrim(),namespaceURI!=null ? namespaceURI.getTextTrim() : "",path.getTextTrim(),operation.getTextTrim());
				this.elements.add(def);						
			}
		}				
	}

	public String getWSDLFilename() {
		return filename;
	}

	public String getName() {
		return name;
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
	
	public Element toElement() {
		Document doc = DocumentFactory.getInstance().createDocument();
		Element wsdlChild = doc.addElement("wsdl");
		wsdlChild.addElement("id").setText(getWSDLID());
		wsdlChild.addElement("name").setText(getName());
		wsdlChild.addElement("address").setText(getAddress());	
		wsdlChild.addElement("filename").setText(getWSDLFilename());
		wsdlChild.addElement("endpoint").setText(getEndpoint());		
		
		if (elements.size()>0) {
			Element elChild = wsdlChild.addElement("elements");
			for (ElementDef elDef : elements) {
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
