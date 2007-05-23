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


public class TestJobBean extends TestDAO  {

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
    	job.setStatus(Status.RUNNING);    	
    	job.setProgressReport("The progress report");
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
        assertEquals("The progress report", fetchedJob.getProgressReport());
        assertEquals(workflow, fetchedJob.getWorkflow().getScufl());
        assertEquals(Status.RUNNING, fetchedJob.getStatus());
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
	
	
	
}
