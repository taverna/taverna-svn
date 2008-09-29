package net.sf.taverna.service.datastore;

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
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.test.TestDAO;

import org.junit.Test;

public class TestQueue extends TestDAO {

	final int JOBS=100;
	
	public Job makeJob() throws ParseException {
		TestJobBean jobTest = new TestJobBean();
		jobTest.createAndStore();
		return daoFactory.getJobDAO().read(TestJobBean.lastJob);
	}

	@Test
	public void createQueue() throws InterruptedException, ParseException {
		Job job = makeJob();
		Queue q = new Queue();
		assertEquals(0, q.getJobs().size());
		// not getJobs().add()
		q.addJob(job);
		assertEquals(1, q.getJobs().size());
		daoFactory.getQueueDAO().create(q);
		Queue q2 = altFactory.getQueueDAO().read(q.getId());
		assertNull(q2);
		daoFactory.commit();
		q2 = altFactory.getQueueDAO().read(q.getId());
		assertNotNull(q2);
		assertNotSame(q, q2);
		assertEquals(1, q2.getJobs().size());
		// Requires Job.equals() to work
		assertTrue(q2.getJobs().contains(job));
	}
	
	public Queue makeBigQueue() throws ParseException {
		List<Job> jobs = new ArrayList<Job>();
		for (int i=0; i<JOBS; i++) {
			jobs.add(makeJob());
		}
		Queue q = new Queue();
		daoFactory.getQueueDAO().create(q);
		for (Job j: jobs) {
			q.addJob(j);
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

		Queue q2 = altFactory.getQueueDAO().read(q.getId());
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
	public void removeJob() throws ParseException {
		Queue q = makeBigQueue();
		List<Job> jobs = q.getJobs();
		// Let's remove the first and last job
		q.removeJob(jobs.get(0));
		q.removeJob(jobs.get(JOBS-1));
		daoFactory.getQueueDAO().update(q);
		daoFactory.commit();
		q = daoFactory.getQueueDAO().read(q.getId());
		assertEquals(JOBS-2, q.getJobs().size());
		
		Queue q2 = altFactory.getQueueDAO().read(q.getId());
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
		q.removeJob(firstJob);
		q.addJob(firstJob);
		
		daoFactory.getQueueDAO().update(q);
		daoFactory.commit();
		q = daoFactory.getQueueDAO().read(q.getId());
		assertEquals(JOBS, q.getJobs().size());		
		Queue q2 = altFactory.getQueueDAO().read(q.getId());
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
