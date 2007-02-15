package net.sf.taverna.service.queue;

import java.util.Date;
import java.util.Map;

import net.sf.taverna.service.TestCommon;
import net.sf.taverna.service.interfaces.QueueException;
import net.sf.taverna.service.queue.Job.State;

import org.embl.ebi.escience.baclava.DataThing;



public class TavernaQueueTest extends TestCommon {
	
	public void testCreateQueue() {
		TavernaQueue queue = new TavernaQueue();
		assertEquals(0, queue.size());			
	}
	
	public void testAddQueue() throws InterruptedException, QueueException {
		TavernaQueue queue = new TavernaQueue();
		Date before = new Date();
		Thread.sleep(5);
		Job job = queue.add(workflow, "");
		assertEquals(1, queue.size());
		assertEquals(job, queue.peek());
		// Check timestamp
		assertTrue(job.created.after(before));
		Thread.sleep(5);
		assertTrue(job.created.before(new Date()));
		// And that it was parsed..
		assertEquals(8, job.workflow.getProcessors().length);
		assertEquals(1, job.workflow.getWorkflowSinkPorts().length);
		assertEquals(State.QUEUED, job.getState());
	}
	
	public void testAddNonWorkflow() {
		TavernaQueue queue = new TavernaQueue();
		try {
			queue.add("Not a workflow", "");
			fail("Did not throw InvalidWorkflowException");
		} catch (QueueException ex) {
			// Expected
		}
	}
	
	public void testAddNonInputDoc() {
		TavernaQueue queue = new TavernaQueue();
		try {
			queue.add(workflow, "Not an input document");
			fail("Did not throw InvalidWorkflowException");
		} catch (QueueException ex) {
			// Expected
		}
	}
		
	public void testPoll() throws QueueException {
		TavernaQueue queue = new TavernaQueue();
		queue.add(workflow, "");
		Job job = queue.poll();
		assertEquals(State.DEQUEUED, job.getState());
	}
	
	public void testAddAndPollMany() throws QueueException {
		TavernaQueue queue = new TavernaQueue();
		Job job1 = queue.add(workflow, "");
		Job job2 = queue.add(workflow, "");					
		assertNotSame(job1, job2);
		assertSame(job1, queue.poll());
		Job job3 = queue.add(workflow, "");
		assertSame(job2, queue.poll());
		assertSame(job3, queue.poll());
		assertNull(queue.poll());
	}

	
	public void testQueueListener() throws QueueException {
		TavernaQueue queue = new TavernaQueue();		
		final StringBuffer progress = new StringBuffer();
		QueueListener listener = new QueueListener(queue){						
			@Override
			void execute(Job job) {
				// Indicate that we have run
				progress.append("Executed.");
			}							
		};
		assertEquals("", progress.toString());
		new Thread(listener).start();
		Job job;
		
		job = queue.add(workflow, "");		
		// We can't check if it is queued or started or so.. because there will be lots
		// of timing issues. 
		
		// But we can give it a chance to finish
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// ignore
		}
		// And it should not fail
		assertNotSame(State.FAILED, job.getState());
		assertNotSame(State.CANCELLED, job.getState());
		if (job.getState().equals(State.COMPLETE)) {
			assertEquals("Executed.", progress.toString());
		}		
	}
	
	public void testTavernaQueue() throws QueueException {
		TavernaQueue queue = new TavernaQueue();
		QueueListener listener = new TavernaQueueListener(queue);
		new Thread(listener).start();
		Job job = queue.add(workflow, "");
		State state = job.waitForCompletion(2000);
		assertEquals(State.COMPLETE, state);
		Map<String, DataThing> result = job.getResults();
		DataThing thing = result.get("Output");
		assertEquals("[[square red cat, square greenrabbit], [circular red cat, circular greenrabbit], [triangularred cat, triangulargreenrabbit]]",
			thing.getDataObject().toString());
	}
}
