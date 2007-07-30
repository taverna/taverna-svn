package net.sf.taverna.service.datastore;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.QueueDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.test.TestDAO;

import org.junit.Test;

public class TestQueue extends TestDAO {

	final int JOBS=100;
	
	public Job makeJob() throws ParseException {
		TestJob jobTest = new TestJob();
		jobTest.createAndStore();
		return daoFactory.getJobDAO().read(TestJob.lastJob);
	}

	@Test
	public void createQueue() throws InterruptedException, ParseException {
		Job job = makeJob();
		Queue q = new Queue();
		daoFactory.getQueueDAO().create(q);
		assertEquals(0, q.getJobs().size());
		// not getJobs().add()
		daoFactory.getQueueEntryDAO().create(q.addJob(job));
		assertEquals(1, q.getJobs().size());
		daoFactory.getQueueDAO().create(q);
		Queue q2 = altFactory.getQueueDAO().reread(q);
		assertNull(q2);
		daoFactory.commit();
		q2 = altFactory.getQueueDAO().reread(q);
		assertNotNull(q2);
		assertNotSame(q, q2);
		assertEquals(1, q2.getJobs().size());
		// Requires Job.equals() to work
		assertTrue(q2.getJobs().contains(job));
	}
	
	@Test
	public void defaultQueueIsCreated() {
		deleteDefaultQueue(); //delete existing default queue
		
		QueueDAO queueDao = daoFactory.getQueueDAO();
		Queue q = queueDao.defaultQueue();
		assertNotNull("The default queue should not be null", q);

		assertNotNull("Default queue should now exist",
			queueDao.reread(q));

		assertSame(q, queueDao.defaultQueue());
		daoFactory.rollback();
	}
	
	private void deleteDefaultQueue() {
		QueueDAO queueDao = daoFactory.getQueueDAO();
		Queue defaultQueue = queueDao.defaultQueue();
		for (Job job : defaultQueue.getJobs()) {
			QueueEntry entry = defaultQueue.removeJob(job);
			daoFactory.getQueueEntryDAO().delete(entry);
		}
		for (Worker worker : defaultQueue.getWorkers()) {
			worker.setQueue(null);
		}
		queueDao.delete(defaultQueue);
		assertNull("Default queue should have been deleted",
			queueDao.reread(defaultQueue));
	}
	
	public Queue makeBigQueue() throws ParseException {
		List<Job> jobs = new ArrayList<Job>();
		for (int i=0; i<JOBS; i++) {
			jobs.add(makeJob());
		}
		Queue q = new Queue();
		daoFactory.getQueueDAO().create(q);
		for (Job j: jobs) {
			daoFactory.getQueueEntryDAO().create(q.addJob(j));
		}
		daoFactory.getQueueDAO().update(q);
		return q;
	}
	
	@Test
	public void jobOrder() throws ParseException, InterruptedException {
		Queue q = makeBigQueue();
		List<Job> jobs = q.getJobs();
		assertEquals(JOBS, jobs.size());
		daoFactory.commit();

		Queue q2 = altFactory.getQueueDAO().reread(q);
		assertEquals(JOBS, q2.getJobs().size());
		
		// OK, check that all of them are there
		assertTrue(q2.getJobs().containsAll(jobs));
		
		// Check the order
		Iterator<Job> jobIterator = jobs.iterator();
		for (Job readJob : q2.getJobs()) {
			Job origJob = jobIterator.next();
			assertEquals(origJob, readJob);
			assertNotSame(origJob, readJob);
		}
	}
	
	@Test
	public void testDeleteQueue() throws ParseException {
		QueueDAO queueDao = daoFactory.getQueueDAO();
		int size = queueDao.all().size();
		Queue q = new Queue();
		queueDao.create(q);
		assertEquals("Size should have increased by 1",size+1,queueDao.all().size());
		
		queueDao.delete(q);
		assertEquals("Size should be back to the original size",size,queueDao.all().size());
		daoFactory.rollback();
	}
	
	@Test
	public void removeJob() throws ParseException {
		Queue q = makeBigQueue();
		List<Job> jobs = q.getJobs();
		// Let's remove the first and last job
		daoFactory.getQueueEntryDAO().delete(q.removeJob(jobs.get(0)));
		daoFactory.getQueueEntryDAO().delete(q.removeJob(jobs.get(JOBS-1)));
		daoFactory.getQueueDAO().update(q);
		daoFactory.commit();
		q = daoFactory.getQueueDAO().reread(q);
		assertEquals(JOBS-2, q.getJobs().size());
		
		Queue q2 = altFactory.getQueueDAO().reread(q);
		assertEquals(JOBS-2, q2.getJobs().size());
		// Check the order
		Iterator<Job> jobIterator = jobs.iterator();
		assertEquals(jobs.get(0), jobIterator.next()); // skip #0
		for (Job readJob : q2.getJobs()) {
			Job origJob = jobIterator.next();
			assertEquals(origJob, readJob);
			assertNotSame(origJob, readJob);
		}
		// And the last one left
		assertEquals(jobs.get(JOBS-1), jobIterator.next());
	}
	
	
	@Test
	public void removeAndInsertJob() throws ParseException {
		Queue q = makeBigQueue();
		List<Job> jobs = q.getJobs();
		Job firstJob = jobs.get(0);
		// More difficult, let's remove the first and last job
		daoFactory.getQueueEntryDAO().delete(q.removeJob(firstJob));
		daoFactory.getQueueEntryDAO().create(q.addJob(firstJob));
		
		daoFactory.getQueueDAO().update(q);
		daoFactory.commit();
		q = daoFactory.getQueueDAO().reread(q);
		assertEquals(JOBS, q.getJobs().size());		
		Queue q2 = altFactory.getQueueDAO().reread(q);
		assertEquals(JOBS, q2.getJobs().size());
		
		// Check the order
		// Modify our List<Job> to match in the test
		jobs.remove(0);
		jobs.add(firstJob);
		Iterator<Job> jobIterator = jobs.iterator();
		for (Job readJob : q2.getJobs()) {
			Job origJob = jobIterator.next();
			assertEquals(origJob, readJob);
			assertNotSame(origJob, readJob);
		}
	}
}
