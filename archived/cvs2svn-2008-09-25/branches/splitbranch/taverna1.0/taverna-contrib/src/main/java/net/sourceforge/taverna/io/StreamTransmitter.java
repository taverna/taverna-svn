package net.sourceforge.taverna.io;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * This interface describes the methods needed by a Stream Transmitter. This
 * type of class can transmit values to an endpoint (such as a servlet or JSP
 * page) and process the results via a StreamProcessor.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public interface StreamTransmitter {
	/**
	 * This method takes a reflectable object and transmits it to a service
	 * 
	 * @param map
	 *            A map object (either HashTable or HashMap) containing the
	 *            parameter names(keys) and parameter values to be transmitted.
	 */
	public Map transmit(Map map, StreamProcessor streamProcessor) throws TransmitterException;

	/**
	 * This method is used to set the service URL.
	 * 
	 * @param serviceName
	 *            The name of the service for the url.
	 */
	public void setServiceName(String serviceName);

	/**
	 * This method sets the host for the url.
	 * 
	 * @param host
	 */
	public void setHost(String host);

	/**
	 * This method sets the port for the url.
	 * 
	 * @param port
	 *            The port used by the service.
	 */
	public void setPort(int port);

	/**
	 * This method sets the context for the url.
	 * 
	 * @param context
	 *            The following portion of the url: http://host:port/context
	 */
	public void setContext(String context);

	/**
	 * This method sets the userName and password used to transmit data to the
	 * service.
	 * 
	 * @param userName
	 *            The username used for authentication.
	 * @param password
	 *            The password used for authentication.
	 */
	public void setAuthentication(String userName, String password);

	/**
	 * This method sets the mime-header.
	 * 
	 * @param name
	 *            Name of the header parameter.
	 * @param value
	 *            The value of the header parameter.
	 */
	public void setMimeHeader(String name, String value);

	/**
	 * This method sets the URL.
	 * 
	 * @param url
	 */
	public void setURL(String url) throws MalformedURLException;
}
