package net.sf.taverna.service.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Map;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.QueueException;
import net.sf.taverna.service.test.EngineTest;
import net.sf.taverna.service.util.XMLUtils;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.junit.Test;



public class TavernaQueueTest extends EngineTest {
	
	private DAOFactory daoFactory = DAOFactory.getFactory();
	
	@Test
	public void createQueue() {
		TavernaQueue queue = new TavernaQueue();
		assertEquals(0, queue.size());			
	}
	
	@Test
	public void addQueue() throws InterruptedException, QueueException, ParseException {
		TavernaQueue queue = new TavernaQueue();
		Date before = new Date();
		Thread.sleep(5);
		Job job = queue.add(workflow, "");
		daoFactory.getJobDAO().create(job);
		assertEquals(1, queue.size());
		assertEquals(job, queue.peek());
		// Check timestamp
		assertTrue(job.getCreated().after(before));
		Thread.sleep(5);
		assertTrue(job.getCreated().before(new Date()));
		// And that it was parsed..
		ScuflModel workflow = XMLUtils.parseXScufl(job.getWorkflow().getScufl());
		assertEquals(8, workflow.getProcessors().length);
		assertEquals(1, workflow.getWorkflowSinkPorts().length);
		assertEquals(Status.QUEUED, job.getStatus());
	}
	
	@Test
	public void addNonWorkflow() throws ParseException {
		TavernaQueue queue = new TavernaQueue();
		try {
			queue.add("Not a workflow", "");
			fail("Did not throw InvalidWorkflowException");
		} catch (QueueException ex) {
			// Expected
		}
	}
	
	@Test
	public void addNonInputDoc() throws ParseException {
		TavernaQueue queue = new TavernaQueue();
		try {
			queue.add(workflow, "Not an input document");
			fail("Did not throw InvalidWorkflowException");
		} catch (QueueException ex) {
			// Expected
		}
	}
		
	@Test
	public void poll() throws QueueException, ParseException {
		TavernaQueue queue = new TavernaQueue();
		queue.add(workflow, "");
		Job job = queue.poll();
		assertEquals(Status.DEQUEUED, job.getStatus());
	}
	
	@Test
	public void addAndPollMany() throws QueueException, ParseException {
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

	
	public void testQueueListener() throws QueueException, ParseException {
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
		assertNotSame(Status.FAILED, job.getStatus());
		assertNotSame(Status.CANCELLED, job.getStatus());
		if (job.getStatus().equals(Status.COMPLETE)) {
			assertEquals("Executed.", progress.toString());
		}		
	}
	
	public void testTavernaQueue() throws QueueException, ParseException {
		JobDAO jobDao = daoFactory.getJobDAO();
		TavernaQueue queue = new TavernaQueue();
		QueueListener listener = new TavernaQueueListener(queue);
		new Thread(listener).start();
		Job job = queue.add(workflow, "");
		jobDao.create(job);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		jobDao.refresh(job);
		assertEquals(Status.COMPLETE, job.getStatus());
		Map<String, DataThing> result = job.getOutputDoc().getDataMap();
		DataThing thing = result.get("Output");
		assertEquals("[[square red cat, square greenrabbit], [circular red cat, circular greenrabbit], [triangularred cat, triangulargreenrabbit]]",
			thing.getDataObject().toString());
	}
}
