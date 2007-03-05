/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-20 14:51:32 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.io.Serializable;

/**
 * Class containing the connection settings
 * 
 * @author Ingo Wassink
 * 
 */
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
	 */
	public RshellConnectionSettings(String host, int port, String username,
			String Password) {
		setHost(host);
		setPort(port);
		setUsername(username);
		setPassword(password);
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

}
