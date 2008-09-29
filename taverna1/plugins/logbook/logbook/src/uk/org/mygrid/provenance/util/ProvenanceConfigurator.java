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
 * Filename           $RCSfile: ProvenanceConfigurator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:22 $
 *               by   $Author: stain $
 * Created on 02-May-2006
 *****************************************************************/
package uk.org.mygrid.provenance.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.LogLevel;

/**
 * Utility class for configuring provenance properties.
 * 
 * @author dturi
 * @version $Id: ProvenanceConfigurator.java,v 1.10 2006/10/05 07:15:22 turid
 *          Exp $
 */
public class ProvenanceConfigurator {

	public static final String DB_DRIVER_KEY = "mygrid.kave.jdbc.driver";

	public static final String DB_PASSWORD_KEY = "mygrid.kave.jdbc.password";

	public static final String DB_USER_KEY = "mygrid.kave.jdbc.user";

	public static final String DB_URL_KEY = "mygrid.kave.jdbc.url";

	public static final String PROVENANCE_STORE_HOME = System
			.getProperty("user.home")
			+ "/.taverna/provenance";

	public static final String LOGBOOK_LEVEL = "mygrid.logbook.loglevel";

	public static final String KAVE_TYPE_KEY = "mygrid.kave.type";

	public static final String JENA = "jena/hypersonic";

	public static final String JENA_MYSQL = "jena/mysql";

	public static final String DEFAULT_KAVE_TYPE = JENA_MYSQL;

	public static final String BOCA = "boca";

	public static final String BOCA_DERBY = "BocaDerby";

	public static final String SESAME = "sesame";

	public static final String DATASERVICE_TYPE_KEY = "mygrid.dataservice.type";

	public static final String MYSQL = "mysql";

	public static final String DERBY = "derby";

	public static final String HYPERSONIC = "hypersonic";

	public static final String DEFAULT_DATASERVICE_TYPE = MYSQL;

	public static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";

	public static final String HSQL_JDBC_DRIVER = "org.hsqldb.jdbcDriver";

	public static final String DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

	public static final String MISSING_PROPERTY_MESSAGE = "Cannot initialise service: missing property ";

	public static final String MYGRID_METADATASERVICE_JDBC_DRIVER = "mygrid.metadataservice.jdbc.driver";

	//
	// public static final String MYGRID_METADATASERVICE_JDBC_PASSWORD =
	// "mygrid.metadataservice.mysql.password";
	//
	// public static final String MYGRID_METADATASERVICE_JDBC_USER =
	// "mygrid.metadataservice.mysql.user";
	//
	// public static final String MYGRID_METADATASERVICE_JDBC_URL =
	// "mygrid.metadataservice.mysql.url";
	//
	public static final String MYGRID_DATASERVICE_JDBC_DRIVER = "mygrid.dataservice.jdbc.driver";

	//
	// public static final String MYGRID_DATASERVICE_JDBC_PASSWORD =
	// "mygrid.dataservice.jdbc.password";
	//
	// public static final String MYGRID_DATASERVICE_JDBC_USER =
	// "mygrid.dataservice.jdbc.user";
	//
	// public static final String MYGRID_DATASERVICE_JDBC_URL =
	// "mygrid.dataservice.jdbc.url";

	public static final String PROVENANCE_BUNDLE_NAME = "provenance";

	public static final String MYGRID_DATASERVICE_MYSQL = "mygrid.dataservice.mysql.";

	public static final String MYSQL_CONNECTION_URL = MYGRID_DATASERVICE_MYSQL
			+ "url";

	public static final String MYSQL_USER = MYGRID_DATASERVICE_MYSQL + "user";

	public static final String MYSQL_PASSWORD = MYGRID_DATASERVICE_MYSQL
			+ "password";

	public static final String MYGRID_METADATASERVICE_MYSQL = "mygrid.metadataservice.mysql.";

	public static final String METADATA_MYSQL_CONNECTION_URL = MYGRID_METADATASERVICE_MYSQL
			+ "url";

	public static final String METADATA_MYSQL_USER = MYGRID_METADATASERVICE_MYSQL
			+ "user";

	public static final String METADATA_MYSQL_PASSWORD = MYGRID_METADATASERVICE_MYSQL
			+ "password";

	public static final String MYGRID_DATASERVICE_DERBY = "mygrid.dataservice.derby.";

	public static final String DERBY_CONNECTION_URL = MYGRID_DATASERVICE_DERBY
			+ "url";

	public static final String DERBY_USER = MYGRID_DATASERVICE_DERBY + "user";

	public static final String DERBY_PASSWORD = MYGRID_DATASERVICE_DERBY
			+ "password";

	public static final String MYGRID_DATASERVICE_HSQL = "mygrid.dataservice.hsql.";

	public static final String MYGRID_METADATASERVICE_HSQL = "mygrid.metadataservice.hsql.";

	public static final String HSQL_CONNECTION_URL = MYGRID_DATASERVICE_HSQL
			+ "url";

	public static final String METADATA_HSQL_CONNECTION_URL = MYGRID_METADATASERVICE_HSQL
			+ "url";

