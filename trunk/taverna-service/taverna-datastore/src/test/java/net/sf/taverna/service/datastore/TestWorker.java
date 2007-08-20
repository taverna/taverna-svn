package net.sf.taverna.service.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

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
		worker.setPassword("ABCDE");
		workerDao.create(worker);
		daoFactory.commit();
		worker=daoFactory.getWorkerDAO().reread(worker);
		assertEquals("ABCDE",worker.getWorkerPasswordStr());
		assertTrue(worker.checkPassword("ABCDE"));
	}

	@Test(expected = javax.persistence.PersistenceException.class)
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
		
		Worker readWorker = workerDao.reread(worker);
		
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
		
		Job job2 = jobDAO.reread(job);
		
		assertNotNull(job2);
		assertEquals("Status should now be " + Status.INITIALISING, job
				.getStatus(), Status.INITIALISING);
		
		Worker worker2 = workerDAO.reread(worker);
		assertNotNull(worker2);
		assertEquals("There should be 1 job assigned to the worker", 1,
			worker.getWorkerJobs().size());
	}
	
	@Test 
	public void testUnAssignJobs() throws Exception {
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
		job.setStatus(Status.COMPLETE);
		jobDAO.update(job);
		
		List<Job> removed=worker.unassignJobs();
		for (Job removedJob : removed) jobDAO.update(removedJob);
		
		Job job2=jobDAO.reread(job);
		assertTrue(job2.getWorker()==null);
		assertEquals(0,worker.getWorkerJobs().size());
		
	}
	
	@Test
	public void testIsBusy() throws Exception {
		WorkerDAO workerDAO = daoFactory.getWorkerDAO();
		JobDAO jobDAO = daoFactory.getJobDAO();
		
		Worker worker = new Worker();
		worker.setPassword(User.generatePassword());
		workerDAO.create(worker);
		
		new TestJob().createAndStore();
		Job job = jobDAO.read(TestJob.lastJob);
		job.setStatus(Status.QUEUED);
		jobDAO.create(job);
		
		assertFalse("Worker hasn't started yet so shouldn't be busy",worker.isBusy());
		worker.assignJob(job);
		workerDAO.update(worker);
		jobDAO.update(job);
		
		assertTrue("Worker should be busy now on the assigned job",worker.isBusy());
		assertTrue("Looked up worker should be busy now on the assigned job",workerDAO.reread(worker).isBusy());
		
		job.setStatus(Status.RUNNING);
		jobDAO.update(job);
		
		assertTrue("Worker should be busy now on the running job",worker.isBusy());
		assertTrue("Looked up worker should be busy now on the running job",workerDAO.reread(worker).isBusy());
		
		job.setStatus(Status.COMPLETE);
		jobDAO.update(job);
		
		assertFalse("Worker should no longer be busy now the job has finished",worker.isBusy());	
	}
	
	@Test
	public void testIsRunning() throws Exception {
		WorkerDAO workerDAO = daoFactory.getWorkerDAO();
		JobDAO jobDAO = daoFactory.getJobDAO();
		
		Worker worker = new Worker();
		worker.setPassword(User.generatePassword());
		workerDAO.create(worker);
		
		new TestJob().createAndStore();
		Job job = jobDAO.read(TestJob.lastJob);
		job.setStatus(Status.QUEUED);
		jobDAO.create(job);
		
		assertFalse("Worker hasn't started yet so shouldn't be running",worker.isRunning());
		worker.assignJob(job);
		workerDAO.update(worker);
		jobDAO.update(job);
		
		assertFalse("Worker should be not be running the assigned job",worker.isRunning());
		assertFalse("Looked up worker should be not be running the assigned job",workerDAO.reread(worker).isRunning());
		
		job.setStatus(Status.RUNNING);
		jobDAO.update(job);
		
		assertTrue("Worker should be busy now on the running job",worker.isRunning());
		assertTrue("Looked up worker should be busy now on the running job",workerDAO.reread(worker).isRunning());
		
		job.setStatus(Status.COMPLETE);
		jobDAO.update(job);
		
		assertFalse("Worker should no longer be busy now the job has finished",worker.isRunning());	
	}
	
	

}
