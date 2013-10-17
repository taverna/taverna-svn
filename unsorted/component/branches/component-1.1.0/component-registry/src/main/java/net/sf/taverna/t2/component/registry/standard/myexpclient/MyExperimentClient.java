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

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Base64.encodeBytes;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.User.buildFromXML;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.isRunningInTaverna;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.component.registry.ClientVersion;
import net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.RequestType;
import net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 * @author Donal Fellows (mostly by stripping things out)
 */
public class MyExperimentClient {
	// CONSTANTS
	public static final String DEFAULT_BASE_URL = "http://www.myexperiment.org";
	public static final String PLUGIN_USER_AGENT = "Taverna2-myExperiment-plugin/"
			+ ClientVersion.VERSION
			+ " Java/"
			+ System.getProperty("java.version");
	private static final String INI_FILE_NAME = "myexperiment-plugin.ini";
	public static final String INI_BASE_URL = "my_experiment_base_url";
	public static final String INI_AUTO_LOGIN = "auto_login";

	public static boolean baseChangedSinceLastStart = false;

	// old format
	private static final DateFormat OLD_DATE_FORMATTER = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	private static final DateFormat OLD_SHORT_DATE_FORMATTER = new SimpleDateFormat(
			"HH:mm 'on' dd/MM/yyyy", Locale.ENGLISH);
	// universal date formatter
	private static final DateFormat NEW_DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss Z");

	// SETTINGS
	/** myExperiment base URL to use */
	private String BASE_URL;
	/**
	 * a folder, where the INI file will be / stored
	 */
	private java.io.File fIniFileDir;
	/**
	 * settings that are read/stored from/to INI file
	 */
	private Properties iniSettings;

	// the logger
	private Logger logger;

	// authentication settings (and the current user)
	private boolean LOGGED_IN = false;
	private String AUTH_STRING = "";

	// default constructor
	public MyExperimentClient() {
	}

	public MyExperimentClient(Logger logger) {
		this();

		logger.info("Starting myExperiment client");

		this.logger = logger;

		// === Load INI settings ===
		/*
		 * but loading settings from INI file, determine what folder is to be
		 * used for INI file
		 */
		if (isRunningInTaverna()) {
			/* running inside Taverna - use its folder to place the config file */
			fIniFileDir = new java.io.File(ApplicationRuntime.getInstance()
					.getApplicationHomeDir(), "conf");
		} else {
			/*
			 * running outside Taverna, place config file into the user's home
			 * directory
			 */
			fIniFileDir = new java.io.File(getProperty("user.home"),
					".Taverna2-myExperiment Plugin");
		}

		/* load preferences if the INI file exists */
		iniSettings = new Properties();
		loadSettings();

		/*
		 * === Check if defaults should be applied to override not sensible
		 * settings from INI file ===
		 * 
		 * verify that myExperiment BASE URL was read - use default otherwise
		 */
		if (BASE_URL == null || BASE_URL.length() == 0)
			BASE_URL = DEFAULT_BASE_URL;
		/*
		 * store this to settings (if no changes were made - same as before,
		 * alternatively default URL)
		 */
		iniSettings.put(INI_BASE_URL, BASE_URL);

		logger.info("Created myExperiment client");

	}

	// getter for the current status
	public boolean isLoggedIn() {
		return LOGGED_IN;
	}

	public String getBaseURL() {
		return BASE_URL;
	}

	public void setBaseURL(String baseURL) {
		this.BASE_URL = baseURL;
	}

	private java.io.File getIniFile() {
		return new java.io.File(fIniFileDir, INI_FILE_NAME);
	}

	// loads all plugin settings from the INI file
	public synchronized void loadSettings() {
		try {
			// === READ SETTINGS ===
			FileInputStream fIniInputStream = new FileInputStream(getIniFile());
			iniSettings.load(fIniInputStream);
			fIniInputStream.close();

			// set BASE_URL if from INI settings
			BASE_URL = iniSettings.getProperty(INI_BASE_URL);

		} catch (FileNotFoundException e) {
			logger.debug("myExperiment plugin INI file was not found, defaults will be used.");
		} catch (IOException e) {
			logger.error("failed to read settings from INI file: "
					+ getIniFile(), e);
		}
	}

