package net.sf.taverna.service.backend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.QueueException;
import net.sf.taverna.service.interfaces.UnknownJobException;
import net.sf.taverna.service.test.EngineTest;
import net.sf.taverna.service.util.XMLUtils;

import org.embl.ebi.escience.baclava.DataThing;
import org.junit.Before;
import org.junit.Test;

public class TavernaTest extends EngineTest {

	private Engine engine;
	
	@Before
	public void findEngine() throws IOException {
		engine = Engine.getInstance();
	}

	@Test
	public void runWorkflow() throws QueueException, IOException,
		ParseException {
		JobDAO jobDao = DAOFactory.getFactory().getJobDAO();
		String job_id = engine.runWorkflow(workflow, "");
		// Should be a valid, random uuid
		UUID uuid = UUID.fromString(job_id);
		assertEquals(4, uuid.version());
		String status = engine.jobStatus(job_id);
		// Must be a valid state
		Status.valueOf(status);
		Job job = jobDao.read(job_id);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		jobDao.refresh(job);
		assertTrue(job.isFinished());
		// Check status and fetch result
		assertEquals("COMPLETED", engine.jobStatus(job_id));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getResults() throws QueueException, IOException,
		UnknownJobException, ParseException {
		JobDAO jobDao = DAOFactory.getFactory().getJobDAO();
		String job_id = engine.runWorkflow(workflow, "");
		// OK, we'll cheat and wait for the job to finish
		Job job = jobDao.read(job_id);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		jobDao.refresh(job);
		assertTrue(job.isFinished());
		String resultDoc = engine.getResultDocument(job_id);
		Map<String, DataThing> results = XMLUtils.parseDataDoc(resultDoc);

		int expectedResult = 0;
		String[] expectedResults =
			{ "square red cat", "square greenrabbit", "circular red cat",
					"circular greenrabbit", "triangularred cat",
					"triangulargreenrabbit" };
		for (Entry<String, DataThing> item : results.entrySet()) {
			assertEquals("Output", item.getKey());
			DataThing thing = item.getValue();
			assertTrue(thing.getDataObject() instanceof Collection);
			Collection<Collection> children =
				(Collection) thing.getDataObject();
			for (Collection<String> child : children) {
				for (String grandChild : child) {
					assertEquals(expectedResults[expectedResult++], grandChild);
				}
			}
		}
		// Should have traversed all the expected kids
		assertEquals(expectedResults.length, expectedResult);
	}

}
