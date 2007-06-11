package net.sf.taverna.service.rest.client;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.service.xml.StatusType;

import org.junit.Before;
import org.junit.Test;

public class TestJobs extends ContextTest {
	
	WorkflowREST wf;

	private JobREST job;
	
	@Before
	public void makeWorkflow() throws NotSuccessException {
		wf = null;
		TestWorkflows testWorkflows = new TestWorkflows();
		testWorkflows.context = context;
		testWorkflows.user = user;
		testWorkflows.uploadWorkflow();
		wf = TestWorkflows.wf;
	}
	
	
	@Test
	public void uploadJob() throws RESTException {
		job = null;
		JobsREST jobs = user.getJobs();
		job = jobs.add(wf);
		assertEquals(wf, job.getWorkflow());
		assertEquals(user, job.getOwner());
		assertEquals(StatusType.NEW, job.getStatus());
	}

	@Test
	public void setStatus() throws RESTException {
		if (job == null) {
			uploadJob();
		}
		job.setStatus(StatusType.QUEUED);
	} 
}
