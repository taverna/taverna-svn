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
 * Filename           $RCSfile: ProxyConfig.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 14 Feb 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration;

import java.net.URL;
import java.util.List;

import uk.org.mygrid.dataproxy.configuration.impl.WSDLConfigException;

/**
 * The interface to the Proxy servers configuration. The underlying
 * implementation defines how these settings are stored.
 * 
 * @author Stuart Owen
 * 
 */
public interface ProxyConfig {
		
	/**
	 * Defines the full http://<host>:<port>/<context>/ to the base context of
	 * the installation. This is set up during the initial administration of the
	 * server. It is required to generate the endpoint, data servlet, wsdl
	 * servlet, schema servlet urls.
	 * 
	 * @return the path
	 */
	public String getContextPath();
	
	/**
	 * Sets the context path, which is a String of the form http://<host>:<port>/<context>/
	 * @param contextPath
	 */
	public void setContextPath(String contextPath);
		
	/**
	 * Provides a URL to the base location that the data will be stored. It needs to be readable
	 * and writable by the server.
	 * @return a URL to a file location
	 */
	public URL getStoreBaseURL();
	
	/**
	 * Sets the base location that the data will be stored. It needs to be readable and writable by the server.
	 * @param storeBaseURL
	 */
	public void setStoreBaseURL(URL storeBaseURL);
	
	/**
	 * Provides WSDL configuration details for the WSDL matching the given ID.
	 * 
	 * @param the WSDL ID
	 * @return a WSDLConfig object
	 */
	public WSDLConfig getWSDLConfigForID(String ID);
	
	/**
	 * Adds a new WSDL to the configuration.
	 * @param config - a WSDLConfig object.
	 * @throws WSDLConfigException if the configuration is invalid
	 */
	public void addWSDLConfig(WSDLConfig config) throws WSDLConfigException;
	
	/**
	 * Deletes the configuration that matches the given WSDLConfig object.
	 * @param config
	 */
	public void deleteWSDLConfig(WSDLConfig config);
	
	/**
	 * Provides a List of all WSDLConfig's defined for this server.
	 * @return
	 */
	public List<WSDLConfig> getWSDLConfigs();
	
	/** 
	 * @return a String representation of the entire Proxy configuration, including WSDLConfigs.
	 */
	public String toStringForm();
}