	private static UsernamePassword getUserPass(String urlString) {
		try {
			URI userpassUrl = URI.create(urlString);
			UsernamePassword userAndPass = CredentialManager.getInstance()
					.getUsernameAndPasswordForService(userpassUrl, true, null);
			return userAndPass;
		} catch (CMException e) {
			throw new RuntimeException("Error in Taverna Credential Manager", e);
		}
	}

	public boolean doLogin() {
		// check if the stored credentials are valid
		ServerResponse response = null;
		Document doc = null;
		try {
			UsernamePassword userPass = getUserPass(BASE_URL);
			if (userPass == null) {
				logger.info("UserPass is null for " + BASE_URL);
				return false;
			}

			// set the system to the "logged in" state from INI file properties
			LOGGED_IN = true;
			logger.info("this.LOGGED_IN set to true");
			AUTH_STRING = encodeBytes((userPass.getUsername() + ":" + userPass
					.getPasswordAsString()).getBytes("UTF-8"));

			response = doMyExperimentGET(BASE_URL + "/whoami.xml");
		} catch (Exception e) {
			logger.error(
					"failed when verifying login credentials from INI file: "
							+ getIniFile(), e);
		}

		if (response.getResponseCode() == HTTP_UNAUTHORIZED) {
			logger.info("Unauthorized");
			try {
				clearCredentials();
				doc = null;
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			doc = response.getResponseBody();
		}

		// verify outcomes
		if (doc == null) {
			/*
			 * login credentials were invalid - revert to not logged in state
			 * and disable autologin function; stored credentials will be kept
			 * to allow the user to verify and edit them (login screen will be
			 * displayed as usual + an error message box will appear)
			 */

			LOGGED_IN = false;
			AUTH_STRING = "";
			iniSettings.put(INI_AUTO_LOGIN, FALSE.toString());

			return false;
		}

		// login credentials were verified successfully; load current user
		String strCurrentUserURI = doc.getDocumentElement().getAttribute("uri");
		try {
			fetchCurrentUser(strCurrentUserURI, RequestType.SHORT_LISTING);
			logger.debug("Logged in to myExperiment successfully with credentials that were loaded from INI file.");
			return true;
		} catch (Exception e) {
			/*
			 * this is highly unlikely because the login credentials were
			 * validated successfully just before this
			 */
			logger.error(
					format("failed fetching user data from myExperiment (%s)",
							strCurrentUserURI), e);
			return false;
		}
	}

	private void clearCredentials() throws CMException {
		CredentialManager cm = CredentialManager.getInstance();
		@SuppressWarnings("deprecation")
		List<String> toDelete = cm
				.getServiceURLsforAllUsernameAndPasswordPairs();
		for (String uri : toDelete)
			if (uri.startsWith(BASE_URL))
				cm.deleteUsernameAndPasswordForService(uri);
		// cm.resetAuthCache();
	}

	/**
	 * Simulates a "logout" action. Logging in and out in the plugin is only an
	 * abstraction created for user convenience; it is a purely virtual concept,
	 * because the myExperiment API is completely stateless - hence, logging out
	 * simply consists of "forgetting" the authentication details and updating
	 * the state.
	 */
	public void doLogout() throws Exception {
		LOGGED_IN = false;
		AUTH_STRING = "";
	}

	private HttpURLConnection connect(String strURL)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(strURL)
				.openConnection();
		conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
		return conn;
	}

