package net.sf.taverna.service.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.test.TestDAO;

import org.junit.Test;


public class TestJob extends TestDAO  {

	public static final Status status = Status.NEW;
	
	public static final String progressReport = "<progress>report</progress>";
	
	public static String lastJob;
	
	@Test
    public void createAndStore() throws ParseException {    	
		JobDAO jobDao = daoFactory.getJobDAO();
		WorkflowDAO workflowDao = daoFactory.getWorkflowDAO();
		UserDAO userDao = daoFactory.getUserDAO();

		User user = new User();		
		user.setPassword(User.generatePassword());
		userDao.create(user);
		
    	Workflow w = new Workflow();
    	w.setScufl(workflow);
    	w.setOwner(user);
    	workflowDao.create(w);
		
    	Job job = new Job();
    	job.setWorkflow(w);
    	job.setOwner(user);
    	// job.setStatus(status);  leave at NEW
    	job.setProgressReport(progressReport);
        jobDao.create(job);
        lastJob = job.getId();
        assertNotNull(lastJob);
        daoFactory.commit();
    }
	
	@Test
	public void retrieveLast() throws ParseException {
		if (lastJob == null) { 
			createAndStore();
		}
		assertNotNull("retrieveLast() depends on createAndStore() being run first", lastJob);
		JobDAO jobDao = altFactory.getJobDAO();
    	Job fetchedJob = jobDao.read(lastJob);
        assertEquals(TestJob.progressReport, fetchedJob.getProgressReport());
        assertEquals(workflow, fetchedJob.getWorkflow().getScufl());
        assertEquals(status, fetchedJob.getStatus());
        assertEquals(lastJob, fetchedJob.getId());
        assertFalse(fetchedJob.getCreated().before(fetchedJob.getLastModified()));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void retrieveAll() throws ParseException {
		if (lastJob == null) { 
			createAndStore();
		}
		assertNotNull("retrieveAll() depends on createAndStore() being run first", lastJob);
		JobDAO jobDao = daoFactory.getJobDAO();
        Date date = new Date();
        boolean foundResults = false;
        for (Job j : jobDao) {
        	// Could be the same, but never after
        	assertFalse("Job date not in decreasing order", j.getCreated().after(date));
        	date = j.getCreated();
        	foundResults = true;
        }
        assertTrue("No results found", foundResults);
	}
	
	@Test
	public void isFinished() throws Exception {
		createAndStore();
		Job job = daoFactory.getJobDAO().read(lastJob);
		job.setStatus(Status.RUNNING);
		assertFalse(job.isFinished());
		job.setStatus(Status.PAUSED);
		assertFalse(job.isFinished());
		job.setStatus(Status.FAILING);
		assertFalse(job.isFinished());
		job.setStatus(Status.CANCELLING);
		assertFalse(job.isFinished());
		
		job.setStatus(Status.CANCELLED);
		assertTrue(job.isFinished());
		job.setStatus(Status.COMPLETE);
		assertTrue(job.isFinished());
		job.setStatus(Status.FAILED);
		assertTrue(job.isFinished());
		job.setStatus(Status.DESTROYED);
		assertTrue(job.isFinished());
	}
	
	@Test
	public void hasStarted() throws Exception {
		createAndStore();
		Job job = daoFactory.getJobDAO().read(lastJob);
		job.setStatus(Status.QUEUED);
		assertFalse(job.hasStarted());
		
		job.setStatus(Status.RUNNING);
		assertTrue(job.hasStarted());
		job.setStatus(Status.PAUSED);
		assertTrue(job.hasStarted());
		job.setStatus(Status.FAILING);
		assertTrue(job.hasStarted());
		job.setStatus(Status.CANCELLING);
		assertTrue(job.hasStarted());
		job.setStatus(Status.CANCELLED);
		assertTrue(job.hasStarted());
		job.setStatus(Status.COMPLETE);
		assertTrue(job.hasStarted());
		job.setStatus(Status.FAILED);
		assertTrue(job.hasStarted());
		job.setStatus(Status.DESTROYED);
		assertTrue(job.hasStarted());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void cantReverseStatus() throws ParseException {
		createAndStore();
		Job job = daoFactory.getJobDAO().read(lastJob);
		job.setStatus(Status.QUEUED);
		job.setStatus(Status.RUNNING);
		job.setStatus(Status.QUEUED);
	}
	
}
