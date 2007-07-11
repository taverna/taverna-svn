package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.xml.Workflow;
import net.sf.taverna.service.xml.WorkflowDocument;
import net.sf.taverna.service.xml.WorkflowsDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Unit tests for workflow resource creation and retrieval
 * 
 * @author Stian Soiland
 *
 */
public class TestWorkflow extends ClientTest {

	private static Reference justCreated;

	@BeforeClass
	public static void resetJustCreated() {
		justCreated = null;
	}
	
	@Test
	public void getAll() throws IOException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/workflows");
		System.out.println("Checking " + request.getResourceRef());
		request.getClientInfo().getAcceptedMediaTypes().add(
				new Preference<MediaType>(restType));
		request.setMethod(Method.GET);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		response.getEntity().write(System.out);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getAllXML() throws IOException, ParseException, XmlException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/workflows");
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		WorkflowsDocument doc = WorkflowsDocument.Factory.parse(response.getEntity().getStream());
		// FIXME: Must insert some workflows first
		for (Workflow wf : doc.getWorkflows().getWorkflowArray()) {
			assertTrue(wf.getHref().startsWith(BASE_URL + "workflow"));
		}
	}

	@Test
	public void create() throws IOException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/workflows");
		request.setMethod(Method.POST);
		request.setEntity(workflow, scuflType);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_CREATED, response.getStatus());
		justCreated = response.getRedirectRef();
		System.out.println("Created " + justCreated);
	}

	@Test
	public void readJustCreated() throws IOException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(MediaType.TEXT_PLAIN));
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		String result = response.getEntity().getText();
		
		assertTrue(MediaType.TEXT_PLAIN.includes(response.getEntity().getMediaType()));
		assertTrue(result.startsWith("Workflow "));
		assertTrue(result.contains(workflow));
	}

	
	@Test
	public void readJustCreatedXML() throws IOException, XmlException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertTrue(restType.includes(response.getEntity().getMediaType()));

		Workflow wf = WorkflowDocument.Factory.parse(response.getEntity().getStream()).getWorkflow();
		assertEquals("User URI didn't match owner", useruri, wf.getOwner().getHref());
	}

	@Test
	public void readJustCreatedScufl() throws IOException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(scuflType));
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertTrue(scuflType.includes(response.getEntity().getMediaType()));
		assertTrue(response.getEntity().getText().equals(workflow));
	}
	
}