	/**
	 * Generic method to execute GET requests to myExperiment server.
	 * 
	 * @param strURL
	 *            The URL on myExperiment to issue GET request to.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse doMyExperimentGET(String strURL) throws Exception {
		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(strURL);
		if (LOGGED_IN) {
			logger.info("It is logged in");
			// if the user has "logged in", also add authentication details
			conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
		} else {
			logger.info("It is not logged in");
		}

		// check server's response
		return doMyExperimentReceiveServerResponse(conn, strURL, true);
	}

	/**
	 * Generic method to execute GET requests to myExperiment server.
	 * 
	 * @param strURL
	 *            The URL on myExperiment to POST to.
	 * @param strXMLDataBody
	 *            Body of the XML data to be POSTed to strURL.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse doMyExperimentPOST(String strURL,
			String strXMLDataBody) throws Exception {
		// POSTing to myExperiment is only allowed for authorised users
		if (!LOGGED_IN)
			return null;

		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(strURL);

		// "tune" the connection
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
		/*
		 * the last line wouldn't be executed if the user wasn't logged in (see
		 * above code), so safe to run
		 */

		// prepare and PUT/POST XML data
		String strPOSTContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ strXMLDataBody;
		OutputStreamWriter out = new OutputStreamWriter(
				conn.getOutputStream());
		out.write(strPOSTContent);
		out.close();

