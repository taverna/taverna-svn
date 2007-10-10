package net.sf.taverna.t2.activities.gridsam;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public GridsamActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {
			public void run() {

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
							GridsamTest.JOB_MANAGER);
					JobDefinitionDocument jobdef = JobDefinitionDocument.Factory
							.parse(GridsamTest.JSDL.replace(
									GridsamTest.IN_URI_KEY, url.toString()));
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
							new URL(GridsamTest.OUT_URI));
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
