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
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Base64.encodeBytes;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.COMMENT;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.FILE;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.GROUP;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.PACK;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.PACK_EXTERNAL_ITEM;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.PACK_INTERNAL_ITEM;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.REQUEST_ALL_DATA;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.REQUEST_FULL_LISTING;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.REQUEST_FULL_PREVIEW;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.REQUEST_USER_APPLIED_TAGS_ONLY;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.REQUEST_WORKFLOW_CONTENT_ONLY;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.TAG;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.USER;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.WORKFLOW;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.ServerResponse.LOCAL_FAILURE;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.User.buildFromXML;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChild;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getChildText;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.getResourceCollection;
import static net.sf.taverna.t2.component.registry.standard.myexpclient.Util.isRunningInTaverna;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 */
public class MyExperimentClient {
	// CONSTANTS
	public static final String DEFAULT_BASE_URL = "http://www.myexperiment.org";
	public static final String PLUGIN_USER_AGENT = "Taverna2-myExperiment-plugin/"
			+ "0.2beta" + " Java/" + System.getProperty("java.version");
	private static final String INI_FILE_NAME = "myexperiment-plugin.ini";
	private static final int EXAMPLE_WORKFLOWS_PACK_ID = 254;

	public static final String INI_BASE_URL = "my_experiment_base_url";
	public static final String INI_AUTO_LOGIN = "auto_login";
	public static final String INI_FAVOURITE_SEARCHES = "favourite_searches";
	public static final String INI_SEARCH_HISTORY = "search_history";
	public static final String INI_TAG_SEARCH_HISTORY = "tag_search_history";
	public static final String INI_PREVIEWED_ITEMS_HISTORY = "previewed_items_history";
	public static final String INI_OPENED_ITEMS_HISTORY = "opened_items_history";
	public static final String INI_UPLOADED_ITEMS_HISTORY = "uploaded_items_history";
	public static final String INI_DOWNLOADED_ITEMS_HISTORY = "downloaded_items_history";
	public static final String INI_COMMENTED_ITEMS_HISTORY = "commented_items_history";
	public static final String INI_DEFAULT_LOGGED_IN_TAB = "default_tab_for_logged_in_users";
	public static final String INI_DEFAULT_ANONYMOUS_TAB = "default_tab_for_anonymous_users";
	public static final String INI_MY_STUFF_WORKFLOWS = "show_workflows_in_my_stuff";
	public static final String INI_MY_STUFF_FILES = "show_files_in_my_stuff";
	public static final String INI_MY_STUFF_PACKS = "show_packs_in_my_stuff";

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
	private User current_user = null;

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

	// getter for the current user, if one is logged in to myExperiment
	public User getCurrentUser() {
		return current_user;
	}

	// setter for the current user (the one that has logged in to myExperiment)
	public void setCurrentUser(User user) {
		current_user = user;
	}