		// check server's response
		return doMyExperimentReceiveServerResponse(conn, strURL, false);
	}

	/**
	 * Generic method to execute DELETE requests to myExperiment server. This is
	 * only to be called when a user is logged in.
	 * 
	 * @param strURL
	 *            The URL on myExperiment to direct DELETE request to.
	 * @return An object containing XML Document with server's response body and
	 *         a response code. Response body XML document might be null if
	 *         there was an error or the user wasn't authorised to perform a
	 *         certain action. Response code will always be set.
	 * @throws Exception
	 */
	public ServerResponse doMyExperimentDELETE(String strURL) throws Exception {
		/*
		 * open server connection using provided URL (with no modifications to
		 * it)
		 */
		HttpURLConnection conn = connect(strURL);

		// "tune" the connection
		conn.setRequestMethod("DELETE");
		conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);

		// check server's response
		return doMyExperimentReceiveServerResponse(conn, strURL, true);
	}

	private static Document getDocumentFromStream(InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException {
		InputStream is = new BufferedInputStream(inputStream);
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(is);
		is.close();
		return doc;
	}

	/**
	 * A common method for retrieving myExperiment server's response for both
	 * GET and POST requests.
	 * 
	 * @param conn
	 *            Instance of the established URL connection to poll for
	 *            server's response.
	 * @param strURL
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
	private ServerResponse doMyExperimentReceiveServerResponse(
			HttpURLConnection conn, String strURL, boolean bIsGETRequest)
			throws Exception {
		int iResponseCode = conn.getResponseCode();

		switch (iResponseCode) {
		case HTTP_OK:
			/*
			 * data retrieval was successful - parse the response XML and return
			 * it along with response code
			 */
			Document doc = getDocumentFromStream(conn.getInputStream());
			return new ServerResponse(iResponseCode, doc);

		case HTTP_BAD_REQUEST:
			/*
			 * this was a bad XML request - need full XML response to retrieve
			 * the error message from it; Java throws IOException if
			 * getInputStream() is used when non HTTP_OK response code was
			 * received - hence can use getErrorStream() straight away to fetch
			 * the error document
			 */
			Document edoc = getDocumentFromStream(conn.getErrorStream());
			return new ServerResponse(iResponseCode, edoc);

		case HTTP_UNAUTHORIZED:
			// this content is not authorised for current user
			return new ServerResponse(iResponseCode, null);

		default:
			// unexpected response code - raise an exception
			throw new IOException(format(
					"Received unexpected HTTP response code (%d) while %s %s",
					conn.getResponseCode(), (bIsGETRequest ? "fetching data at"
							: "posting data to"), strURL));
		}
	}

	/**
	 * a method to fetch a user instance with full details (including avatar
	 * image)
	 */
	public User fetchCurrentUser(String uri) {
		// fetch user data
		try {
			return fetchCurrentUser(uri, RequestType.PREVIEW);
		} catch (Exception ex) {
			logger.error("problem fetching user data from myExperiment (" + uri
					+ ")", ex);
			return null;
		}
	}

	public User fetchCurrentUser(String uri, RequestType requestType)
			throws Exception {
		return buildFromXML(getResource(Type.USER, uri, requestType), logger);
	}

	/**
	 * Fetches resource data and returns an XML document containing it.
	 * 
	 * @param type
	 *            Type of the resource for which the XML data is to be fetched.
	 *            This implies which elements are required to be selected.
	 * @param uri
	 *            URI of the resource in myExperiment API.
	 * @param type
	 *            Determines the level of detail of data to be fetched from the
	 *            API; constants for using in this field are defined in Resource
	 *            class.
	 */
	public Document getResource(Type type, String uri, RequestType reqtype)
			throws Exception {
		if (reqtype == RequestType.ALL) {
			/*
			 * it doesn't matter what kind of resource this is if all available
			 * data is requested anyway
			 */
			uri += "&all_elements=yes";
		} else {
			/*
			 * only required metadata is to be fetched; this depends on the type
			 * of the resource
			 */
			switch (type) {
			case WORKFLOW:
				uri += "&elements=" + Workflow.getRequiredAPIElements(reqtype);
				break;
			case FILE:
				uri += "&elements=" + File.getRequiredAPIElements(reqtype);
				break;
			case PACK:
				uri += "&elements=" + Pack.getRequiredAPIElements(reqtype);
				break;
			case INTERNAL:
				uri += "&all_elements=yes"; // TODO determine which are required
				break;
			case EXTERNAL:
				uri += "&all_elements=yes"; // TODO determine which are required
				break;
			case USER:
				uri += "&elements=" + User.getRequiredAPIElements(reqtype);
				break;
			case GROUP:
				uri += "&elements=" + Group.getRequiredAPIElements(reqtype);
				break;
			case TAG:
				/*
				 * this should set no elements, because default is desired at
				 * the moment - but even having "&elements=" with and empty
				 * string at the end will still retrieve default fields from the
				 * API
				 */
				uri += "&elements=" + Tag.getRequiredAPIElements(reqtype);
				break;
			default:
				uri += "&all_elements=yes";
				break;
			}
		}

		return doMyExperimentGET(uri).getResponseBody();
	}

	/**
	 * Fetches workflow data from myExperiment.
	 * 
	 * @param strWorkflowURI
	 *            URI of the workflow to be opened.
	 * @return Workflow instance containing only workflow data and content type.
	 */
	public Workflow fetchWorkflowBinary(String strWorkflowURI) throws Exception {
		// fetch workflows data
		Document doc = getResource(Type.WORKFLOW, strWorkflowURI,
				RequestType.CONTENT);

		// verify that the type of the workflow data is correct
		Element root = doc.getDocumentElement();
		Workflow w = new Workflow();
		w.setVisibleType(getChildText(root, "type"));
		w.setContentType(getChildText(root, "content-type"));

		if (!w.isTavernaWorkflow())
			throw new Exception(
					"Unsupported workflow type. Details:\nWorkflow type: "
							+ w.getVisibleType() + "\nMime type: "
							+ w.getContentType());

		// check that content encoding is correct
		String strEncoding = getChild(root, "content").getAttribute("encoding");
		String strDataFormat = getChild(root, "content").getAttribute("type");
		if (!strEncoding.toLowerCase().equals("base64")
				|| !strDataFormat.toLowerCase().equals("binary"))
			throw new Exception(
					"Unsupported workflow data format. Details:\nContent encoding: "
							+ strEncoding + "\nFormat: " + strDataFormat);

		// all checks seem to be fine, decode workflow data
		byte[] arrWorkflowData = Base64.decode(getChildText(root, "content"));
		w.setContent(arrWorkflowData);

		return w;
	}

	/**
	 * A helper to fetch workflows, files or packs of a specific user. This will
	 * only make *one* request to the API, therefore it's faster than getting
	 * all the items one by one.
	 * 
	 * @param user
	 *            User instance for which the items are to be fetched.
	 * @param type
	 *            One of Resource.WORKFLOW, Resource.FILE, Resource.PACK
	 * @param iRequestType
	 *            Type of the request - i.e. amount of data to fetch. One of
	 *            Resource.REQUEST_SHORT_LISTING, Resource.REQUEST_FULL_LISTING,
	 *            Resource.REQUEST_FULL_PREVIEW, Resource.REQUEST_ALL_DATA.
	 * @return An XML document containing data about all items in the amount
	 *         that was specified.
	 */
	public Document getUserContributions(User user, Type type,
			RequestType requestType, int page) {
		Document doc = null;
		String strURL = BASE_URL;
		String strElements = "&elements=";

		try {
			// determine query parameters
			switch (type) {
			case WORKFLOW:
				strURL += "/workflows.xml?uploader=";
				strElements += Workflow.getRequiredAPIElements(requestType);
				break;
			case FILE:
				strURL += "/files.xml?uploader=";
				strElements += File.getRequiredAPIElements(requestType);
				break;
			case PACK:
				strURL += "/packs.xml?owner=";
				strElements += Workflow.getRequiredAPIElements(requestType);
				break;
			default:
				throw new IllegalArgumentException(
						"cannot get user contributions for " + type.getName());
			}

			if (page != 0) {
				strElements += "&num=100&page=" + page;
			}

			// create final query URL and retrieve data
			strURL += urlEncodeQuery(user.getResource()) + strElements;
			doc = doMyExperimentGET(strURL).getResponseBody();
		} catch (Exception e) {
			logger.error("problem fetching user's contributions", e);
		}

		return doc;
	}

	public static Date parseDate(String date) {
		if (date == null || date.isEmpty())
			return null;
		try {
			return OLD_DATE_FORMATTER.parse(date);
		} catch (ParseException e) {
		}
		try {
			return OLD_SHORT_DATE_FORMATTER.parse(date);
		} catch (ParseException e) {
		}
		try {
			return NEW_DATE_FORMATTER.parse(date);
		} catch (ParseException e2) {
		}
		return null;
	}

	public static String formatDate(Date date) {
		return NEW_DATE_FORMATTER.format(date);
	}

	/**
	 * Prepares the string to serve as a part of url query to the server.
	 * 
	 * @param query
	 *            The string that needs URL encoding.
	 * @return URL encoded string that can be inserted into the request URL.
	 */
	private static String urlEncodeQuery(String query) {
		try {
			return URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// do nothing
			return "";
		}
	}

	public ServerResponse doMyExperimentPUT(String strURL, String strXMLDataBody)
			throws Exception {
		if (!LOGGED_IN)
			return null;

		HttpURLConnection conn = connect(strURL);

		conn.setRequestMethod("PUT");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);

		String strPOSTContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ strXMLDataBody;
		OutputStreamWriter out = new OutputStreamWriter(
				conn.getOutputStream());
		out.write(strPOSTContent);
		out.close();

		return doMyExperimentReceiveServerResponse(conn, strURL, false);
	}

	public static class ServerResponse {
		// CONSTANTS
		public static int LOCAL_FAILURE = -1;

		// STORAGE
		private int iResponseCode;
		private Document docResponseBody;

		public ServerResponse() {
			// do nothing - empty constructor
		}

		public ServerResponse(int responseCode, Document responseBody) {
			super();

			this.iResponseCode = responseCode;
			this.docResponseBody = responseBody;
		}

		public int getResponseCode() {
			return iResponseCode;
		}

		public void setResponseCode(int responseCode) {
			this.iResponseCode = responseCode;
		}

		public Document getResponseBody() {
			return docResponseBody;
		}

		public Element getResponse() {
			return docResponseBody.getDocumentElement();
		}

		@SuppressWarnings("unchecked")
		public <T> T getResponse(JAXBContext context, Class<T> clazz)
				throws JAXBException {
			return (T) context.createUnmarshaller().unmarshal(getResponse(),
					clazz);
		}

		public void setResponseBody(Document responseBody) {
			this.docResponseBody = responseBody;
		}
	}
}
