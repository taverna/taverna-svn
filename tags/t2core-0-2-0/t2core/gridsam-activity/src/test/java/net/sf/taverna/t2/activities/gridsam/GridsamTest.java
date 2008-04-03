package net.sf.taverna.t2.activities.gridsam;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.AsynchEchoActivity;
import net.sf.taverna.t2.workflowmodel.processor.EchoConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.commons.io.IOUtils;
import org.icenigrid.gridsam.client.common.ClientSideJobManager;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobStage;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class GridsamTest {

	private final class DummyCallBack implements AsynchronousActivityCallback {
		private final InMemoryDataManager dataManager;
		public Map<String, EntityIdentifier> data;
		public Thread thread;

		private DummyCallBack(InMemoryDataManager dataManager) {
			this.dataManager = dataManager;
		}

		public void fail(String message, Throwable t) {
			// TODO Auto-generated method stub

		}

		public void fail(String message) {
			// TODO Auto-generated method stub

		}

		public DataManager getLocalDataManager() {
			return dataManager;
		}

		public SecurityAgentManager getLocalSecurityManager() {
			// TODO Auto-generated method stub
			return null;
		}

		public void receiveCompletion(int[] completionIndex) {
			// TODO Auto-generated method stub

		}

		public void receiveResult(Map<String, EntityIdentifier> data,
				int[] index) {
			this.data = data;
		}

		public void requestRun(Runnable runMe) {
			thread = new Thread(runMe);
			thread.start();
		}
	}

	public InMemoryDataManager dataManager;

	@Before
	public void makeDataManager() {
		dataManager = new InMemoryDataManager(
				"namespace", Collections.EMPTY_SET);

	}

	@Before
	public void resetClientConfig() {
		System.getProperties().remove("axis.ClientConfigFile");
	}


	@Test
	public void testGridsam() throws Exception {
		System.setProperty("axis.ClientConfigFile", "/gridsam-client-config.wsdd");

		System.out.println("client-config.wsdd: " + getClass().getResource("/client-config.wsdd"));
		ClientSideJobManager jobmgr = new ClientSideJobManager(GridsamActivity.JOB_MANAGER);
		String outURI = GridsamActivity.makeOutURI();
		String jsdl = GridsamActivity.JSDL
				.replace(GridsamActivity.IN_URI_KEY, "http://www.soton.ac.uk/");
		jsdl = jsdl.replace(GridsamActivity.OUT_URI_KEY, outURI);
		//System.out.println(jsdl);
		JobDefinitionDocument jobdef = JobDefinitionDocument.Factory.parse(jsdl);
		JobInstance job = jobmgr.submitJob(jobdef);
		String jobId = job.getID();


		boolean done = false;
		while (!done) {
			Thread.sleep(1000);
			job = jobmgr.findJobInstance(jobId);
			List stages = job.getJobStages();
			if (! stages.isEmpty()) {
				JobStage stage = (JobStage) stages.get(stages.size() - 1);
				done = stage.getState().isTerminal();
			}
		}
		assertTrue(done);
		URL outURL = new URL(outURI);
		InputStream stream = outURL.openStream();
		// Assumes www.soton.ac.uk is 224 lines long
		assertEquals("224 inputfile.txt\n", IOUtils.toString(stream));
	}

	@Test
	public void testActivity() throws Exception {
		String outURI = GridsamActivity.makeOutURI();

		ReferenceScheme referenceScheme = new URLReferenceScheme(new URL(
				"http://www.soton.ac.uk/"));
		DataDocumentIdentifier ref = dataManager.registerDocument(Collections
				.singleton(referenceScheme));

		GridsamActivity gridsamActivity = new GridsamActivity();

		Map<String, EntityIdentifier> inputs = new HashMap<String, EntityIdentifier>();
		inputs.put("inputPort", ref);

		DummyCallBack callback = new DummyCallBack(dataManager);

		gridsamActivity.executeAsynch(inputs, callback);

		callback.thread.join();
		assertNotNull(callback.data);

		EntityIdentifier entId = callback.data.get("outputPort");
		URL url = null;
		assertTrue(entId instanceof DataDocumentIdentifier);
		DataDocumentIdentifier dataDoc = (DataDocumentIdentifier) entId;
		DataDocument ent = (DataDocument) dataManager.getEntity(dataDoc);
		for (ReferenceScheme reference : ent.getReferenceSchemes()) {
			assertTrue(reference instanceof URLReferenceScheme);
			URLReferenceScheme urlRef = (URLReferenceScheme) reference;
			url = urlRef.getUrl();
			break;
		}
		assertNotNull(url);
		// Check content
		InputStream stream = url.openStream();
		// Assumes www.soton.ac.uk is 224 lines long
		assertEquals("224 inputfile.txt\n", IOUtils.toString(stream));
	}






}
