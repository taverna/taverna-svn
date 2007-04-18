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
 * Filename           $RCSfile: ProxyConfigFactory.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-04-18 16:09:53 $
 *               by   $Author: sowen70 $
 * Created on 5 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import uk.org.mygrid.dataproxy.configuration.impl.XMLProxyConfig;
import uk.org.mygrid.dataproxy.web.ServerInfo;

/**
 * Factory singleton class that provides access to the ProxyConfig server configuration object. 
 * @author Stuart Owen
 *
 */

public class ProxyConfigFactory {	
	private static ProxyConfig instance;
	private static Logger logger = Logger.getLogger(ProxyConfigFactory.class);	
	
	/**
	 * 
	 * @return a singleton instance of the configuration
	 */
	public static ProxyConfig getInstance() {
		if (instance==null) {
			try {								
				SAXReader reader = new SAXReader();
				InputStream stream = getConfigInputStream();
				Element element = reader.read(stream).getRootElement();
				instance=new XMLProxyConfig(element);
			}
			catch(Exception e) {
				logger.error("Exception reading the XML configuration file",e);
			}
		}
		return instance;
	}
		
	private static InputStream getConfigInputStream() {
		String configFileLocation = ServerInfo.getConfigFileLocation();		
		File file = null;
		if (configFileLocation!=null ) file = new File(configFileLocation);
		if (file!=null && file.exists()) {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return ProxyConfigFactory.class.getResourceAsStream("/config.xml");
			}
		}
		else {
			return ProxyConfigFactory.class.getResourceAsStream("/config.xml");
		}
	}
	
	/**
	 * Generates a unique ID for a WSDL. Its based upon a UUID and check is performed that
	 * a corresponding ID doesn't already exist.
	 * @return
	 */
	public static String getUniqueWSDLID() {
		String wsdlID;
		while (true) {
			String uuid=UUID.randomUUID().toString();
			wsdlID=uuid.split("-")[0];
			if (getInstance().getWSDLConfigForID(wsdlID)==null) {
				break;
			}			
		}
		return wsdlID;
	}
	
	/**
	 * Writes the String representation of the configuration to a file, the file location defined
	 * by the ServerInfo.getConfigFileLocation
	 * 
	 * @see uk.org.mygrid.dataproxy.web.ServerInfo#getConfigurationLocation
	 * @see uk.org.mygrid.dataproxy.configuration.ProxyConfig#toStringForm
	 * 
	 * @throws Exception
	 * 
	 */
	public static void writeConfig() throws Exception {
		ProxyConfig config = getInstance();
		String textConfig = config.toStringForm();
		Writer writer = new FileWriter(ServerInfo.getConfigFileLocation());
		writer.write(textConfig);
		writer.close();
	}	
}
