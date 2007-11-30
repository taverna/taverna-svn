package net.sf.taverna.t2.activities.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityHealthReport;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;

/**
 * <p>
 * An Activity providing nested Dataflow functionality.
 * </p>
 * 
 * @author David Withers
 */
public class DataflowActivity extends
		AbstractAsynchronousActivity<DataflowActivityConfigurationBean> {

	private static final Logger logger = Logger
			.getLogger(DataflowActivity.class);

	private DataflowActivityConfigurationBean configurationBean;

	private Dataflow dataflow;

	private Edits edits = EditsRegistry.getEdits();

	@Override
	public void configure(DataflowActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		this.dataflow = configurationBean.getDataflow();
		buildInputPorts();
		buildOutputPorts();
	}

	@Override
	public DataflowActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {

				final WorkflowInstanceFacade facade = edits
						.createWorkflowInstanceFacade(dataflow,callback.getContext());

				facade.addResultListener(new ResultListener() {
					int outputPortCount = dataflow.getOutputPorts().size();

					public void resultTokenProduced(EntityIdentifier id,
							int[] index, String port, String owningProcess) {
						Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();
						outputData.put(port, id);
						callback.receiveResult(outputData, index);
						if (index.length == 0) {
							if (--outputPortCount == 0) {
								facade.removeResultListener(this);
							}
						}
					}

				});

				facade.fire();

				for (Map.Entry<String, EntityIdentifier> entry : data
						.entrySet()) {
					try {
						facade.pushData(entry.getValue(), new int[0], entry
								.getKey());
					} catch (TokenOrderException e) {
						callback.fail("Failed to push data into facade", e);
					}
				}

			}

		});
	}

	private void buildInputPorts() throws ActivityConfigurationException {
		for (DataflowInputPort dataflowInputPort : dataflow.getInputPorts()) {
			addInput(dataflowInputPort.getName(), dataflowInputPort.getDepth(),
					getMimeTypes(dataflowInputPort));
		}
	}

	private void buildOutputPorts() throws ActivityConfigurationException {
		for (DataflowOutputPort dataflowOutputPort : dataflow.getOutputPorts()) {
			addOutput(dataflowOutputPort.getName(), dataflowOutputPort
					.getDepth(), dataflowOutputPort.getGranularDepth(),
					getMimeTypes(dataflowOutputPort));
		}
	}

	private List<String> getMimeTypes(DataflowPort outputPort) {
		// TODO get the mime types from the annotation
		return new ArrayList<String>();
	}
	
	public ActivityHealthReport checkActivityHealth() {
		return new ActivityHealthReport("Checking the health of this type of Activity is not yet implemented.",Status.WARNING);
	}

}
