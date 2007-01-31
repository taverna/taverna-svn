package uk.org.mygrid.tavernaservice.wsdl;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import uk.org.mygrid.tavernaservice.TestCommon;
import uk.org.mygrid.tavernaservice.queue.Job.State;
import uk.org.mygrid.tavernaservice.queue.Job;
import uk.org.mygrid.tavernaservice.queue.QueueException;

public class TavernaTest extends TestCommon {

	private Taverna taverna;

	public void setUp() throws IOException {
		super.setUp();
		this.taverna = new Taverna();
	}
	
	public void testRunWorkflow() throws QueueException {
		String job_id = taverna.runWorkflow(workflow, "");
		// Should be a valid, random uuid
		UUID uuid = UUID.fromString(job_id);
		assertEquals(4, uuid.version());
		String status = taverna.jobStatus(job_id);
		// Must be a valid state
		State.valueOf(status);
		// OK, we'll cheat and wait for the job to finish
		Job job = taverna.jobs.get(job_id);
		job.waitForCompletion(1000);
		assertTrue(job.isFinished());
		// Check status and fetch result
		assertEquals("COMPLETED", taverna.jobStatus(job_id));
	}
	
	public void testGetResults() throws QueueException {
		String job_id = taverna.runWorkflow(workflow, "");
		// OK, we'll cheat and wait for the job to finish
		Job job = taverna.jobs.get(job_id);
		job.waitForCompletion(1000);
		assertTrue(job.isFinished());
		ResultBean[] results = taverna.getResults(job_id);
		int expectedResult=0;
		String[] expectedResults = {
				"square red cat",
				"square greenrabbit",
				"circular red cat",
				"circular greenrabbit",
				"triangularred cat",
				"triangulargreenrabbit"
		};
		for (ResultBean result : results) {
			assertEquals("Output", result.getName());
			DataThingBean thing = result.getValue();	
			assertTrue(thing.isCollection());
			for (DataThingBean child : thing.getChildren()) {
				assertTrue(child.isCollection());
				for (DataThingBean grandChild : child.getChildren()) {
					assertFalse(child.isCollection());
					assertEquals(expectedResults[expectedResult++], grandChild.getStringValue());
					assertNull(child.getChildren());
				}
			}
		}
		// Should have traversed all the expected kids
		assertEquals(expectedResults.length, expectedResult);
	}
	
}
