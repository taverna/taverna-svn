/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static net.sf.taverna.t2.component.registry.ClientVersion.VERSION;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.log4j.Logger.getLogger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 * @author Donal Fellows (mostly by stripping things out)
 */
public class MyExperimentClient {
	private static final String PLUGIN_USER_AGENT = "Taverna2-Component-plugin/"
			+ VERSION + " Java/" + getProperty("java.version");
	private static final Logger logger = getLogger(MyExperimentClient.class);
	private static final int MESSAGE_TRIM_LENGTH = 512;
	private static final String WHOAMI = "/whoami.xml";

	// SETTINGS
	/** myExperiment base URL to use */
	private final String baseURL;
	// authentication settings (and the current user)
	private String authString = null;

	public MyExperimentClient(String baseURL) {
		new Properties();
		this.baseURL = baseURL;
	}

	// getter for the current status
	private boolean isLoggedIn() {
		return authString != null;
	}

	private static CredentialManager cm() throws CMException {
		return CredentialManager.getInstance();
	}

	private static String getCredentials(String urlString) {
		try {
			UsernamePassword userAndPass = cm()
					.getUsernameAndPasswordForService(URI.create(urlString),
							true, null);
			// Check for user didn't log in...
			if (userAndPass == null)
				return null;
			return printBase64Binary((userAndPass.getUsername() + ":" + userAndPass
					.getPasswordAsString()).getBytes("UTF-8"));
		} catch (CMException e) {
			throw new RuntimeException("error in Taverna Credential Manager", e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("error in encoding", e);
		}
	}

	public boolean login() {
		// check if the stored credentials are valid
		ServerResponse response = null;
		try {
			String userPass = getCredentials(baseURL);
			if (userPass == null) {
				logger.info("UserPass is null for " + baseURL);
				return false;
			}

			// set the system to the "logged in" state from INI file properties
			authString = userPass;
			response = GET(baseURL + WHOAMI);
		} catch (Exception e) {
			logger.error("failed when verifying login credentials", e);
		}

		if (response == null || response.getCode() != HTTP_OK) {
			if (response != null) {
				logger.info("failed to log in: " + response.getError());
			} else
				logger.info("Unauthorized");
			try {
				clearCredentials();
			} catch (Exception e) {
				logger.error("failed to clear credentials", e);
			}
			authString = null;
			return false;
		}
		logger.debug("logged in to repository successfully");
		return true;
	}

	private void clearCredentials() throws CMException {
		@SuppressWarnings("deprecation")
		List<String> toDelete = cm()
				.getServiceURLsforAllUsernameAndPasswordPairs();
		for (String uri : toDelete)
			if (uri.startsWith(baseURL))
				cm().deleteUsernameAndPasswordForService(uri);
		// cm.resetAuthCache();
	}

	/**
	 * Simulates a "logout" action. Logging in and out in the plugin is only an
	 * abstraction created for user convenience; it is a purely virtual concept,
	 * because the myExperiment API is completely stateless - hence, logging out
	 * simply consists of "forgetting" the authentication details and updating
	 * the state.
	 */
	public void logout() throws Exception {
		authString = null;
	}

	private HttpURLConnection connect(String strURL)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(strURL)
				.openConnection();
		conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
		if (authString != null)
			conn.setRequestProperty("Authorization", "Basic " + authString);
		return conn;
	}

