package net.sf.taverna.service.rest;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.TavernaConstants;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.test.TestCommon;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Request;

/**
 * Base class for unit tests that need a running server. The class will start
 * the server on initialization and shut it down afterwards.
 * 
 * @author Stian Soiland
 */
public abstract class ClientTest extends TestCommon {

	public static final int PORT = 8977;

	public static final String ROOT_URL = "http://localhost:" + PORT  + "/";
	
	public static final String BASE_URL = ROOT_URL + "v1/";
	
	public static final MediaType restType = new MediaType(TavernaConstants.restType);

	public static final MediaType scuflType = new MediaType(TavernaConstants.scuflType);

	public static final MediaType baclavaType = new MediaType(TavernaConstants.baclavaType);

	private static RestApplication server;

	public static String username;

	public static String password;

	public static String useruri;
	
	public static User user;
	
	public URIFactory uriFactory = URIFactory.getInstance(BASE_URL);
		
	@BeforeClass
	public synchronized static void startServer() throws Exception {
		stopServer();
		server = new RestApplication();
		server.startServer(PORT);
	}

	@AfterClass
	public synchronized static void stopServer() throws Exception {
		if (server != null) {
			server.stopServer();
		}
	}
	
	@BeforeClass
	public synchronized static void registerAdminUser() {
		username = null;
		useruri = null;
		DAOFactory daoFactory = DAOFactory.getFactory();
		user = new User();
		password = User.generatePassword();
		user.setPassword(password);
		user.setAdmin(true);
		daoFactory.getUserDAO().create(user);
		daoFactory.commit();
		username = user.getUsername();
		useruri = BASE_URL + "users/" + username;
		System.out.println("Registered " + username + " " + password);
	}

	/**
	 * Make an authenticated GET request.
	 * 
	 * @return
	 */
	public Request makeAuthRequest() {
		Request request = new Request();
		ChallengeResponse challengeResponse = 
			new ChallengeResponse(ChallengeScheme.HTTP_BASIC, 
				username, password);
		request.setChallengeResponse(challengeResponse);
		request.setMethod(Method.GET);
		return request;
	}
	
}
