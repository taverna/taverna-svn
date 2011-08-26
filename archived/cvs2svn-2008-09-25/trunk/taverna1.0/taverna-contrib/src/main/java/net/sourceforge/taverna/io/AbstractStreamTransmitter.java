package net.sourceforge.taverna.io;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;

/**
 * This class provides the base class for all Stream-based transmitters. These
 * transmitters transmit an object or a reflector containing a set of parameters
 * to an endpoint (such as a servlet or JSP page) and process the result stream
 * using a StreamProcessor.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public abstract class AbstractStreamTransmitter implements StreamTransmitter {
	
	private static Logger logger = Logger
			.getLogger(AbstractStreamTransmitter.class);
	
	protected String serviceName = null;

	protected HashMap mimeHeaders = null;

	protected String host = null;

	protected int port = 80;

	protected String context = null;

	protected String userName = null;

	protected String password = null;

	protected URL url;

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#transmit(java.util.HashMap,
	 *      com.kymerix.reflector.StreamProcessor)
	 */
	public abstract Map transmit(Map map, StreamProcessor streamProcessor) throws TransmitterException;

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#setServiceName(java.lang.String)
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;

	}

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#setHost(java.lang.String)
	 */
	public void setHost(String host) {
		this.host = host;

	}

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#setPort(int)
	 */
	public void setPort(int port) {
		this.port = port;

	}

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#setContext(java.lang.String)
	 */
	public void setContext(String context) {
		this.context = context;

	}

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#setAuthentication(java.lang.String,
	 *      java.lang.String)
	 */
	public void setAuthentication(String userName, String password) {
		this.userName = userName;
		this.password = password;

	}

	/**
	 * @see com.kymerix.reflector.StreamTransmitter#setMimeHeader(java.lang.String,
	 *      java.lang.String)
	 */
	public void setMimeHeader(String name, String value) {
		mimeHeaders.put(name, value);
	}

	public void setURL(String urlStr) throws MalformedURLException {
		this.url = new URL(urlStr);
		this.host = url.getHost();
		this.context = url.getPath();
		this.port = url.getPort();

	}

	/**
	 * This method creates a transmitter exception.
	 * 
	 * @param e
	 *            The original exception.
	 * @param repostUrl
	 *            The URL to be used to repost the response.
	 * @return
	 */
	protected TransmitterException createTxException(Exception e) {
		TransmitterException ex = new TransmitterException(e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));

		StringBuffer sb = new StringBuffer();
		sb.append(sw.toString());
		ex.setDetailMsg(sb.toString());

		return ex;
	}
	
	protected void setProxy(HttpClient client) {
		String host = System.getProperty("http.proxyHost");
		String port = System.getProperty("http.proxyPort");
		String user = System.getProperty("http.proxyUser");
		String password = System.getProperty("http.proxyPassword");

		if (host != null && port != null) {
			try {
				int portInteger = Integer.parseInt(port);
				client.getHostConfiguration().setProxy(host, portInteger);
				if (user != null && password != null) {
					client.getState().setProxyCredentials(
							new AuthScope(host, portInteger),
							new UsernamePasswordCredentials(user, password));
				}
			} catch (NumberFormatException e) {
				logger.error("Proxy port not an integer", e);
			}
		}
	}

}
