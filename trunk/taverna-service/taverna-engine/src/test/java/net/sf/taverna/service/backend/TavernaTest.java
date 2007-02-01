package net.sf.taverna.service.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.sf.taverna.service.TestCommon;
import net.sf.taverna.service.queue.Job;
import net.sf.taverna.service.queue.QueueException;
import net.sf.taverna.service.queue.TavernaQueue;
import net.sf.taverna.service.queue.Job.State;

import org.embl.ebi.escience.baclava.DataThing;
import org.jdom.JDOMException;


public class TavernaTest extends TestCommon {

	private Engine engine;

	public void setUp() throws IOException {
		super.setUp();
		this.engine = Engine.getInstance();
	}
	
	public void testRunWorkflow() throws QueueException, IOException {
		String job_id = engine.runWorkflow(workflow, "");
		// Should be a valid, random uuid
		UUID uuid = UUID.fromString(job_id);
		assertEquals(4, uuid.version());
		String status = engine.jobStatus(job_id);
		// Must be a valid state
		State.valueOf(status);
		// OK, we'll cheat and wait for the job to finish
		Job job = engine.jobs.get(job_id);
		job.waitForCompletion(1000);
		assertTrue(job.isFinished());
		// Check status and fetch result
		assertEquals("COMPLETED", engine.jobStatus(job_id));
	}
	
	@SuppressWarnings("unchecked")
	public void testGetResults() throws QueueException, JDOMException, IOException {
		String job_id = engine.runWorkflow(workflow, "");
		// OK, we'll cheat and wait for the job to finish
		Job job = engine.jobs.get(job_id);
		job.waitForCompletion(1000);
		assertTrue(job.isFinished());
		String resultDoc = engine.getResultDocument(job_id);
		Map<String, DataThing> results = TavernaQueue.parseDataDoc(resultDoc);
				
		int expectedResult=0;
		String[] expectedResults = {
				"square red cat",
				"square greenrabbit",
				"circular red cat",
				"circular greenrabbit",
				"triangularred cat",
				"triangulargreenrabbit"
		};
		for (Entry<String, DataThing> item : results.entrySet()) {
			assertEquals("Output", item.getKey());
			DataThing thing = item.getValue();	
			assertTrue(thing.getDataObject() instanceof Collection);
			Collection<Collection> children = (Collection)thing.getDataObject();
			for (Collection<String> child : children) {
				for (String grandChild : child) {
					assertEquals(expectedResults[expectedResult++], 
						grandChild);
				}
			}
		}
		// Should have traversed all the expected kids
		assertEquals(expectedResults.length, expectedResult);
	}
	
}
