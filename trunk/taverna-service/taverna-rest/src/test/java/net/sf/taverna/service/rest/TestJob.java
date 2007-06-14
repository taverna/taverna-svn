package net.sf.taverna.service.rest;

import javax.print.attribute.standard.JobName;

import org.junit.Test;
import static org.junit.Assert.*;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.xml.Job;
import net.sf.taverna.service.xml.JobDocument;

public class TestJob extends ClientTest {

	private DAOFactory daoFactory = DAOFactory.getFactory();
	
	@Test
	public void newJobIsAddedToQueue() {
		WorkflowDAO workflowDao = daoFactory.getWorkflowDAO();
		Workflow w = new Workflow();
    	w.setScufl(workflow);
    	w.setOwner(user);
    	workflowDao.create(w);
    	daoFactory.commit();
    	
    	String workflowURI=URIFactory.getInstance().getURI(w);
    	JobDocument jobDocument = JobDocument.Factory.newInstance();
    	Job job=jobDocument.addNewJob();
    	job.addNewWorkflow().setHref(workflowURI);
    	
    	Request request = makeAuthRequest();
		Client client = new Client(Protocol.HTTP);
		request.setResourceRef(useruri + "/jobs");
		request.setMethod(Method.POST);
		request.getClientInfo().getAcceptedMediaTypes().add(
			new Preference<MediaType>(restType));
		request.setEntity(jobDocument.xmlText(), restType);
		Response response = client.handle(request);
		
		assertEquals(Status.SUCCESS_CREATED,response.getStatus());
		Reference ref=response.getRedirectRef();
		assertTrue(ref.toString().startsWith(BASE_URL+"jobs"));
		String id = ref.toString().replaceAll(BASE_URL+"jobs/", "");
		
		net.sf.taverna.service.datastore.bean.Job jobBean = daoFactory.getJobDAO().read(id);
		assertNotNull(jobBean);
		
		Queue q = daoFactory.getQueueDAO().defaultQueue();
		assertTrue(q.getJobs().contains(jobBean));
	}
}
