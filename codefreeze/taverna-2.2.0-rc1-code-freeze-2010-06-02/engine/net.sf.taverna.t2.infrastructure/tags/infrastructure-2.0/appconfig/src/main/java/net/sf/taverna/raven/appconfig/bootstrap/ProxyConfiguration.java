/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.raven.appconfig.bootstrap;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

/**
 * This is scaled down version of the MyGridConfiguration which is used for determining
 * the proxy settings required for the bootstrap. MyGridConfiguration itself wasn't possible
 * to use in the Bootstrap since it contains too many dependencies, particularly to the logging system.
 * 
 * @author Stuart Owen
 *
 */
public class ProxyConfiguration extends AbstractConfiguration {

	@Override
	protected String getConfigurationFilename() {
		return "mygrid.properties";
	}

	@Override
	protected boolean isSystemOverrided() {
		return true;
	}
	
	/**
	 * Takes the the http.proxy<...> settings from the properties
	 * and transfers them to a System.property. If username and password
	 * are defined then an authenticator is also set up. 
	 *
	 */
	public void initialiseProxySettings() {
		Properties props = getProperties();
		
		if (props.getProperty("http.proxyHost")!=null) System.setProperty("http.proxyHost", props.getProperty("http.proxyHost"));
		if (props.getProperty("http.proxyPort")!=null) System.setProperty("http.proxyPort", props.getProperty("http.proxyPort"));
		if (props.getProperty("http.nonProxyHosts")!=null) System.setProperty("http.nonProxyHosts", props.getProperty("http.nonProxyHosts"));
		if (props.getProperty("http.proxyUser")!=null) System.setProperty("http.proxyUser", props.getProperty("http.proxyUser"));
		if (props.getProperty("http.proxyPassword")!=null) System.setProperty("http.proxyPassword", props.getProperty("http.proxyPassword"));
		
		setUpProxyAuthenticator();
	}
	
	private void setUpProxyAuthenticator() {
		if (System.getProperty("http.proxyUser")!=null && System.getProperty("http.proxyPassword")!=null) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					String password=System.getProperty("http.proxyPassword");
					String username=System.getProperty("http.proxyUser");
					return new PasswordAuthentication(username,password.toCharArray());
				}
			});
		}
	}
}
