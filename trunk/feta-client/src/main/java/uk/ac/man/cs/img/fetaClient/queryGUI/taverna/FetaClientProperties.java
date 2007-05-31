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
 ***/
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.sf.taverna.tools.AbstractConfiguration;
import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;

public class FetaClientProperties extends AbstractConfiguration {
	
	private static Logger logger = Logger.getLogger(FetaClientProperties.class);

	private static FetaClientProperties instance = null;
	
	private FetaClientProperties() {
		super();
	}
	
	public static FetaClientProperties getInstance() {
		if (instance==null) instance=new FetaClientProperties();
		return instance;
	}
	
	private final static String FETACLIENT_PROPERTIES = "fetaClient.properties";

	private InputStream getResourceInputStream() {
		InputStream stream = FetaClientProperties.class.getResourceAsStream("/"+FETACLIENT_PROPERTIES);
		return stream;
	}
	/**
	 * Copies the bundled properties from the jar file to the users $taverna.home/conf/fetaClient.properties.
	 */
	private void writeFromResource() throws IOException {
		InputStream inStream = getResourceInputStream();
		
		File userConf = MyGridConfiguration.getUserDir("conf");
		File propertiesOutput = new File(userConf,FETACLIENT_PROPERTIES);
		OutputStream outStream = new FileOutputStream(propertiesOutput);
		int len = 0;
		byte [] buffer = new byte[255];
		
		while ((len = inStream.read(buffer))!=-1) {
			outStream.write(buffer,0,len);
		}
		
		outStream.close();
		inStream.close();
	}
	
	public static boolean isAnnotator() throws IOException {
		return getInstance().getProperties().getProperty("annotator") != null;
	}

	public static String getPropertyValue(String propertyName,
			String defaultValue) throws IOException {
		return getInstance().getProperties().getProperty(propertyName, defaultValue);
	}

	public static String getPropertyValue(String propertyName)
			throws IOException {
		return getInstance().getProperties().getProperty(propertyName);
	}

	@Override
	protected String getConfigurationFilename() {
		return FETACLIENT_PROPERTIES;
	}

	@Override
	protected InputStream getInputStream() {
		InputStream result = super.getInputStream();
		if (result==null) {
			try {
				writeFromResource();
				result=super.getInputStream();
			}
			catch(IOException e) {
				logger.error("There was an error writing out the bundled properties to the users home. Will use the bundled properties but they are not user editable.");
			}
		}
		if (result==null) result = getResourceInputStream();
		
		return result;
	}
} 
