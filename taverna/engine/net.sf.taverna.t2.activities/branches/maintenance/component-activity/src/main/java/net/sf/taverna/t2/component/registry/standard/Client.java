package net.sf.taverna.t2.component.registry.standard;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.URLEncoder.encode;
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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.component.api.RegistryException;
import net.sf.taverna.t2.component.registry.standard.Client.MyExperimentConnector.ServerResponse;
import net.sf.taverna.t2.component.registry.standard.annotations.Unused;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

class Client {
	private static final String API_VERIFICATION_RESOURCE = "/component-profiles.xml";
	private static final String WHOAMI = "/whoami.xml";
	private static final String PLUGIN_USER_AGENT = "Taverna2-Component-plugin/"
			+ VERSION + " Java/" + getProperty("java.version");
	private static final int MESSAGE_TRIM_LENGTH = 512;
	private static final Logger logger = getLogger(Client.class);
	private final MyExperimentConnector http;
	private final URL registryBase;
	private final JAXBContext jaxbContext;

	Client(JAXBContext context, URL repository) throws RegistryException {
		this.registryBase = repository;
		this.jaxbContext = context;
		this.http = new MyExperimentConnector();
		logger.info("instantiated client connection engine to " + repository);
	}

	public boolean verify() {
		try {
			String url = url(API_VERIFICATION_RESOURCE);
			logger.info("API verification: HEAD for " + url);
			return http.HEAD(url).getCode() == HTTP_OK;
		} catch (Exception e) {
			logger.info("failed to connect to " + registryBase, e);
			return false;
		}
	}

	private String url(String uri, String... arguments)
			throws MalformedURLException, UnsupportedEncodingException {
		StringBuilder uriBuilder = new StringBuilder(uri);
		for (String queryElement : arguments) {
			String[] bits = queryElement.split("=", 2);
			uriBuilder.append(uriBuilder.indexOf("?") < 0 ? "?" : "&")
					.append(bits[0]).append('=')
					.append(encode(bits[1], "UTF-8"));
		}
		return new URL(registryBase, uriBuilder.toString()).toString();
	}

	private Marshaller getMarshaller() throws JAXBException {
		return jaxbContext.createMarshaller();
	}

	/**
	 * Does an HTTP GET against the configured repository.
	 * 
	 * @param clazz
	 *            The JAXB-annotated class that the result is supposed to be
	 *            instantiated into.
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @return The deserialized response object.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public <T> T get(Class<T> clazz, String uri, String... query)
			throws RegistryException {
		try {
			int redirectCounter = 0;

			String url = url(uri, query);
			ServerResponse response;
			do {
				if (redirectCounter++ > 5)
					throw new RegistryException("too many redirects!");
				logger.info("GET of " + url);
				response = http.GET(url);
				if (response.isFailure())
					throw new RegistryException(
							"Unable to perform request (%d): %s",
							response.getCode(), response.getError());
			} while ((url = response.getLocation()) != null);
			return response.getResponse(clazz);

		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (JAXBException e) {
			throw new RegistryException("Problem when unmarshalling response",
					e);
		} catch (Exception e) {
			throw new RegistryException("Problem when sending request", e);
		}
	}

	/**
	 * Does an HTTP POST against the configured repository.
	 * 
	 * @param clazz
	 *            The JAXB-annotated class that the result is supposed to be
	 *            instantiated into.
	 * @param elem
	 *            The JAXB element to post to the resource.
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @return The deserialized response object.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public <T> T post(Class<T> clazz, JAXBElement<?> elem, String uri,
			String... query) throws RegistryException {
		try {

			String url = url(uri, query);
			logger.info("POST to " + url);
			StringWriter sw = new StringWriter();
			getMarshaller().marshal(elem, sw);
			if (logger.isDebugEnabled())
				logger.info("About to post XML document:\n" + sw);
			ServerResponse response = http.POST(url, sw.toString());
			if (response.isFailure())
				throw new RegistryException(
						"Unable to perform request (%d): %s",
						response.getCode(), response.getError());
			if (response.getLocation() != null)
				return get(clazz, response.getLocation());
			return response.getResponse(clazz);

		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			throw new RegistryException("Problem when sending request", e);
		}
	}

	/**
	 * Does an HTTP PUT against the configured repository.
	 * 
	 * @param clazz
	 *            The JAXB-annotated class that the result is supposed to be
	 *            instantiated into.
	 * @param elem
	 *            The JAXB element to post to the resource.
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @return The deserialized response object.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	@Unused
	public <T> T put(Class<T> clazz, JAXBElement<?> elem, String uri,
			String... query) throws RegistryException {
		try {

			String url = url(uri, query);
			logger.info("PUT to " + url);
			StringWriter sw = new StringWriter();
			getMarshaller().marshal(elem, sw);
			if (logger.isDebugEnabled())
				logger.info("About to put XML document:\n" + sw);
			ServerResponse response = http.PUT(url, sw.toString());
			if (response.isFailure())
				throw new RegistryException(
						"Unable to perform request (%d): %s",
						response.getCode(), response.getError());
			if (response.getLocation() != null)
				return get(clazz, response.getLocation());
			return response.getResponse(clazz);

		} catch (RegistryException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (JAXBException e) {
			throw new RegistryException("Problem when marshalling request", e);
		} catch (Exception e) {
			throw new RegistryException("Problem when sending request", e);
		}
	}

	/**
	 * Does an HTTP DELETE against the configured repository.
	 * 
	 * @param uri
	 *            The path part of the URI within the repository.
	 * @param query
	 *            The strings to put into the query part. Each should be in
	 *            <tt>key=value</tt> form.
	 * @throws RegistryException
	 *             If anything goes wrong.
	 */
	public void delete(String uri, String... query) throws RegistryException {
		ServerResponse response;
		try {

			String url = url(uri, query);
			logger.info("DELETE of " + url);
			response = http.DELETE(url);

		} catch (MalformedURLException e) {
			throw new RegistryException("Problem constructing resource URL", e);
		} catch (Exception e) {
			throw new RegistryException("Unable to perform request", e);
		}
		if (response.isFailure())
			throw new RegistryException("Unable to perform request (%d): %s",
					response.getCode(), response.getError());
	}

