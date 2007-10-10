package net.sf.taverna.t2.activities.gridsam;

import static org.junit.Assert.*;

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
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.tsunami.SecurityAgentManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.commons.io.IOUtils;
import org.icenigrid.gridsam.client.common.ClientSideJobManager;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobStage;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;
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
			this.thread = new Thread(runMe);
			thread.start();
		}
	}

	public static final String OUT_URI = "ftp://gridsam.lesc.doc.ic.ac.uk:45521/public/test.txt";

	public static final String IN_URI_KEY = "$$$";

	public static final String JSDL = "<JobDefinition xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">  "
			+ "   <JobDescription>"
			+ "      <Application>"
			+ " <POSIXApplication xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">"
			+ "<Executable>/usr/bin/wc</Executable> "
			+ "<Argument>-l</Argument> "
			+ "<Argument>inputfile.txt</Argument> "
			+ "<Output>stdout.txt</Output> "
			+ "<Error>error.txt</Error> "
			+ "</POSIXApplication>"
			+ "        </Application>"
			+ "        <DataStaging>"
			+ "            <FileName>inputfile.txt</FileName>"
			+ "            <CreationFlag>overwrite</CreationFlag>"
			+ "            <DeleteOnTermination>true</DeleteOnTermination>"
			+ "            <Source>" + "                <URI>"
			+ IN_URI_KEY
			+ "</URI>"
			+ "            </Source>"
			+ "        </DataStaging>"
			+ "        <DataStaging>"
			+ "            <FileName>error.txt</FileName>"
			+ "            <CreationFlag>overwrite</CreationFlag>"
			+ "            <DeleteOnTermination>true</DeleteOnTermination>"
			+ "            <Target>"
			+ "                <URI>ftp://gridsam.lesc.doc.ic.ac.uk:45521/public/error.txt</URI>"
			+ "            </Target>"
			+ "        </DataStaging>"
			+ "        <DataStaging>"
			+ "            <FileName>stdout.txt</FileName>"
			+ "            <CreationFlag>overwrite</CreationFlag>"
			+ "            <DeleteOnTermination>true</DeleteOnTermination>"
			+ "            <Target>"
			+ "                <URI>"
			+ OUT_URI
			+ "</URI>"
			+ "            </Target>"
			+ "        </DataStaging>"
			+ "    </JobDescription>" + "</JobDefinition>";

	
	public static final String JOB_MANAGER = "https://172.24.2.34:18443/gridsam/services/gridsam?wsdl";

	@Test
	public void testGridsam() throws Exception {
		ClientSideJobManager jobmgr = new ClientSideJobManager(JOB_MANAGER);
		JobDefinitionDocument jobdef = JobDefinitionDocument.Factory.parse(JSDL
				.replace(IN_URI_KEY, "http://wwdfdfw.soton.ac.uk/"));
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
		URL outURL = new URL(OUT_URI);
		InputStream stream = outURL.openStream();
		// Assumes www.soton.ac.uk is 224 lines long
		assertEquals("224 inputfile.txt\n", IOUtils.toString(stream));
	}

	@Test
	public void testActivity() throws Exception {
		final InMemoryDataManager dataManager = new InMemoryDataManager(
				"namespace", Collections.EMPTY_SET);

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
		assertEquals(OUT_URI, url.toString());
		// Check content
		URL outURL = new URL(OUT_URI);
		InputStream stream = outURL.openStream();
		// Assumes www.soton.ac.uk is 224 lines long
		assertEquals("224 inputfile.txt\n", IOUtils.toString(stream));
	}

}
