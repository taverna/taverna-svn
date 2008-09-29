package net.sf.taverna.service.rest.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import net.sf.taverna.service.xml.StatusType;

import org.junit.Before;
import org.junit.Test;

public class TestJobs extends ContextTest {
	
	WorkflowREST wf;

	private JobREST job;

	private DataREST data;
	
	@Before
	public void makeWorkflow() throws NotSuccessException {
		wf = null;
		TestWorkflows testWorkflows = new TestWorkflows();
		testWorkflows.context = context;
		testWorkflows.user = user;
		testWorkflows.uploadWorkflow();
		wf = TestWorkflows.wf;
	}
	
	@Before
	public void makeData() throws NotSuccessException {
		// TODO: Create TestDatas unit tests
		data = user.getDatas().add(datadoc);
	}
	
	
	@Test
	public void uploadJob() throws RESTException {
		job = null;
		JobsREST jobs = user.getJobs();
		job = jobs.add(wf, data);
		assertEquals(wf, job.getWorkflow());
		assertEquals(user, job.getOwner());
		assertEquals(StatusType.QUEUED, job.getStatus());
	}

	@Test
	public void setStatus() throws RESTException {
		if (job == null) {
			uploadJob();
		}
		job.setStatus(StatusType.INITIALISING);
	}
	
	@Test
	public void getInput() throws RESTException, IOException {
		if (job == null) {
			uploadJob();
		}
		DataREST inputs = job.getInputs();
		String baclava = inputs.getBaclava();
	}
	
}
