package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import net.sf.taverna.service.datastore.bean.AbstractOwned;
import net.sf.taverna.service.datastore.bean.AbstractUUID;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;

public class TestURIFactory extends ClientTest {
	private static Logger logger = Logger.getLogger(TestURIFactory.class);
	
	@Test
	public void applicationRoot() {
		assertEquals(BASE_URL, uriFactory.getApplicationRoot().toString());
	}
	
	@Test
	public void htmlRoot() {
		assertEquals(ROOT_URL + "html", uriFactory.getHTMLRoot());
	}
	
	private void testURIClass(Class <? extends AbstractUUID> resourceClass, String path) {
		assertEquals(BASE_URL + path, uriFactory.getURI(resourceClass));
	}

	
	@Test
	public void getURIResource() {
		testURIResource(new Job(), "jobs");
		testURIResource(new Workflow(), "workflows");
		testURIResource(new DataDoc(), "data");
		testURIResource(new Queue(), "queues");
		testURIResource(new Worker(), "workers");		
	}

	@Test
	public void getURIUser() {
		User u = new User("soup");
		assertEquals(BASE_URL + "users/soup" , uriFactory.getURI(u));
	}
	
	
	private void testURIResource(AbstractUUID resource, String path) {
		assertEquals(BASE_URL + path + "/" + resource.getId() , uriFactory.getURI(resource));
	}

	@Test
	public void getURIClass() {
		testURIClass(Job.class, "jobs");
		testURIClass(Workflow.class, "workflows");
		testURIClass(DataDoc.class, "data");
		testURIClass(User.class, "users");
		testURIClass(Queue.class, "queues");
		testURIClass(Worker.class, "workers");	
	}

	private void testURIOwned(Class <? extends AbstractOwned> resourceClass, String path) {
		assertEquals(BASE_URL + "users/" + username + "/" + path, uriFactory.getURI(user, resourceClass));
	}
	
	@Test
	public void getURIOwner() {
		testURIOwned(Job.class, "jobs");
		testURIOwned(Workflow.class, "workflows");
		testURIOwned(DataDoc.class, "data");
	}
	
	@Test
	public void currentUser() {
		assertEquals(BASE_URL + "users;current", uriFactory.getURICurrentUser());
	}
	
	@Test
	public void jobReport() {
		Job j = new Job();
		assertEquals(BASE_URL + "jobs/" + j.getId() + "/report", uriFactory.getURIReport(j));
	}

	@Test
	public void jobStatus() {
		Job j = new Job();
		assertEquals(BASE_URL + "jobs/" + j.getId() + "/status", uriFactory.getURIStatus(j));
	}

	@Test
	public void getMapping() {
		assertEquals("jobs", URIFactory.getMapping(Job.class));
		assertEquals("workflows", URIFactory.getMapping(Workflow.class));
		assertEquals("data", URIFactory.getMapping(DataDoc.class));
		assertEquals("users", URIFactory.getMapping(User.class));
		assertEquals("queues", URIFactory.getMapping(Queue.class));
		assertEquals("workers", URIFactory.getMapping(Worker.class));
	}
	
	@Test
	public void getMappingCurrentUser() {
		assertEquals(";current", URIFactory.getMappingCurrentUser());
	}
	
	@Test
	public void getMappingCurrentReport() {
		assertEquals("/report", URIFactory.getMappingReport());
	}

	@Test
	public void getMappingCurrentStatus() {
		assertEquals("/status", URIFactory.getMappingStatus());
	}

	@Test
	public void getInstancePath() {
		String URL = "http://www.example.com/blah/v1/";
		Job j = new Job();
		URIFactory blah = URIFactory.getInstance(URL);	
		assertEquals(URL + "jobs/" + j.getId(), blah.getURI(j));
	}
	
	@Test
	public void getInstanceReference() {
		// Should be getTarget-ed to /fish/bowl/
		Reference ref = new Reference(new Reference("http://example.com/fish/soup"), "bowl/");
		assertEquals("http://example.com/fish/bowl/", ref.getTargetRef().toString());
		Request req = new Request(Method.GET, "http://example.com/somewhere/else");
		req.setRootRef(ref);
		URIFactory f = URIFactory.getInstance(req);
		assertTrue(f.getApplicationRoot().isEquivalentTo(ref));
		assertEquals("http://example.com/fish/bowl/users;current", f.getURICurrentUser());
		// Because it's ../html from fish/bowl/
		assertEquals("http://example.com/fish/html", f.getHTMLRoot());

	}
}
