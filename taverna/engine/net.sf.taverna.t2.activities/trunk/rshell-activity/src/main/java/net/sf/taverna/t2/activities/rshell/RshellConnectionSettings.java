/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */

package net.sf.taverna.t2.activities.rshell;

import java.io.Serializable;

import net.sf.taverna.t2.workflowmodel.processor.activity.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ConfigurationProperty;

/**
 * Class containing the connection settings
 * 
 */
@ConfigurationBean(uri = RshellActivity.URI + "/connection/configuration")
public class RshellConnectionSettings implements Serializable {
	public static final String DEFAULT_HOST = "localhost";

	public static final int DEFAULT_PORT = 6311;

	private static final boolean DEFAULT_KEEP_SESSION_ALIVE = false;

	private static final String DEFAULT_USERNAME = "";

	private static final String DEFAULT_PASSWORD = "";

	private static final long serialVersionUID = -8311679711262064315L;

	private String host = DEFAULT_HOST;

	private int port = DEFAULT_PORT;

	private String username = DEFAULT_USERNAME;

	private String password = DEFAULT_PASSWORD;

	private boolean keepSessionAlive = false;

	private boolean newRVersion = false;

	/**
	 * Constructor of the rshell connection settings
	 */
	public RshellConnectionSettings() {
	}

	/**
	 * Constructor of the rshell connection settings
	 * 
	 * @param host
	 *            the host name
	 * @param port
	 *            the port number
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @param newRVersion
	 *            what R version the connection is for 2.4-2.5 or 2.6+
	 */
	public RshellConnectionSettings(String host, int port, String username,
			String Password, boolean newRVersion) {
		setHost(host);
		setPort(port);
		setUsername(username);
		setPassword(password);
		setNewRVersion(newRVersion);
	}

	/**
	 * Method for getting the host
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Method for setting the host
	 * 
	 * @param host
	 *            the new host value
	 */
	@ConfigurationProperty(name = "host", label = "Host", description = "The host name for the R server", required = false)
	public void setHost(String host) {
		this.host = (host == null) ? DEFAULT_HOST : host;
	}

	/**
	 * Method for getting the port number
	 * 
	 * @return the port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Method for setting the port value
	 * 
	 * @param port
	 *            the new port value
	 */
	@ConfigurationProperty(name = "port", label = "Port", description = "The port number for the R server", required = false)
	public void setPort(int port) {
		
			this.port = (port < 0 || port > 65535) ? DEFAULT_PORT : port;
		
	}

	/**
	 * Method for setting the port value
	 * 
	 * @param port
	 *            the new port value
	 */
	public void setPort(String port) {
		this.port = (port == null) ? DEFAULT_PORT : Integer.parseInt(port);
	}

	/**
	 * Method for getting the username
	 * 
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Method for setting the username
	 * 
	 * @param username
	 *            the new username
	 */
	@ConfigurationProperty(name = "username", label = "Username", description = "The username to connect with", required = false)
	public void setUsername(String username) {
		this.username = (username == null) ? DEFAULT_USERNAME : username;
	}

	/**
	 * Method for getting the password
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Method for setting the password
	 * 
	 * @param password
	 *            the new password
	 */
	@ConfigurationProperty(name = "password", label = "Password", description = "The password to connect with", required = false)
	public void setPassword(String password) {
		this.password = (password == null) ? DEFAULT_PASSWORD : password;
	}

	/**
	 * Method for getting the keep session alive setting
	 * 
	 * @return the value for keep session alive
	 */
	public boolean isKeepSessionAlive() {
		return keepSessionAlive;
	}

	/**
	 * Method for setting the keep session alive
	 * 
	 * @param keepSessionAlive
	 *            the new value
	 */
	@ConfigurationProperty(name = "keepSessionAlive", label = "Keep Session Alive", description = "If the session should be kept alive", required = false)
	public void setKeepSessionAlive(boolean keepSessionAlive) {
		this.keepSessionAlive = keepSessionAlive;
	}

	/**
	 * Method for setting the keep session alive
	 * 
	 * @param keepSessionAlive
	 *            the new value
	 */
	public void setKeepSessionAlive(String keepSessionAlive) {
		this.keepSessionAlive = (keepSessionAlive == null) ? DEFAULT_KEEP_SESSION_ALIVE
				: Boolean.parseBoolean(keepSessionAlive);
	}

	/**
	 * Method for overriding the equals method Two connections settings are
	 * equal, if all their settings are equal (host, port, username, password
	 * and keep session alive)
	 * 
	 * @param anObject
	 *            the object to compare to
	 * @return true if the connections are equal
	 */
	public boolean equals(Object anObject) {
		if (!(anObject instanceof RshellConnectionSettings)) {
			return false;
		} else {
			RshellConnectionSettings aConnection = (RshellConnectionSettings) anObject;
			return host.equals(aConnection.host) && port == aConnection.port
					&& username.equals(aConnection.username)
					&& password.equals(aConnection.password)
					&& keepSessionAlive == aConnection.keepSessionAlive;
		}
	}

	/**
	 * Method overriding the hashcode function
	 * 
	 * @return the hash code
	 */
	public int hashCode() {
		StringBuffer stringRepresentation = new StringBuffer();
		stringRepresentation.append(host);
		stringRepresentation.append(port);
		stringRepresentation.append(username);
		stringRepresentation.append(password);
		stringRepresentation.append(keepSessionAlive);

		return stringRepresentation.toString().hashCode();
	}

	@ConfigurationProperty(name = "newRVersion", label = "Is New R Version", description = "If the R version is 2.4-2.5 or 2.6+", required = false)
	public void setNewRVersion(boolean newRVersion) {
		this.newRVersion = newRVersion;
	}

	public boolean isNewRVersion() {
		return newRVersion;
	}

	public static RshellConnectionSettings defaultSettings() {
		RshellConnectionSettings result = new RshellConnectionSettings();
		result.setHost(DEFAULT_HOST);
		result.setPort(DEFAULT_PORT);
		result.setUsername(DEFAULT_USERNAME);
		result.setPassword(DEFAULT_PASSWORD);
		result.setKeepSessionAlive(DEFAULT_KEEP_SESSION_ALIVE);
		return result;
	}
}
