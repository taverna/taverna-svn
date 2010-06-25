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
package net.sf.taverna.raven.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 *         The LauncherHttpProxyConfiguration handles the configuration of HTTP
 *         proxy when Taverna is launched and is also used by the
 *         HttpProxyConfiguration to dynamically change the proxy
 * 
 */
public class LauncherHttpProxyConfiguration {

	/**
	 * The acceptable values for which proxy values to use
	 */
	public static String USE_SYSTEM_PROPERTIES_OPTION = "useSystemProperties";
	public static String USE_NO_PROXY_OPTION = "useNoProxy";
	public static String USE_SPECIFIED_VALUES_OPTION = "useSpecifiedValues";

	/**
	 * The key within the Properties where the value will indicate which set of
	 * proxy values to use
	 */
	public static String PROXY_USE_OPTION = "proxyUseOption";

	/**
	 * The keys within the Properties for the ad hoc Taverna proxy settings
	 */
	public static String TAVERNA_PROXY_HOST = "tavernaProxyHost";
	public static String TAVERNA_PROXY_PORT = "tavernaProxyPort";
	public static String TAVERNA_PROXY_USER = "tavernaProxyUser";
	public static String TAVERNA_PROXY_PASSWORD = "tavernaProxyPassword";
	public static String TAVERNA_NON_PROXY_HOSTS = "tavernaNonProxyHosts";

	/**
	 * The keys within the Properties for the System proxy settings
	 */
	public static String SYSTEM_PROXY_HOST = "systemProxyHost";
	public static String SYSTEM_PROXY_PORT = "systemProxyPort";
	public static String SYSTEM_PROXY_USER = "systemProxyUser";
	public static String SYSTEM_PROXY_PASSWORD = "systemProxyPassword";
	public static String SYSTEM_NON_PROXY_HOSTS = "systemNonProxyHosts";

	/**
	 * The keys within the System Properties that are used for specifying HTTP
	 * proxy information
	 */
	public static String PROXY_HOST = "http.proxyHost";
	public static String PROXY_PORT = "http.proxyPort";
	public static String PROXY_USER = "http.proxyUser";
	public static String PROXY_PASSWORD = "http.proxyPassword";
	public static String NON_PROXY_HOSTS = "http.nonProxyHosts";

	/**
	 * The singleton instance. In theory, more than one instance could be used
	 */
	private static LauncherHttpProxyConfiguration instance = null;

	/**
	 * A Properties that holds the original System settings for HTTP proxy. They
	 * need to be copied as they are overwritten if something other than those
	 * System settings are used.
	 */
	private Properties originalSystemSettings;

	/**
	 * Properties into which the configuration properties are initially read
	 */
	private Properties configurationProps;

	private static Logger logger = Logger
			.getLogger(LauncherHttpProxyConfiguration.class);

	/**
	 * Read the original System settings. Read the configuration file and set
	 * the proxy settings accordingly.
	 */
	private LauncherHttpProxyConfiguration() {
		super();

		rememberOriginalSystemSettings();
		readConfigurationFile();
	}

	/**
	 * Copy the System properties. All the properties are copied, although only
	 * the HTTP proxy ones are relevant.
	 */
	private void rememberOriginalSystemSettings() {
		originalSystemSettings = new Properties();
		originalSystemSettings.putAll(System.getProperties());
	}

	/**
	 * Return the original System property value for the specified key. Null of
	 * no such property existed.
	 * 
	 * @param key
	 * @return
	 */
	public String getOriginalSystemSetting(String key) {
		return originalSystemSettings.getProperty(key);
	}

	/**
	 * Attempt to read the configuration file containing Taverna's HTTP proxy
	 * settings from the application's home directory. If the file does not
	 * exist then assume use of the System proxy settings.
	 * 
	 * Change the proxy settings.
	 */
	private void readConfigurationFile() {
		configurationProps = new Properties();
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();
		File configDir = new File(home, "conf");
		if (!configDir.exists()) {
			configDir.mkdir();
		}
		File configFile = new File(configDir, getName() + "-" + getUUID()
				+ ".config");
		if (configFile.exists()) {
			try {
				configurationProps.load(new FileInputStream(configFile));
				logger.info("Read from " + configFile.getPath());
			} catch (FileNotFoundException e) {
				logger.info("Failed to read from " + configFile.getPath());
			} catch (IOException e) {
				logger.info("Error reading from " + configFile.getPath());
			}
		} else {
			configurationProps.setProperty(PROXY_USE_OPTION,
					USE_SYSTEM_PROPERTIES_OPTION);
			logger.info(configFile.getPath() + " does not exist");
		}
		changeProxySettings(configurationProps);
	}