	public Properties getSettings() {
		return iniSettings;
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

	// writes all plugin settings to the INI file
	private void storeSettings() {
		// === STORE THE SETTINGS ===
		try {
			fIniFileDir.mkdirs();
			FileOutputStream fIniOutputStream = new FileOutputStream(
					getIniFile());
			iniSettings.store(fIniOutputStream, "Test comment");
			fIniOutputStream.close();
		} catch (IOException e) {
			logger.error(
					"failed to store settings to INI file " + getIniFile(), e);
		}
	}

	public void storeHistoryAndSettings() {
		storeSettings();
	}

	private UsernamePassword getUserPass(String urlString) {
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
				CredentialManager cm = CredentialManager.getInstance();
				@SuppressWarnings("deprecation")
				List<String> toDelete = cm
						.getServiceURLsforAllUsernameAndPasswordPairs();
				for (String uri : toDelete)
					if (uri.startsWith(BASE_URL))
						cm.deleteUsernameAndPasswordForService(uri);
				// CredentialManager.getInstance().resetAuthCache();
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
			current_user = fetchCurrentUser(strCurrentUserURI);
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
		URL url = new URL(strURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
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
		URL url = new URL(strURL);
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

		// "tune" the connection
		urlConn.setRequestMethod("POST");
		urlConn.setDoOutput(true);
		urlConn.setRequestProperty("Content-Type", "application/xml");
		urlConn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
		urlConn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
		/*
		 * the last line wouldn't be executed if the user wasn't logged in (see
		 * above code), so safe to run
		 */

		// prepare and PUT/POST XML data
		String strPOSTContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ strXMLDataBody;
		OutputStreamWriter out = new OutputStreamWriter(
				urlConn.getOutputStream());
		out.write(strPOSTContent);
		out.close();

		// check server's response
		return doMyExperimentReceiveServerResponse(urlConn, strURL, false);
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
		URL url = new URL(strURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		// "tune" the connection
		conn.setRequestMethod("DELETE");
		conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
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
			return buildFromXML(getResource(USER, uri, REQUEST_FULL_PREVIEW),
					logger);
		} catch (Exception ex) {
			logger.error("problem fetching user data from myExperiment (" + uri
					+ ")", ex);
			return null;
		}
	}

	/**
	 * Fetches resource data and returns an XML document containing it.
	 * 
	 * @param resourceType
	 *            Type of the resource for which the XML data is to be fetched.
	 *            This implies which elements are required to be selected.
	 * @param uri
	 *            URI of the resource in myExperiment API.
	 * @param requestType
	 *            Determines the level of detail of data to be fetched from the
	 *            API; constants for using in this field are defined in Resource
	 *            class.
	 */
	public Document getResource(int resourceType, String uri, int requestType)
			throws Exception {
		if (requestType == REQUEST_ALL_DATA) {
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
			switch (resourceType) {
			case WORKFLOW:
				uri += "&elements="
						+ Workflow.getRequiredAPIElements(requestType);
				break;
			case FILE:
				uri += "&elements=" + File.getRequiredAPIElements(requestType);
				break;
			case PACK:
				uri += "&elements=" + Pack.getRequiredAPIElements(requestType);
				break;
			case PACK_INTERNAL_ITEM:
				uri += "&all_elements=yes"; // TODO determine which are required
				break;
			case PACK_EXTERNAL_ITEM:
				uri += "&all_elements=yes"; // TODO determine which are required
				break;
			case USER:
				uri += "&elements=" + User.getRequiredAPIElements(requestType);
				break;
			case GROUP:
				uri += "&elements=" + Group.getRequiredAPIElements(requestType);
				break;
			case TAG:
				/*
				 * this should set no elements, because default is desired at
				 * the moment - but even having "&elements=" with and empty
				 * string at the end will still retrieve default fields from the
				 * API
				 */
				uri += "&elements=" + Tag.getRequiredAPIElements(requestType);
				break;
			case COMMENT:
				/*
				 * this should set no elements, because default is desired at
				 * the moment - but even having "&elements=" with and empty
				 * string at the end will still retrieve default fields from the
				 * API
				 */
				uri += "&elements="
						+ Comment.getRequiredAPIElements(requestType);
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
		Document doc = getResource(WORKFLOW, strWorkflowURI,
				REQUEST_WORKFLOW_CONTENT_ONLY);

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

	public List<Workflow> getExampleWorkflows() {
		List<Workflow> workflows = new ArrayList<Workflow>();

		try {
			String strExampleWorkflowsPackUrl = BASE_URL + "/pack.xml?id="
					+ EXAMPLE_WORKFLOWS_PACK_ID
					+ "&elements=internal-pack-items";
			Document doc = doMyExperimentGET(strExampleWorkflowsPackUrl)
					.getResponseBody();

			if (doc != null) {
				NodeList allInternalItems = getChild(doc.getDocumentElement(),
						"internal-pack-items").getElementsByTagName("workflow");
				for (int i = 0; i < allInternalItems.getLength(); i++) {
					Element e = (Element) allInternalItems.item(i);
					String itemUri = e.getAttribute("uri");

					Element item = doMyExperimentGET(itemUri).getResponseBody()
							.getDocumentElement();

					String workflowUri = getChild(getChild(item, "item"),
							"workflow").getAttribute("uri");
					Document docCurWorkflow = getResource(WORKFLOW,
							workflowUri, REQUEST_FULL_LISTING);
					workflows
							.add(Workflow.buildFromXML(docCurWorkflow, logger));
				}
			}
		} catch (Exception e) {
			logger.error("problem retrieving example workflows", e);
		}

		logger.debug(workflows.size()
				+ " example workflows retrieved from myExperiment");

		return workflows;
	}

	public TagCloud getGeneralTagCloud(int size) {
		TagCloud tcCloud = new TagCloud();

		try {
			// assemble tag cloud URL and fetch the XML document
			String strTagCloudURL = BASE_URL + "/tag-cloud.xml?num="
					+ (size > 0 ? ("" + size) : "all");
			Document doc = this.doMyExperimentGET(strTagCloudURL)
					.getResponseBody();

			// process all tags and add them to the cloud
			if (doc != null) {
				NodeList nodes = doc.getDocumentElement().getElementsByTagName(
						"tag");
				for (int i = 0; i < nodes.getLength(); i++) {
					Element e = (Element) nodes.item(i);
					Tag t = new Tag();
					t.setTitle(e.getTextContent());
					t.setTagName(e.getTextContent());
					t.setResource(e.getAttribute("resource"));
					t.setURI(e.getAttribute("uri"));
					t.setCount(parseInt(e.getAttribute("count")));

					tcCloud.getTags().add(t);
				}
			}
			logger.debug(format(
					"Tag cloud retrieval successful; fetched %d tags from myExperiment",
					tcCloud.getTags().size()));
		} catch (Exception e) {
			logger.error("problem getting tag cloud", e);
		}

		return tcCloud;
	}

	public TagCloud getUserTagCloud(User user, int size) {
		TagCloud tcCloud = new TagCloud();

		/*
		 * iterate through all tags that the user has applied; fetch the title
		 * and the number of times that this tag was applied across myExperiment
		 * (e.g. overall popularity)
		 */
		try {
			/*
			 * update user tags first (this happens concurrently with the other
			 * threads during the load time, hence needs to be synchronised
			 * properly)
			 */
			synchronized (user.getTags()) {
				user.getTags().clear();
				Document doc = getResource(USER, user.getURI(),
						REQUEST_USER_APPLIED_TAGS_ONLY);
				NodeList iNewUserTags = getChild(doc.getDocumentElement(),
						"tags-applied").getChildNodes();
				getResourceCollection(iNewUserTags, user.getTags());
			}

			// fetch additional required data about the tags
			for (Map<String, String> tagResources : user.getTags()) {
				/*
				 * get the tag object uri in myExperiment API and fetch tag data
				 * from myExperiment (namely, number of times that this tag was
				 * applied)
				 */
				Element root = doMyExperimentGET(tagResources.get("uri"))
						.getResponseBody().getDocumentElement();

				// create the tag
				Tag t = new Tag();
				t.setTagName(getChildText(root, "name"));
				t.setCount(parseInt(getChildText(root, "count")));

				tcCloud.add(t);
			}

			/*
			 * a little preprocessing before tag selection - if "size" is set to
			 * 0, -1 or any negative number, assume the request is for ALL user
			 * tags
			 */
			if (size <= 0)
				size = tcCloud.getTags().size();

			// sort the collection by popularity..
			tcCloud.sort(new Tag.ReversePopularityComparator());

			// ..take top "size" elements
			int iSelectedTags = 0;
			List<Tag> tagListOfRequiredSize = new ArrayList<Tag>();
			for (Tag tag : tcCloud.getTags()) {
				if (iSelectedTags++ >= size)
					break;
				tagListOfRequiredSize.add(tag);
			}

			/*
			 * purge the original tag collection; add only selected tags to it;
			 * then sort back in alphabetical order again
			 */
			tcCloud.clear();
			tcCloud.addAll(tagListOfRequiredSize);
			tcCloud.sort(new Tag.AlphanumericComparator());
		} catch (Exception e) {
			logger.error(
					format("problem fetching user tags for user ID = %d",
							user.getID()), e);
		}

		return tcCloud;
	}

	/**
	 * A helper to fetch workflows, files or packs of a specific user. This will
	 * only make *one* request to the API, therefore it's faster than getting
	 * all the items one by one.
	 * 
	 * @param user
	 *            User instance for which the items are to be fetched.
	 * @param iResourceType
	 *            One of Resource.WORKFLOW, Resource.FILE, Resource.PACK
	 * @param iRequestType
	 *            Type of the request - i.e. amount of data to fetch. One of
	 *            Resource.REQUEST_SHORT_LISTING, Resource.REQUEST_FULL_LISTING,
	 *            Resource.REQUEST_FULL_PREVIEW, Resource.REQUEST_ALL_DATA.
	 * @return An XML document containing data about all items in the amount
	 *         that was specified.
	 */
	public Document getUserContributions(User user, int iResourceType,
			int iRequestType, int page) {
		Document doc = null;
		String strURL = BASE_URL;
		String strElements = "&elements=";

		try {
			// determine query parameters
			switch (iResourceType) {
			case WORKFLOW:
				strURL += "/workflows.xml?uploader=";
				strElements += Workflow.getRequiredAPIElements(iRequestType);
				break;
			case FILE:
				strURL += "/files.xml?uploader=";
				strElements += File.getRequiredAPIElements(iRequestType);
				break;
			case PACK:
				strURL += "/packs.xml?owner=";
				strElements += Workflow.getRequiredAPIElements(iRequestType);
				break;
			}

			if (page != 0) {
				strElements += "&num=100&page=" + page;
			}

			// create final query URL and retrieve data
			strURL += urlEncodeQuery(user.getResource()) + strElements;
			doc = this.doMyExperimentGET(strURL).getResponseBody();
		} catch (Exception e) {
			logger.error("problem fetching user's contributions", e);
		}

		return doc;
	}

	/**
	 * Converts a tag list into tag cloud data by fetching tag application count
	 * for each instance in the list.
	 * 
	 * @param tags
	 *            Tag list to work on.
	 */
	public void convertTagListIntoTagCloudData(List<Tag> tags) {
		try {
			Document doc = null;

			for (Tag t : tags) {
				doc = getResource(TAG, t.getURI(), REQUEST_ALL_DATA);
				Element rootElement = doc.getDocumentElement();
				t.setCount(parseInt(getChildText(rootElement, "count")));
			}
		} catch (Exception e) {
			logger.error("problem getting tag application counts "
					+ "when turning tag list into tag cloud data", e);
		}
	}

	/**
	 * Fetches the data about user's favourite items and updates the provided
	 * user instance with the latest data.
	 */
	/*
	 * public void updateUserFavourites(User user) { // fetch and update
	 * favourites data try { Document doc = this.getResource(Resource.USER,
	 * user.getURI(), Resource.REQUEST_USER_FAVOURITES_ONLY); List<Resource>
	 * newUserFavouritesList = Util.retrieveUserFavourites(doc
	 * .getRootElement());
	 * 
	 * user.getFavourites().clear();
	 * user.getFavourites().addAll(newUserFavouritesList); } catch (Exception
	 * ex) { logger
	 * .error("Failed to fetch favourites data from myExperiment for a user (URI: "
	 * + user.getURI() + "); exception:\n" + ex); JOptionPane
	 * .showMessageDialog( null,
	 * "Couldn't synchronise data about your favourite items with myExperiment.\n"
	 * +
	 * "You might not be able to add / remove other items to your favourites and.\n"
	 * +
	 * "Please refresh your profile data manually by clicking 'Refresh' button in 'My Stuff' tab."
	 * , "myExperiment Plugin - Error", JOptionPane.ERROR_MESSAGE); } }
	 */
	/**
	 * For each comment in the list fetches the user which made the comment, the
	 * date when it was made, etc.
	 */
	public void updateCommentListWithExtraData(List<Comment> comments) {
		try {
			Document doc = null;

			for (Comment c : comments) {
				doc = getResource(COMMENT, c.getURI(), REQUEST_ALL_DATA);
				Element rootElement = doc.getDocumentElement();

				Element userElement = getChild(rootElement, "author");
				User u = new User();
				u.setTitle(userElement.getTextContent());
				u.setName(userElement.getTextContent());
				u.setResource(userElement.getAttribute("resource"));
				u.setURI(userElement.getAttribute("uri"));
				c.setUser(u);

				String createdAt = getChildText(rootElement, "created-at");
				if (createdAt != null && !createdAt.equals(""))
					c.setCreatedAt(createdAt);
			}
		} catch (Exception e) {
			logger.error("problem updating comment list for preview", e);
		}
	}

	public ServerResponse postComment(Resource resource, String strComment) {
		try {
			String strCommentData = "<comment><subject resource=\""
					+ resource.getResource() + "\"/><comment>" + strComment
					+ "</comment></comment>";
			ServerResponse response = doMyExperimentPOST(BASE_URL
					+ "/comment.xml", strCommentData);

			if (response.getResponseCode() == HTTP_OK) {
				// XML response should contain the new comment that was posted
				Comment cNew = new Comment(response.getResponseBody(), logger);

				/*
				 * this resource should be commentable on as the comment was
				 * posted
				 */
				resource.getComments().add(cNew);
			}

			/*
			 * will return the whole response object so that the application
			 * could decide on the next steps
			 */
			return response;
		} catch (Exception e) {
			logger.error(
					"problem trying to post a comment for " + resource.getURI(),
					e);
			return new ServerResponse(LOCAL_FAILURE, null);
		}
	}

	public ServerResponse addFavourite(Resource resource) {
		try {
			String strData = "<favourite><object resource=\""
					+ resource.getResource() + "\"/></favourite>";
			return doMyExperimentPOST(BASE_URL + "/favourite.xml", strData);
			// return full server response
		} catch (Exception e) {
			logger.error("problem trying to add an item (" + resource.getURI()
					+ ") to favourites", e);
			return new ServerResponse(LOCAL_FAILURE, null);
		}
	}

	public ServerResponse deleteFavourite(Resource resource) {
		try {
			/*
			 * deleting a favourite is a two-step process - first need to
			 * retrieve the the actual "favourite" object by current user's URL
			 * and favourited item's URL
			 */
			String strGetFavouriteObjectURL = BASE_URL
					+ "/favourites.xml?user="
					+ urlEncodeQuery(getCurrentUser().getResource())
					+ "&object=" + urlEncodeQuery(resource.getResource());
			ServerResponse response = doMyExperimentGET(strGetFavouriteObjectURL);

			// now retrieve this object's URI from server's response
			Element root = response.getResponseBody().getDocumentElement();
			String strFavouriteURI = getChild(root, "favourite").getAttribute(
					"uri");

			// finally, delete the found object
			return doMyExperimentDELETE(strFavouriteURI);
			// return full server response
		} catch (Exception e) {
			logger.error(
					"problem trying to remove an item (" + resource.getURI()
							+ ") from favourites", e);
			return new ServerResponse(LOCAL_FAILURE, null);
		}
	}

	public static Date parseDate(String date) {
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

		URL url = new URL(strURL);
		HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

		urlConn.setRequestMethod("PUT");
		urlConn.setDoOutput(true);
		urlConn.setRequestProperty("Content-Type", "application/xml");
		urlConn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
		urlConn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);

		String strPOSTContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
				+ strXMLDataBody;
		OutputStreamWriter out = new OutputStreamWriter(
				urlConn.getOutputStream());
		out.write(strPOSTContent);
		out.close();

		return doMyExperimentReceiveServerResponse(urlConn, strURL, false);
	}
}