	public static final String HSQL_USER = MYGRID_DATASERVICE_HSQL + "user";

	public static final String HSQL_PASSWORD = MYGRID_DATASERVICE_HSQL
			+ "password";

	public static final String METADATA_HSQL_USER = MYGRID_METADATASERVICE_HSQL
			+ "user";

	public static final String METADATA_HSQL_PASSWORD = MYGRID_METADATASERVICE_HSQL
			+ "password";

	public static final String EXPERIMENTER_KEY = "mygrid.usercontext.experimenter";

	public static final String DEFAULT_EXPERIMENTER = "http://www.someplace/someuser";

	public static final String ORGANISATION_KEY = "mygrid.usercontext.organisation";

	public static final String DEFAULT_ORGANIZATION = "http://www.someplace/somelab";

	public static Logger logger = Logger
			.getLogger(ProvenanceConfigurator.class);

	private static Properties provenanceProperties;

	static public Properties getMetadataStoreConfiguration()
			throws LogBookConfigurationNotFoundException {
		return getConfiguration();
	}

	static public Properties getDataStoreConfiguration()
			throws LogBookConfigurationNotFoundException {
		return getConfiguration();
	}

	/**
	 * Retrieves the properties in the file with name <code>fileName</code> in
	 * the default configuration directory.
	 * 
	 * @param fileName
	 *            the (local) name of the configuration file.
	 * @return {@link Properties}.
	 * @throws LogBookConfigurationNotFoundException
	 */
	static public Properties getConfiguration(String fileName)
			throws LogBookConfigurationNotFoundException {
		provenanceProperties = new Properties();
		File configurationFile = getConfigurationFile(fileName);
		if (!configurationFile.exists())
			throw new LogBookConfigurationNotFoundException(configurationFile);
		try {
			InputStream inStream = new FileInputStream(configurationFile);
			provenanceProperties.load(inStream);
			inStream.close();
		} catch (FileNotFoundException e) {
			logger.error("Configuration file " + configurationFile
					+ " not found", e);
		} catch (IOException e) {
			logger.error("Error reading from file " + configurationFile, e);
		}
		return provenanceProperties;
	}

	public static Properties createDefaultConfiguration() {
		Properties defaultConfiguration = new Properties();
		defaultConfiguration.setProperty(LOGBOOK_LEVEL, LogLevel.DEFAULT_LEVEL);
		defaultConfiguration.setProperty(METADATA_MYSQL_CONNECTION_URL,
				"jdbc:mysql://localhost/logbook_metadata");
		defaultConfiguration.setProperty(MYSQL_CONNECTION_URL,
				"jdbc:mysql://localhost/logbook_data");
		defaultConfiguration.setProperty(METADATA_MYSQL_USER, "root");
		defaultConfiguration.setProperty(MYSQL_USER, "root");
		defaultConfiguration.setProperty(METADATA_MYSQL_PASSWORD, "");
		defaultConfiguration.setProperty(MYSQL_PASSWORD, "");
		return defaultConfiguration;
	}

	static public Properties getConfigurationFromBundle(String bundleName) {
		if (provenanceProperties != null)
			return provenanceProperties;
		provenanceProperties = new Properties();// System.getProperties();
		try {
			ResourceBundle rb = ResourceBundle.getBundle(bundleName);
			Enumeration keys = rb.getKeys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = (String) rb.getString(key);
				provenanceProperties.put(key, value);
			}
		} catch (MissingResourceException e) {
			// logger.warn(e.getMessage() + "\nUsing system properties");
			logger.error("No properties found for provenance", e);
		}
		return provenanceProperties;
	}

	/**
	 * 
	 * @return
	 * @throws LogBookConfigurationNotFoundException 
	 */
	static public Properties getConfiguration() throws LogBookConfigurationNotFoundException {
		return getConfiguration(PROVENANCE_BUNDLE_NAME);
	}

	public static File getConfigurationFile() {
		return getConfigurationFile(ProvenanceConfigurator.PROVENANCE_BUNDLE_NAME);
	}

	public static File getConfigurationFile(String bundleName) {
		File confDir = MyGridConfiguration.getUserDir("conf");
		if (!confDir.exists())
			confDir.mkdir();
		File configurationFile = new File(confDir, bundleName + ".properties");
		return configurationFile;
	}
	
	public static File getExistingConfigurationFile() throws LogBookConfigurationNotFoundException {
		return getExistingConfigurationFile(ProvenanceConfigurator.PROVENANCE_BUNDLE_NAME);
	}

	public static File getExistingConfigurationFile(String bundleName)
			throws LogBookConfigurationNotFoundException {
		File configurationFile = getConfigurationFile(bundleName);
		if (!configurationFile.exists())
			throw new LogBookConfigurationNotFoundException(configurationFile
					.toString());
		return configurationFile;
	}

	static public void missingPropertyMessage(String missingProperty)
			throws PropertyMissingException {
		System.err.println(MISSING_PROPERTY_MESSAGE + missingProperty);
		logger.error(MISSING_PROPERTY_MESSAGE + missingProperty);
		throw new PropertyMissingException();
	}

}
