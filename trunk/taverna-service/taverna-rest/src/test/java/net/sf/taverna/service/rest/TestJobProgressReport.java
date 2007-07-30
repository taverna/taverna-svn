package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import net.sf.taverna.service.datastore.TestJob;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.TavernaConstants;

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


public class TestJobProgressReport extends ClientTest {

	public static final MediaType reportType = new MediaType(TavernaConstants.reportType);
	
	private DAOFactory daoFactory = DAOFactory.getFactory();

	String jobURI;

	String progressReportURI;

	Job job;
	
	@Before
	public void makeJob() throws ParseException {
		new TestJob().createAndStore();
		job = daoFactory.getJobDAO().read(TestJob.lastJob);
		// Steal the job so we are allowed to access it
		job.setOwner(daoFactory.getUserDAO().readByUsername(username));
		daoFactory.commit();
		jobURI = uriFactory.getURI(job);
		progressReportURI=uriFactory.getURIReport(job);
		
	}
	
	@Test
	public void getReport() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(progressReportURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(reportType));
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals(TestJob.progressReport, response.getEntity().getText().trim());
	}
	
	@Test
	public void getPlainTextNotAcceptable() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(progressReportURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(MediaType.TEXT_PLAIN));
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
	}
	
	@Test
	public void setReport() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.PUT);
		request.setResourceRef(progressReportURI);
		request.setEntity("<progres>updated</progress>", reportType);
		// Just to be sure we will be changing it since makeJob()
		assertFalse("<progres>updated</progress>".equals(job.getProgressReport()));

		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
		
		// Confirm in database
		job = daoFactory.getJobDAO().refresh(job);
		assertEquals("<progres>updated</progress>", job.getProgressReport());
		
		// GET it to confirm
		request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(progressReportURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(reportType));
		response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals("<progres>updated</progress>", response.getEntity().getText().trim());
	}
	
	@Test
	public void putPlainTextUnsupported() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.PUT);
		request.setResourceRef(progressReportURI);
		String PLAIN = "some plain text";
		request.setEntity(PLAIN, MediaType.TEXT_PLAIN);
		// Just to be sure we will be changing it since makeJob()
		job = daoFactory.getJobDAO().refresh(job);
		assertFalse(PLAIN.equals(job.getProgressReport()));

		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
		job = daoFactory.getJobDAO().refresh(job);
		assertFalse(PLAIN.equals(job.getProgressReport()));
	}
}
