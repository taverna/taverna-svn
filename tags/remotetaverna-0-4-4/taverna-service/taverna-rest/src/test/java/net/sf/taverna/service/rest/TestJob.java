package net.sf.taverna.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.xml.Job;
import net.sf.taverna.service.xml.JobDocument;

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

public class TestJob extends ClientTest {

	private DAOFactory daoFactory = DAOFactory.getFactory();

	private net.sf.taverna.service.datastore.bean.Job job;

	private String jobURI;

	@Test
	public void newJobIsAddedToQueue() {
		WorkflowDAO workflowDao = daoFactory.getWorkflowDAO();
		Workflow w = new Workflow();
		w.setScufl(workflow);
		w.setOwner(user);
		workflowDao.create(w);
		daoFactory.commit();

		String workflowURI = uriFactory.getURI(w);
		JobDocument jobDocument = JobDocument.Factory.newInstance();
		Job job = jobDocument.addNewJob();
		job.addNewWorkflow().setHref(workflowURI);

		Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/jobs");
		request.setMethod(Method.POST);
		request.setEntity(jobDocument.xmlText(), restType);
		Response response = client.handle(request);

		assertEquals("Job was not created", Status.SUCCESS_CREATED,
			response.getStatus());
		Reference ref = response.getRedirectRef();
		assertTrue(ref.toString().startsWith(BASE_URL + "jobs"));
		String id = ref.toString().replaceAll(BASE_URL + "jobs/", "");

		net.sf.taverna.service.datastore.bean.Job jobBean =
			daoFactory.getJobDAO().read(id);
		assertNotNull(jobBean);

		Queue q = daoFactory.getQueueDAO().defaultQueue();
		q = daoFactory.getQueueDAO().refresh(q);
		System.out.println(">>> " + daoFactory.getJobDAO().read(id).getQueue());
		System.out.println(">>> " + daoFactory.getJobDAO().read(id).getStatus());
		assertEquals("The jobs queue should equal the default queue", q,
			jobBean.getQueue());

		assertTrue("The default queue should now contain the new job",
			q.getJobs().contains(jobBean));

		assertEquals("job should be queued status",
			net.sf.taverna.service.datastore.bean.Job.Status.QUEUED,
			jobBean.getStatus());
	}

	@Before
	public void makeJob() throws ParseException {
		Date started = new Date();
		new net.sf.taverna.service.datastore.TestJob().createAndStore();
		job =
			daoFactory.getJobDAO().read(
				net.sf.taverna.service.datastore.TestJob.lastJob);
		// Steal the job so we are allowed to access it
		job.setOwner(daoFactory.getUserDAO().readByUsername(username));
		daoFactory.commit();
		jobURI = uriFactory.getURI(job);
		assertFalse(job.getLastModified().after(new Date()));
		assertFalse(job.getCreated().before(started));
		assertFalse(job.getLastModified().before(job.getCreated()));
	}

	@Test
	public void getJob() throws XmlException, IOException {
		Request request = makeAuthRequest();
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(jobURI);
		Response response = client.handle(request);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertTrue(restType.includes(response.getEntity().getMediaType()));
		JobDocument j =
			JobDocument.Factory.parse(response.getEntity().getStream());
		Job jobElem = j.getJob();
		assertEquals(job.getCreated(), jobElem.getCreated().getTime());
		assertEquals(job.getLastModified(), jobElem.getModified().getTime());
		assertFalse(jobElem.getModified().before(jobElem.getCreated()));

	}

}
