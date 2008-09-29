package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.TavernaService;
import net.sf.taverna.service.util.XMLUtils;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Unit tests for datadoc resource creation and retrieval
 * 
 * @author Stian Soiland
 *
 */

public class TestDataDoc extends ClientTest {
	
	private static Reference justCreated;

	@BeforeClass
	public static void resetJustCreated() {
		justCreated = null;
	}

	@Ignore
	@Test
	public void getAll() throws IOException {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(BASE_URL + "data");
		request.setMethod(Method.GET);
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		//response.getEntity().write(System.out);
	}

	@Ignore
	@Test
	public void getAllXML() throws IOException, ParseException {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(BASE_URL + "data");
		request.setMethod(Method.GET);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		Document doc = XMLUtils.parseXML(response.getEntity().getStream());
		Element root = doc.getRootElement();
		assertEquals(TavernaService.NS, root.getNamespaceURI());
		assertEquals("data", root.getName());
		List<Element> children = root.getChildren();
		for (Element child : children) {
			assertEquals(TavernaService.NS, child.getNamespaceURI());
			assertEquals("datadoc", child.getName());
			Attribute xlink = child.getAttribute("href", 
				Namespace.getNamespace("http://www.w3.org/1999/xlink"));
			assertNotNull("Didn't have xlink:href", xlink);
			assertTrue(xlink.getValue().startsWith("/data"));
		}
		
	}

	@Ignore
	@Test
	public void create() throws IOException {
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(BASE_URL + "data");
		request.setMethod(Method.POST);
		request.setEntity(datadoc, baclavaType);
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		justCreated = response.getRedirectRef();
	}

	@Ignore
	@Test
	public void readJustCreated() throws IOException {
		if (justCreated == null) {
			create();
		}
		assertNotNull("create() must be run first", justCreated);
		Request request = new Request();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(justCreated);
		request.setMethod(Method.GET);
		Response response = client.handle(request);
		assertTrue(response.getStatus().isSuccess());
		String result = response.getEntity().getText();
		// Should do text/plain as fallback
		assertTrue(MediaType.TEXT_PLAIN.includes(response.getEntity().getMediaType()));
		assertTrue(result.startsWith("Data document "));
	}

	@Ignore
	@Test
	public void readJustCreatedXML() throws IOException {
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
		assertTrue(response.getStatus().isSuccess());
		assertTrue(restType.includes(response.getEntity().getMediaType()));
		assertTrue(response.getEntity().getText().equals(datadoc));
	}

}
