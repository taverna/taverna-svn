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
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-02-15 10:27:24 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;

public class XMLProxyConfig implements ProxyConfig {
	
	private static Logger logger = Logger.getLogger(XMLProxyConfig.class);
	
	private URL baseURL;
	private Map<String,WSDLConfig> wsdlMap = new HashMap<String, WSDLConfig>();
	
	@SuppressWarnings("unchecked")
	public XMLProxyConfig(Element element) throws ProxyConfigException,WSDLConfigException {
		Element storeElement = element.element("store");
		if (storeElement == null) throw new ProxyConfigException("No element 'store' defined");
		Element baseURLElement = storeElement.element("baseURL");
		if (baseURLElement == null) throw new ProxyConfigException("No element 'baseURL' defined for 'store'");
		
		String baseURLStr = baseURLElement.getTextTrim();
		try {
			baseURL=new URL(baseURLStr);			
		} catch (MalformedURLException e) {
			throw new ProxyConfigException("MalforedException with baseURL:'"+baseURLStr+"'");
		}
		
		logger.info("BaseURL defined in config as:"+baseURLStr);
		
		Element wsdlsElement = element.element("wsdls");
		if (wsdlsElement!=null) {
			List<Element> wsdlElements = (List<Element>)wsdlsElement.elements("wsdl");
			logger.info(wsdlElements.size()+" WSDLS found in config");			
			for (Element wsdlElement : wsdlElements)				
			{
				WSDLConfig wsdlConfig = new XMLWSDLConfig(wsdlElement);
				wsdlMap.put(wsdlConfig.getWSDLID(),wsdlConfig);
				logger.info("WSDL added from config with ID:"+wsdlConfig.getWSDLID()+", Endpoint:"+wsdlConfig.getEndpoint());
			}
		}
	}

	public URL getStoreBaseURL() {
		return baseURL;
	}

	public WSDLConfig getWSDLConfigForID(String ID) {
		return wsdlMap.get(ID);
	}

}