	/**
	 * Change the System Proxy settings according to the property values
	 * specified.
	 * 
	 * @param props
	 */
	public void changeProxySettings(Properties props) {
		String option = props.getProperty(PROXY_USE_OPTION);
		if (option.equals(USE_SYSTEM_PROPERTIES_OPTION)) {
			changeSystemProperty(PROXY_HOST,
					getOriginalSystemSetting(PROXY_HOST));
			changeSystemProperty(PROXY_PORT,
					getOriginalSystemSetting(PROXY_PORT));
			changeSystemProperty(PROXY_USER,
					getOriginalSystemSetting(PROXY_USER));
			changeSystemProperty(PROXY_PASSWORD,
					getOriginalSystemSetting(PROXY_PASSWORD));
			changeSystemProperty(NON_PROXY_HOSTS,
					getOriginalSystemSetting(NON_PROXY_HOSTS));
		} else if (option.equals(USE_NO_PROXY_OPTION)) {
			changeSystemProperty(PROXY_HOST, null);
			changeSystemProperty(PROXY_PORT, null);
			changeSystemProperty(PROXY_USER, null);
			changeSystemProperty(PROXY_PASSWORD, null);
			changeSystemProperty(NON_PROXY_HOSTS, null);
		} else if (option.equals(USE_SPECIFIED_VALUES_OPTION)) {
			changeSystemProperty(PROXY_HOST, props
					.getProperty(TAVERNA_PROXY_HOST));
			changeSystemProperty(PROXY_PORT, props
					.getProperty(TAVERNA_PROXY_PORT));
			changeSystemProperty(PROXY_USER, props
					.getProperty(TAVERNA_PROXY_USER));
			changeSystemProperty(PROXY_PASSWORD, props
					.getProperty(TAVERNA_PROXY_PASSWORD));
			changeSystemProperty(NON_PROXY_HOSTS, props
					.getProperty(TAVERNA_NON_PROXY_HOSTS));
		}
		setUpProxyAuthenticator();
		logger.info(PROXY_HOST + " is " + System.getProperty(PROXY_HOST));
		logger.info(PROXY_PORT + " is " + System.getProperty(PROXY_PORT));
		logger.info(PROXY_USER + " is " + System.getProperty(PROXY_USER));
		logger.info(PROXY_PASSWORD + " is "
				+ System.getProperty(PROXY_PASSWORD));
		logger.info(NON_PROXY_HOSTS + " is "
				+ System.getProperty(NON_PROXY_HOSTS));
	}

	/**
	 * If appropriate, set up a proxy authenticator for user name and password.
	 */
	private void setUpProxyAuthenticator() {
		if (System.getProperty(PROXY_USER) != null
				&& System.getProperty(PROXY_PASSWORD) != null) {
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					String password = System.getProperty(PROXY_PASSWORD);
					String username = System.getProperty(PROXY_USER);
					return new PasswordAuthentication(username, password
							.toCharArray());
				}
			});
		} else {
			Authenticator.setDefault(null);
		}
	}

	/**
	 * Change the specified System property to the given value. If the value is
	 * null then the property is cleared.ì
	 * 
	 * @param key
	 * @param value
	 */
	private void changeSystemProperty(String key, String value) {
		if ((value == null) || value.equals("")) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
		// AxisProperties.setProperty(key, (value == null ? "" : value));
	}

	/**
	 * @return
	 */
	public static String getName() {
		return "HttpProxy";
	}

	/**
	 * Return the singleton instance of LauncherHttpProxyConfiguration
	 * 
	 * @return
	 */
	public static LauncherHttpProxyConfiguration getInstance() {
		if (instance == null) {
			instance = new LauncherHttpProxyConfiguration();
		}
		return instance;
	}

	/**
	 * @return
	 */
	public static String getUUID() {
		return "B307A902-F292-4D2F-B8E7-00CC983982B6";
	}
}
