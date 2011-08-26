package net.sf.taverna.service.rest;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Reference;

import static org.junit.Assert.*;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.rest.utils.URItoDAO;
import net.sf.taverna.service.test.TestCommon;

public class TestURIToDAO extends TestCommon {

	private static final String ROOT = "http://example.com";

	DAOFactory daoFactory = DAOFactory.getFactory();

	URIFactory uriFactory = URIFactory.getInstance();

	URItoDAO uriToDao = URItoDAO.getInstance();

	@Before
	public void setRoot() {
		uriFactory.setRoot(ROOT);
	}

	@Test
	public void resolveWorkflow() {
		Workflow wf = storeWorkflow();
		String wfUri = uriFactory.getURI(wf);
		assertTrue(wfUri.startsWith(ROOT));
		assertTrue(wfUri.endsWith(wf.getId()));
		assertSame(wf, uriToDao.getResource(wfUri, Workflow.class));
		assertSame(wf, uriToDao.getResource(new Reference(wfUri),
			Workflow.class));
	}
	
	public void resolveWrongClass() {
		Workflow wf = storeWorkflow();
		String wfUri = uriFactory.getURI(wf);
		uriToDao.getResource(wfUri, Job.class);
	}

	@Test
	public void resolveWorkflowReference() {
		Workflow wf = storeWorkflow();
		Reference wfRef = new Reference(uriFactory.getURI(wf));
		assertSame(wf, uriToDao.getResource(wfRef, Workflow.class));
	}

	@Test
	public void resolveJob() {
		Job job = storeJob();
		String jobUri = uriFactory.getURI(job);
		assertTrue(jobUri.startsWith(ROOT));
		assertTrue(jobUri.endsWith(job.getId()));
		Job job2 = uriToDao.getResource(jobUri, Job.class);
		assertSame(job, job2);
	}

	@Test
	public void resolveUser() {
		User user = storeUser();
		String uri = uriFactory.getURI(user);
		assertTrue(uri.startsWith(ROOT));
		assertTrue(uri.endsWith(user.getId()));
		User user2 = uriToDao.getResource(uri, User.class);
		assertSame(user, user2);
	}

	@Test
	public void resolveData() {
		DataDoc data = storeData();
		String uri = uriFactory.getURI(data);
		assertTrue(uri.startsWith(ROOT));
		assertTrue(uri.endsWith(data.getId()));
		DataDoc data2 = uriToDao.getResource(uri, DataDoc.class);
		assertSame(data, data2);
	}

	private Workflow storeWorkflow() {
		Workflow wf = new Workflow();
		wf.setScufl(workflow);
		daoFactory.getWorkflowDAO().create(wf);
		return wf;
	}

	private Job storeJob() {
		Workflow wf = storeWorkflow();
		Job job = new Job();
		job.setWorkflow(wf);
		daoFactory.getJobDAO().create(job);
		return job;
	}

	private User storeUser() {
		User user = new User();
		user.setPassword(User.generatePassword());
		daoFactory.getUserDAO().create(user);
		return user;
	}

	private DataDoc storeData() {
		DataDoc data = new DataDoc();
		data.setBaclava(datadoc);
		daoFactory.getDataDocDAO().create(data);
		return data;
	}

}
