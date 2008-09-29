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

public class TestJobConsole extends ClientTest {

	private static final String THE_INITIAL_CONSOLE = "The initial\nconsole";

	private static final String THIS_IS_THE_CONSOLE = "This is\nthe\nconsole";

	public static final MediaType consoleType =
		new MediaType(TavernaConstants.consoleType);

	private DAOFactory daoFactory = DAOFactory.getFactory();

	String jobURI;

	String consoleURI;

	Job job;

	@Before
	public void makeJob() throws ParseException {
		new TestJob().createAndStore();
		job = daoFactory.getJobDAO().read(TestJob.lastJob);
		// Steal the job so we are allowed to access it
		job.setOwner(daoFactory.getUserDAO().readByUsername(username));
		job.setConsole(THE_INITIAL_CONSOLE);
		daoFactory.commit();
		jobURI = uriFactory.getURI(job);
		consoleURI = uriFactory.getURIConsole(job);

	}

	@Test
	public void getConsole() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(consoleURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(consoleType));
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals(THE_INITIAL_CONSOLE, response.getEntity().getText().trim());
	}

	@Test
	public void setReport() throws IOException {
		Request request = makeAuthRequest();
		request.setMethod(Method.PUT);
		request.setResourceRef(consoleURI);
		request.setEntity(THIS_IS_THE_CONSOLE, consoleType);
		assertFalse(THIS_IS_THE_CONSOLE.equals(job.getConsole()));

		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());

		// Confirm in database
		job = daoFactory.getJobDAO().refresh(job);
		assertEquals(THIS_IS_THE_CONSOLE, job.getConsole());

		// GET it to confirm
		request = makeAuthRequest();
		request.setMethod(Method.GET);
		request.setResourceRef(consoleURI);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(consoleType));
		response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals(THIS_IS_THE_CONSOLE, response.getEntity().getText());
	}

}
