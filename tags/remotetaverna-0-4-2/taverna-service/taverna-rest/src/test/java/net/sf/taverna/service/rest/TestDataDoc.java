package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.xml.Data;
import net.sf.taverna.service.xml.DataDocument;
import net.sf.taverna.service.xml.DatasDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
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
import org.w3c.dom.Node;

/**
 * Unit tests for datadoc resource creation and retrieval
 * 
 * @author Stian Soiland
 */

public class TestDataDoc extends ClientTest {

	private Reference justCreated;

	@Before
	public void resetJustCreated() {
		justCreated = null;
	}

	@Test
	public void getAll() throws IOException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/data");
		request.setMethod(Method.GET);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertNotNull(response.getEntity().getText());
	}

	@Test
	public void getAllXML() throws IOException, ParseException, XmlException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/data");
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		DatasDocument doc =
			DatasDocument.Factory.parse(response.getEntity().getStream());
		for (Data data : doc.getDatas().getDataArray()) {
			assertTrue(data.getHref().startsWith(BASE_URL + "data"));
		}
	}

	@Test
	public void create() throws IOException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/data");
		request.setMethod(Method.POST);
		request.setEntity(datadoc, baclavaType);
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		justCreated = response.getRedirectRef();
	}

	@Test
	public void createEmpty() throws IOException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/data");
		request.setMethod(Method.POST);
		request.setEntity("", baclavaType);
		Response response = client.handle(request);
		assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
		assertNull(response.getRedirectRef());
	}

	@Test
	public void createMissing() throws IOException {
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/data");
		request.setMethod(Method.POST);
		Response response = client.handle(request);
		assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
		assertNull(response.getRedirectRef());
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
		assertTrue(response.getStatus().isSuccess());
		assertTrue(restType.includes(response.getEntity().getMediaType()));
		DataDocument doc =
			DataDocument.Factory.parse(response.getEntity().getStream());
		assertEquals(useruri, doc.getData().getOwner().getHref());
		Node baclavaNode = doc.getData().getBaclava().getDomNode();
		baclavaNode.normalize();
		Node a = baclavaNode.getChildNodes().item(1);
		assertEquals("dataThingMap", a.getLocalName());
		assertEquals("http://org.embl.ebi.escience/baclava/0.1alpha",
			a.getNamespaceURI());
		assertEquals(3, a.getChildNodes().getLength());
	}

	@Test
	public void readJustCreatedBaclava() throws IOException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(baclavaType));
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		assertTrue(baclavaType.includes(response.getEntity().getMediaType()));
		assertTrue(response.getEntity().getText().equals(datadoc));
	}

}
