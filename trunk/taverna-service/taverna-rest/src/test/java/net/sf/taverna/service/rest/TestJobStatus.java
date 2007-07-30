package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import net.sf.taverna.service.datastore.TestJob;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.ParseException;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;


public class TestJobStatus extends ClientTest {

	private DAOFactory daoFactory = DAOFactory.getFactory();

	String jobURI;

	String statusURI;

	Job job;
	
	@Before
	public void makeJob() throws ParseException {
		new TestJob().createAndStore();
		job = daoFactory.getJobDAO().read(TestJob.lastJob);
		// Steal the job so we are allowed to access it
		job.setOwner(daoFactory.getUserDAO().readByUsername(username));
		daoFactory.commit();
		jobURI = uriFactory.getURI(job);
		statusURI = uriFactory.getURIStatus(job);
	}
	
	@Test
	public void getStatus() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(statusURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(MediaType.TEXT_PLAIN));
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals(TestJob.status.name(), response.getEntity().getText().trim());
	}
	
	@Test
	public void setStatus() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.PUT);
		request.setResourceRef(statusURI);
		request.setEntity("COMPLETE", MediaType.TEXT_PLAIN);
		// Just to be sure we will be changing it since makeJob()
		assertFalse(Job.Status.COMPLETE.equals(job.getStatus()));

		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
		
		// Confirm in database
		job = daoFactory.getJobDAO().refresh(job);
		assertEquals(Job.Status.COMPLETE, job.getStatus());
		
		// GET it to confirm
		request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(statusURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(MediaType.TEXT_PLAIN));
		response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals("COMPLETE", response.getEntity().getText().trim());
	}
	
	
}
