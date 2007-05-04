package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.util.XMLUtils;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

public class TestUser extends ClientTest {

	private static Reference justCreated;

	@BeforeClass
	public static void resetJustCreated() {
		justCreated = null;
	}
	private static String userName;
	private static final String password = "blih1337blapp";


	@Test
	public synchronized void create() throws IOException {
		Request request = new Request();
		userName = "test-" + UUID.randomUUID();
		Client client = new Client(Protocol.HTTP);
		String url = BASE_URL + "/v1/users";
		request.setResourceRef(url);
		request.setMethod(Method.POST);
		request.setEntity("<user xmlns='" + TavernaService.NS + "'>" +
				"  <username>" + userName + "</username>" +
				"  <password>" + password + "</password>" +
				"</user>", restType);
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		justCreated = response.getRedirectRef();
		assertNotNull(justCreated);
		System.out.println("Created user " + justCreated);
		assertTrue(justCreated.toString().startsWith(url));
		assertTrue(justCreated.toString().endsWith(userName));
		assertEquals(url + "/" + userName, justCreated.toString());
		// No password returned
		assertEquals("", response.getEntity().getText());
	}

	@Test
	public void createMinimal() throws IOException {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		String url = BASE_URL + "/v1/users";
		request.setResourceRef(url);
		request.setMethod(Method.POST);
		request.setEntity("<user xmlns='" + TavernaService.NS + "'>" +
				"</user>", restType);
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		Reference created = response.getRedirectRef();
		assertNotNull(created);
		assertTrue(created.toString().startsWith(url));
		System.out.println("Created minimal user " + created);
		// And that we got a password back
		assertTrue(response.getEntity().getText().length() > 8);
	}
	

	
	

	@Test
	public void readJustCreated() throws IOException, ParseException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		Response response = client.handle(request);
		assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
		assertFalse(response.getStatus().isSuccess());

		ChallengeResponse challengeResponse =
			new ChallengeResponse(ChallengeScheme.HTTP_BASIC, 
				userName, password);
		request.setChallengeResponse(challengeResponse);
		response = client.handle(request);
		assertEquals("Request did not succeed", Status.SUCCESS_OK, response.getStatus());

		String result = response.getEntity().getText();
		// Should do text/plain as fallback
		System.out.println(result);
		assertTrue(MediaType.TEXT_PLAIN.includes(response.getEntity().getMediaType()));
		
		readJustCreatedXML();
	}

	
	@Test
	public void readJustCreatedXML() throws IOException, ParseException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		Response response = client.handle(request);
		assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
		assertFalse(response.getStatus().isSuccess());

		ChallengeResponse challengeResponse =
			new ChallengeResponse(ChallengeScheme.HTTP_BASIC, 
				userName, password);
		request.setChallengeResponse(challengeResponse);
		response = client.handle(request);
		assertEquals("Request did not succeed", Status.SUCCESS_OK, response.getStatus());

		assertTrue(restType.includes(response.getEntity().getMediaType()));
		Document userDoc = XMLUtils.parseXML(response.getEntity().getStream());
		Element userElement = userDoc.getRootElement();
		assertEquals("user", userElement.getName());
		assertEquals(TavernaService.NS, userElement.getNamespace().getURI());
	}
	
}
