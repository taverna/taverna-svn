package net.sf.taverna.service.datastore;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.persistence.PersistenceException;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.QueueDAO;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.datastore.dao.WorkerDAO;
import net.sf.taverna.service.test.TestDAO;

import org.junit.Test;

public class TestWorker extends TestDAO {

	@Test
	public void makeWorker() {
		WorkerDAO workerDao = daoFactory.getWorkerDAO();
		Worker worker = new Worker();
		worker.setPassword(User.generatePassword());
		workerDao.create(worker);
		daoFactory.commit();
	}

	@Test(expected = PersistenceException.class)
	public void makeInvalidWorker() {
		WorkerDAO workerDao = daoFactory.getWorkerDAO();
		Worker worker = new Worker();
		worker.setApiURI("urn:uuid:" + UUID.randomUUID());
		workerDao.create(worker);
		daoFactory.commit();
	}
	
	@Test
	public void retrieveWorkerAsUser() {
		WorkerDAO workerDao = daoFactory.getWorkerDAO();
		Worker worker = new Worker();
		worker.setPassword(User.generatePassword());
		workerDao.create(worker);
		daoFactory.commit();
		UserDAO userDao = altFactory.getUserDAO();
		User workerAsUser = userDao.read(worker.getId());
		assertTrue(workerAsUser instanceof Worker);
	}
	
	@Test 
	public void assocateWorkerWithAQueue() {
		WorkerDAO workerDao = daoFactory.getWorkerDAO();
		QueueDAO queueDao = daoFactory.getQueueDAO();
		
		Worker worker = new Worker();
		worker.setPassword(User.generatePassword());
		workerDao.create(worker);
		
		Queue queue = new Queue();
		queueDao.create(queue);
		worker.setQueue(queue);
		
		workerDao.update(worker);
		
		Worker readWorker = workerDao.read(worker.getId());
		
		assertEquals("There queue should match",queue.getId(),readWorker.getQueue().getId());
	}
	
	@Test
	public void testAssignJobToWorker() throws Exception {
		WorkerDAO workerDAO = daoFactory.getWorkerDAO();
		JobDAO jobDAO = daoFactory.getJobDAO();
		
		Worker worker = new Worker();
		worker.setPassword(User.generatePassword());
		workerDAO.create(worker);
		
		new TestJob().createAndStore();
		Job job = jobDAO.read(TestJob.lastJob);
		job.setStatus(Status.QUEUED);
		jobDAO.create(job);
		
		worker.assignJob(job);
		workerDAO.update(worker);
		jobDAO.update(job);
		
		Job job2 = jobDAO.read(job.getId());
		
		assertNotNull(job2);
		assertEquals("Status should now be DEQUEUED",job.getStatus(),Status.DEQUEUED);
		
		Worker worker2 = workerDAO.read(worker.getId());
		assertNotNull(worker2);
		assertEquals("There should be 1 job assigned to the worker",1,worker.getJobs().size());
		
		
	}

}
