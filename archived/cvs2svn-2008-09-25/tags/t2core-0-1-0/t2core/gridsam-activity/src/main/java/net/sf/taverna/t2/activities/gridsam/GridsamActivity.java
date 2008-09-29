package net.sf.taverna.t2.activities.gridsam;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;

import org.icenigrid.gridsam.client.common.ClientSideJobManager;
import org.icenigrid.gridsam.core.JobInstance;
import org.icenigrid.gridsam.core.JobStage;
import org.icenigrid.gridsam.core.JobState;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;

public class GridsamActivity extends AbstractAsynchronousActivity<Object> {

	public static int counter = new Random().nextInt();

	public static final String OUT_URI_BASE = "ftp://gridsam.lesc.doc.ic.ac.uk:45521/public/";

//	public static final String JOB_MANAGER = "https://rpc268.cs.man.ac.uk:18443/gridsam/services/gridsam?wsdl";
	public static final String JOB_MANAGER = "http://rpc268.cs.man.ac.uk:18080/gridsam/services/gridsam?wsdl";
//	public static final String JOB_MANAGER = "http://doesnotexists/";

	public static final String IN_URI_KEY = "$in$";

	public static final String OUT_URI_KEY = "$out$";

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
			+ "            <Source>" + "                " + "<URI>"
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
			+ OUT_URI_KEY
			+ "</URI>"
			+ "            </Target>"
			+ "        </DataStaging>"
			+ "    </JobDescription>" + "</JobDefinition>";



	public GridsamActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {
			public void run() {

				System.setProperty("axis.ClientConfigFile", "/gridsam-client-config.wsdd");

				try {
					DataManager dataManager = callback.getLocalDataManager();
					EntityIdentifier entId = data.get("inputPort");
					URL url = null;
					if (entId instanceof DataDocumentIdentifier) {
						DataDocumentIdentifier dataDoc = (DataDocumentIdentifier) entId;
						DataDocument ent = (DataDocument) dataManager
								.getEntity(dataDoc);
						for (ReferenceScheme reference : ent
								.getReferenceSchemes()) {
							if (reference instanceof URLReferenceScheme) {
								URLReferenceScheme urlRef = (URLReferenceScheme) reference;
								url = urlRef.getUrl();
								break;
							}
						}
					}

					ClientSideJobManager jobmgr = new ClientSideJobManager(
							JOB_MANAGER);
					String out_uri = makeOutURI();
					String jsdl = JSDL.replace(
							IN_URI_KEY, url.toString()).replace(OUT_URI_KEY, out_uri);
					JobDefinitionDocument jobdef = JobDefinitionDocument.Factory
							.parse(jsdl);
					System.out.println(jsdl);
					JobInstance job = jobmgr.submitJob(jobdef);
					String jobId = job.getID();

					JobStage stage = null;
					boolean done = false;
					while (!done) {
						Thread.sleep(1000);
						job = jobmgr.findJobInstance(jobId);
						List stages = job.getJobStages();
						if (stages.size() > 0) {
							stage = (JobStage) stages.get(stages
									.size() - 1);
							done = stage.getState().isTerminal();
						}
					}
					if (! stage.getState().equals(JobState.DONE)) {
						// oh noes
						callback.fail("Job terminated in non-successful state "
								+ stage.getState() + ", "
								+ stage.getDescription());
						return;
					}

					ReferenceScheme referenceScheme = new URLReferenceScheme(
							new URL(out_uri));
					DataDocumentIdentifier ref = dataManager
							.registerDocument(Collections
									.singleton(referenceScheme));

					Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();

					outputData.put("outputPort", ref);

					callback.receiveResult(outputData, new int[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


		});
	}

	public static String makeOutURI() {
		String uri = OUT_URI_BASE + "gridsam" + counter++ + ".txt";
		System.out.println("Making URI " + uri);
		return uri;
	}

	@Override
	protected ActivityPortBuilder getPortBuilder() {
		return ActivityPortBuilderImpl.getInstance();
	}

	@Override
	public void configure(Object conf) throws ActivityConfigurationException {
		addInput("inputPort", 0, Collections.singletonList("text/plain"));
		addOutput("outputPort", 0, 0, Collections.singletonList("text/plain"));
	}

	@Override
	public Object getConfiguration() {
		return null;
	}

}
