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
 * Filename           $RCSfile: XMLProxyConfig.java,v $
 * Revision           $Revision: 1.13 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-20 11:33:58 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.xml.ElementDefinition;

/**
 * An XML based implementation of the ProxyConfig
 * 
 * @see uk.org.mygrid.dataproxy.configuration.ProxyConfig 
 * @author Stuart Owen
 *
 */

public class XMLProxyConfig implements ProxyConfig {
	
	private static Logger logger = Logger.getLogger(XMLProxyConfig.class);
	
	private URL storeBaseURL;
	private String contextPath;
	private Map<String,WSDLConfig> wsdlMap = new HashMap<String, WSDLConfig>();		

	@SuppressWarnings("unchecked")
	public XMLProxyConfig(Element element) throws ProxyConfigException,WSDLConfigException {
		Element contextPathElement = element.element("contextPath");
		if (contextPathElement == null) throw new ProxyConfigException("No element 'contextPath' defined");
		contextPath = contextPathElement.getTextTrim();
		
		Element storeElement = element.element("store");
		if (storeElement == null) throw new ProxyConfigException("No element 'store' defined");
		Element baseURLElement = storeElement.element("baseURL");
		if (baseURLElement == null) throw new ProxyConfigException("No element 'baseURL' defined for 'store'");
		
		String baseURLStr = baseURLElement.getTextTrim();
		try {
			storeBaseURL=new URL(baseURLStr);			
		} catch (MalformedURLException e) {
			throw new ProxyConfigException("MalforedException with baseURL:'"+baseURLStr+"'");
		}
		
		logger.debug("BaseURL defined in config as:"+baseURLStr);
		
		Element wsdlsElement = element.element("wsdls");
		if (wsdlsElement!=null) {
			List<Element> wsdlElements = (List<Element>)wsdlsElement.elements("wsdl");
			logger.info(wsdlElements.size()+" WSDLS found in config");			
			for (Element wsdlElement : wsdlElements)				
			{
				WSDLConfig wsdlConfig = new XMLWSDLConfig(wsdlElement);
				wsdlMap.put(wsdlConfig.getWSDLID(),wsdlConfig);
				if (logger.isDebugEnabled()) logger.debug("WSDL added from config with ID:"+wsdlConfig.getWSDLID()+", Endpoints:"+wsdlConfig.getEndpoints());
			}
		}
	}
	
	public String getContextPath() {		
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		if (!contextPath.endsWith("/")) contextPath+="/";
		this.contextPath = contextPath;		
	}

	public URL getStoreBaseURL() {
		return storeBaseURL;
	}
	
	public void setStoreBaseURL(URL storeBaseURL) {
		this.storeBaseURL=storeBaseURL;
	}
	
	public void deleteWSDLConfig(WSDLConfig config) {
		String id = config.getWSDLID();
		if (wsdlMap.containsKey(id)) {
			wsdlMap.remove(id);
		}
		else {
			logger.warn("Trying to delete a WSDLConfig that isn't defined, ID="+id);
		}		
	}

	public WSDLConfig getWSDLConfigForID(String ID) {
		return wsdlMap.get(ID);
	}

	public void addWSDLConfig(WSDLConfig config) throws WSDLConfigException {
		String id = config.getWSDLID();
		if (id==null) throw new WSDLConfigException("WSDL has no ID");
		if (wsdlMap.containsKey(id)) throw new WSDLConfigException("Duplicate wsdlID: "+id);
		wsdlMap.put(id, config);
	}
	
	/**
	 * 
	 * @return an org.dom4j.Element containing the serialized XML representation of the ProxyConfig
	 */
	private Element toElement() {
		Document doc = DocumentFactory.getInstance().createDocument();
		Element config = doc.addElement("config");
		Element contextPathElement = config.addElement("contextPath");
		contextPathElement.setText(contextPath);
		Element store = config.addElement("store");
		store.addElement("baseURL").setText(getStoreBaseURL().toExternalForm());		
		if (wsdlMap.size()>0) {
			Element wsdls = config.addElement("wsdls");
			for (WSDLConfig wsdl : wsdlMap.values()) {
				if (wsdl instanceof XMLWSDLConfig) {
					wsdls.add(((XMLWSDLConfig)wsdl).toElement());
				}
				else {												
					Element wsdlChild = wsdls.addElement("wsdl");
					wsdlChild.addElement("id").setText(wsdl.getWSDLID());
					wsdlChild.addElement("name").setText(wsdl.getName());
					wsdlChild.addElement("address").setText(wsdl.getAddress());					
					Element endpoints = wsdlChild.addElement("endpoints");
					for (String endp : wsdl.getEndpoints()) {
						endpoints.addElement("endpoint").setText(endp);
					}
					
					if (wsdl.getElements().size()>0) {
						Element elChild = wsdlChild.addElement("elements");
						for (ElementDefinition elDef : wsdl.getElements()) {
							Element element = elChild.addElement("element");
							element.addElement("name").setText(elDef.getElementName());
							element.addElement("namespaceURI").setText(elDef.getNamespaceURI());
							element.addElement("path").setText(elDef.getPath());
							element.addElement("operation").setText(elDef.getOperation());													
						}
					}					
				}
			}
		}
		return doc.getRootElement();
	}
	
	/**
	 * @return the XML representation of the ProxyConfig as a String
	 */
	public String toStringForm() {
		String xml = toElement().asXML();
		if (logger.isDebugEnabled()) {
			logger.debug("Generating config xml");
			logger.debug(xml);
		}
		return xml;
	}

	public List<WSDLConfig> getWSDLConfigs() {
		List<WSDLConfig> result = new ArrayList<WSDLConfig>(wsdlMap.values());
		return result;
	}
}