	private static CredentialManager cm() throws CMException {
		return CredentialManager.getInstance();
	}

	private static String getCredentials(String urlString) throws CMException,
			UnsupportedEncodingException {
		UsernamePassword userAndPass = cm().getUsernameAndPasswordForService(
				URI.create(urlString), true, null);
		// Check for user didn't log in...
		if (userAndPass == null)
			return null;
		return printBase64Binary((userAndPass.getUsername() + ":" + userAndPass
				.getPasswordAsString()).getBytes("UTF-8"));
	}

	private static void clearCredentials(String baseURL) throws CMException {
		@SuppressWarnings("deprecation")
		List<String> serviceURIs = cm()
				.getServiceURLsforAllUsernameAndPasswordPairs();
		for (String uri : serviceURIs)
			if (uri.startsWith(baseURL))
				cm().deleteUsernameAndPasswordForService(uri);
		// cm().resetAuthCache();
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

	class MyExperimentConnector {
		// authentication settings (and the current user)
		private String authString = null;

		MyExperimentConnector() throws RegistryException {
			// check if the stored credentials are valid
			ServerResponse response = null;
			try {
				String userPass = getCredentials(registryBase.toString());
				if (userPass == null)
					throw new RegistryException("no credentials available for "
							+ registryBase);

				// set the system to the "logged in" state from INI file
				// properties
				authString = userPass;
				response = GET(registryBase.toString() + WHOAMI);
			} catch (Exception e) {
				throw new RegistryException(
						"failed when verifying login credentials", e);
			}

			if (response == null || response.getCode() != HTTP_OK) {
				try {
					if (response != null)
						throw new RegistryException("failed to log in: "
								+ response.getError());
					else
						throw new RegistryException("unauthorized");
				} finally {
					try {
						authString = null;
						clearCredentials(registryBase.toString());
					} catch (Exception e) {
						throw new RegistryException(
								"failed to clear credentials", e);
					}
				}
			}
			logger.debug("logged in to repository successfully");
		}

		// getter for the current status
		private boolean isLoggedIn() {
			return authString != null;
		}

		private HttpURLConnection connect(String method, String strURL)
				throws MalformedURLException, IOException {
			HttpURLConnection conn = (HttpURLConnection) new URL(strURL)
					.openConnection();
			conn.setRequestMethod(method);
			if (method.equals("POST") || method.equals("PUT"))
				conn.setDoOutput(true);
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
		 * @return An object containing XML Document with server's response body
		 *         and a response code. Response body XML document might be null
		 *         if there was an error or the user wasn't authorised to
		 *         perform a certain action. Response code will always be set.
		 * @throws Exception
		 */
		public ServerResponse GET(String url) throws Exception {
			if (!isLoggedIn())
				logger.warn("not logged in");
			return receiveServerResponse(connect("GET", url), url, true, false);
		}

		/**
		 * Generic method to execute GET requests to myExperiment server.
		 * 
		 * @param url
		 *            The URL on myExperiment to issue GET request to.
		 * @return An object containing XML Document with server's response body
		 *         and a response code. Response body XML document might be null
		 *         if there was an error or the user wasn't authorised to
		 *         perform a certain action. Response code will always be set.
		 * @throws Exception
		 */
		public ServerResponse HEAD(String url) throws Exception {
			if (!isLoggedIn())
				logger.warn("not logged in");
			return receiveServerResponse(connect("HEAD", url), url, false, true);
		}

		/**
		 * Generic method to execute GET requests to myExperiment server.
		 * 
		 * @param url
		 *            The URL on myExperiment to POST to.
		 * @param xmlDataBody
		 *            Body of the XML data to be POSTed to strURL.
		 * @return An object containing XML Document with server's response body
		 *         and a response code. Response body XML document might be null
		 *         if there was an error or the user wasn't authorised to
		 *         perform a certain action. Response code will always be set.
		 * @throws Exception
		 */
		public ServerResponse POST(String url, String xmlDataBody)
				throws Exception {
			if (!isLoggedIn())
				return null;

			HttpURLConnection conn = connect("POST", url);

			conn.setRequestProperty("Content-Type", "application/xml");
			OutputStreamWriter out = new OutputStreamWriter(
					conn.getOutputStream());
			out.write(xmlDataBody);
			out.close();

			return receiveServerResponse(conn, url, false, false);
		}

		/**
		 * Generic method to execute DELETE requests to myExperiment server.
		 * This is only to be called when a user is logged in.
		 * 
		 * @param url
		 *            The URL on myExperiment to direct DELETE request to.
		 * @return An object containing XML Document with server's response body
		 *         and a response code. Response body XML document might be null
		 *         if there was an error or the user wasn't authorised to
		 *         perform a certain action. Response code will always be set.
		 * @throws Exception
		 */
		public ServerResponse DELETE(String url) throws Exception {
			if (!isLoggedIn())
				return null;
			return receiveServerResponse(connect("DELETE", url), url, true,
					false);
		}

		@Unused
		public ServerResponse PUT(String url, String xmlDataBody)
				throws Exception {
			if (!isLoggedIn())
				return null;

			HttpURLConnection conn = connect("PUT", url);
			conn.setRequestProperty("Content-Type", "application/xml");

			OutputStreamWriter out = new OutputStreamWriter(
					conn.getOutputStream());
			out.write(xmlDataBody);
			out.close();

			return receiveServerResponse(conn, url, false, false);
		}

		/**
		 * A common method for retrieving myExperiment server's response for all
		 * types of requests.
		 * 
		 * @param conn
		 *            Instance of the established URL connection to poll for
		 *            server's response.
		 * @param url
		 *            The URL on myExperiment with which the connection is
		 *            established.
		 * @param isGETrequest
		 *            Flag for identifying type of the request. True when the
		 *            current connection executes GET request; false when it
		 *            executes a POST request.
		 * @return An object containing XML Document with server's response body
		 *         and a response code. Response body XML document might be null
		 *         if there was an error or the user wasn't authorised to
		 *         perform a certain action. Response code will always be set.
		 */
		private ServerResponse receiveServerResponse(HttpURLConnection conn,
				String url, boolean isGETrequest, boolean isHEADrequest)
				throws Exception {
			switch (conn.getResponseCode()) {
			case HTTP_OK:
				/*
				 * data retrieval was successful - parse the response XML and
				 * return it along with response code
				 */
				if (isHEADrequest)
					return new ServerResponse(conn.getResponseCode(), null,
							null);
				return new ServerResponse(conn.getResponseCode(), null,
						getDocumentFromStream(conn.getInputStream()));
			case HTTP_NO_CONTENT:
				return new ServerResponse(HTTP_OK, null, null);

			case HttpURLConnection.HTTP_CREATED:
			case HttpURLConnection.HTTP_MOVED_PERM:
			case HttpURLConnection.HTTP_MOVED_TEMP:
			case HttpURLConnection.HTTP_SEE_OTHER:
			case HttpURLConnection.HTTP_USE_PROXY:
				return new ServerResponse(conn.getResponseCode(),
						conn.getHeaderField("Location"), null);

			case HTTP_BAD_REQUEST:
			case HTTP_FORBIDDEN:
				/*
				 * this was a bad XML request - need full XML response to
				 * retrieve the error message from it; Java throws IOException
				 * if getInputStream() is used when non HTTP_OK response code
				 * was received - hence can use getErrorStream() straight away
				 * to fetch the error document
				 */
				return new ServerResponse(conn.getResponseCode(), null,
						getDocumentFromStream(conn.getErrorStream()));

			case HTTP_UNAUTHORIZED:
				// this content is not authorised for current user
				logger.warn("non-authorised request to " + url + "\n"
						+ IOUtils.toString(conn.getErrorStream()));
				return new ServerResponse(conn.getResponseCode(), null, null);

			case HTTP_NOT_FOUND:
				if (isHEADrequest)
					return new ServerResponse(conn.getResponseCode(), null,
							null);
				throw new FileNotFoundException("no such resource: " + url);
			default:
				// unexpected response code - raise an exception
				throw new IOException(
						format("Received unexpected HTTP response code (%d) while %s %s",
								conn.getResponseCode(),
								(isGETrequest ? "fetching data at"
										: "posting data to"), url));
			}
		}

		class ServerResponse {
			private final int responseCode;
			private final String responseLocation;
			private final Document responseBody;

			ServerResponse(int responseCode, String responseLocation,
					Document responseBody) {
				this.responseCode = responseCode;
				this.responseBody = responseBody;
				this.responseLocation = responseLocation;
			}

			public int getCode() {
				return responseCode;
			}

			public boolean isFailure() {
				return responseCode >= HTTP_BAD_REQUEST;
			}

			public String getLocation() {
				return responseLocation;
			}

			public <T> T getResponse(Class<T> clazz) throws JAXBException {
				return jaxbContext.createUnmarshaller()
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
}
