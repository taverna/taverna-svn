package net.sf.taverna.service.rest;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.test.TestCommon;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;

/**
 * Base class for unit tests that need a running server. The class will start
 * the server on initialization and shut it down afterwards.
 * 
 * @author Stian Soiland
 */
public abstract class ClientTest extends TestCommon {

	public static final int PORT = 8977;

	public static final String BASE_URL = "http://localhost:" + PORT + "/v1/";
	
	public static final MediaType restType = new MediaType(TavernaService.restType);

	public static final MediaType scuflType = new MediaType(TavernaService.scuflType);

	public static final MediaType baclavaType = new MediaType(TavernaService.baclavaType);

	private static RestApplication server;

	public static String username;

	public static String password;

	public static String useruri;
	
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
	public synchronized static void registerUser() {
		username = null;
		useruri = null;
		DAOFactory daoFactory = DAOFactory.getFactory();
		User user = new User();
		password = User.generatePassword();
		user.setPassword(password);
		daoFactory.getUserDAO().create(user);
		daoFactory.commit();
		username = user.getUsername();
		useruri = BASE_URL + "users/" + username;
		System.out.println("Registered " + username + " " + password);
	}
	
	public Request makeAuthRequest() {
		Request request = new Request();
		ChallengeResponse challengeResponse = 
			new ChallengeResponse(ChallengeScheme.HTTP_BASIC, 
				username, password);
		request.setChallengeResponse(challengeResponse);
		return request;
	}
	
}