	/**
	 * Generic method to execute GET requests to myExperiment server.
	 * 
	 * @param url
	 *            The URL on myExperiment to issue GET request to.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse GET(String url) throws Exception {
		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(url);
		if (!isLoggedIn())
			logger.warn("not logged in");

		// check server's response
		return receiveServerResponse(conn, url, true, false);
	}

	/**
	 * Generic method to execute GET requests to myExperiment server.
	 * 
	 * @param url
	 *            The URL on myExperiment to issue GET request to.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse HEAD(String url) throws Exception {
		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(url);
		conn.setRequestMethod("HEAD");
		if (!isLoggedIn())
			logger.warn("not logged in");

		// check server's response
		return receiveServerResponse(conn, url, false, true);
	}

	/**
	 * Generic method to execute GET requests to myExperiment server.
	 * 
	 * @param url
	 *            The URL on myExperiment to POST to.
	 * @param xmlDataBody
	 *            Body of the XML data to be POSTed to strURL.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse POST(String url, String xmlDataBody) throws Exception {
		// POSTing to myExperiment is only allowed for authorised users
		if (!isLoggedIn())
			return null;

		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(url);

		// "tune" the connection
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/xml");

		// prepare and PUT/POST XML data
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(xmlDataBody);
		out.close();

		// check server's response
		return receiveServerResponse(conn, url, false, false);
	}

	/**
	 * Generic method to execute DELETE requests to myExperiment server. This is
	 * only to be called when a user is logged in.
	 * 
	 * @param url
	 *            The URL on myExperiment to direct DELETE request to.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse DELETE(String url) throws Exception {
		if (!isLoggedIn())
			return null;

		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(url);
		conn.setRequestMethod("DELETE");

		// check server's response
		return receiveServerResponse(conn, url, true, false);
	}

	private static Document getDocumentFromStream(InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc;
		InputStream is = new BufferedInputStream(inputStream);
		if (!logger.isDebugEnabled()) {
			doc = db.parse(is);
			is.close();
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copy(is, baos);
			is.close();
			String response = baos.toString("UTF-8");
			logger.info("response message follows\n"
					+ response.substring(0,
							min(MESSAGE_TRIM_LENGTH, response.length())));
			doc = db.parse(new ByteArrayInputStream(baos.toByteArray()));
		}
		return doc;
	}

	/**
	 * A common method for retrieving myExperiment server's response for both
	 * GET and POST requests.
	 * 
	 * @param conn
	 *            Instance of the established URL connection to poll for
	 *            server's response.
	 * @param url
	 *            The URL on myExperiment with which the connection is
	 *            established.
	 * @param bIsGetRequest
	 *            Flag for identifying type of the request. True when the
	 *            current connection executes GET request; false when it
	 *            executes a POST request.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 */
	private ServerResponse receiveServerResponse(HttpURLConnection conn,
			String url, boolean isGETrequest, boolean isHEADrequest)
			throws Exception {
		switch (conn.getResponseCode()) {
		case HTTP_OK:
			/*
			 * data retrieval was successful - parse the response XML and return
			 * it along with response code
			 */
			if (isHEADrequest)
				return new ServerResponse(conn.getResponseCode(), null);
			return new ServerResponse(conn.getResponseCode(),
					getDocumentFromStream(conn.getInputStream()));

		case HTTP_BAD_REQUEST:
		case HTTP_FORBIDDEN:
			/*
			 * this was a bad XML request - need full XML response to retrieve
			 * the error message from it; Java throws IOException if
			 * getInputStream() is used when non HTTP_OK response code was
			 * received - hence can use getErrorStream() straight away to fetch
			 * the error document
			 */
			return new ServerResponse(conn.getResponseCode(),
					getDocumentFromStream(conn.getErrorStream()));

		case HTTP_UNAUTHORIZED:
			// this content is not authorised for current user
			logger.warn("non-authorised request to " + url + "\n"
					+ IOUtils.toString(conn.getErrorStream()));
			return new ServerResponse(conn.getResponseCode(), null);

		case HTTP_NOT_FOUND:
			if (isHEADrequest)
				return new ServerResponse(conn.getResponseCode(), null);
			throw new FileNotFoundException("no such resource: " + url);
		default:
			// unexpected response code - raise an exception
			throw new IOException(format(
					"Received unexpected HTTP response code (%d) while %s %s",
					conn.getResponseCode(), (isGETrequest ? "fetching data at"
							: "posting data to"), url));
		}
	}

	public ServerResponse PUT(String url, String xmlDataBody) throws Exception {
		if (!isLoggedIn())
			return null;

		HttpURLConnection conn = connect(url);
		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/xml");

		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(xmlDataBody);
		out.close();

		return receiveServerResponse(conn, url, false, false);
	}

	public class ServerResponse {
		private final int responseCode;
		private final Document responseBody;

		ServerResponse(int responseCode, Document responseBody) {
			this.responseCode = responseCode;
			this.responseBody = responseBody;
		}

		public int getCode() {
			return responseCode;
		}

		public boolean isFailure() {
			return responseCode >= HTTP_BAD_REQUEST;
		}

		public <T> T getResponse(JAXBContext context, Class<T> clazz)
				throws JAXBException {
			return context.createUnmarshaller()
					.unmarshal(responseBody.getDocumentElement(), clazz)
					.getValue();
		}

		/**
		 * Returns contents of the "reason" field of the error message.
		 */
		public String getError() {
			if (responseBody != null) {
				Node reasonElement = responseBody.getDocumentElement()
						.getElementsByTagName("reason").item(0);
				if (reasonElement != null) {
					String reason = reasonElement.getTextContent();
					if (!reason.isEmpty())
						return reason;
				}
			}
			return format("unknown reason (%d)", responseCode);
		}
	}
}
