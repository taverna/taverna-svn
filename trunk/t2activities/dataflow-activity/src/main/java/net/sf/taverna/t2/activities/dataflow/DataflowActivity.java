package net.sf.taverna.t2.activities.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

import org.apache.log4j.Logger;

/**
 * <p>
 * An Activity providing nested Dataflow functionality.
 * </p>
 * 
 * @author David Withers
 */
public class DataflowActivity extends
		AbstractAsynchronousActivity<Dataflow> implements NestedDataflow{

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(DataflowActivity.class);

	private Edits edits = EditsRegistry.getEdits();

	private Dataflow dataflow;
	
	@Override
	public void configure(Dataflow dataflow)
			throws ActivityConfigurationException {
		this.dataflow=dataflow;
		buildInputPorts();
		buildOutputPorts();
	}

	@Override
	public Dataflow getConfiguration() {
		return dataflow;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {

				final WorkflowInstanceFacade facade;
				try {
					facade = edits
							.createWorkflowInstanceFacade(dataflow, callback
									.getContext(), callback
									.getParentProcessIdentifier());
				} catch (InvalidDataflowException ex) {
					callback.fail("Invalid dataflow", ex);
					return;
				}

				facade.addResultListener(new ResultListener() {
					int outputPortCount = dataflow.getOutputPorts().size();

					Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

					public void resultTokenProduced(
							WorkflowDataToken dataToken, String port) {
						if (dataToken.getIndex().length == 0) {
							outputData.put(port, dataToken.getData());
							synchronized (this) {
								if (--outputPortCount == 0) {
									callback.receiveResult(outputData, dataToken.getIndex());
									facade.removeResultListener(this);
								}
							}
						}
					}
				});

				facade.fire();

				for (Map.Entry<String, T2Reference> entry : data
						.entrySet()) {
					try {
						WorkflowDataToken token = new WorkflowDataToken(
								callback.getParentProcessIdentifier(),
								new int[] {}, entry.getValue(), callback
										.getContext());
						facade.pushData(token, entry.getKey());
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
					true, new ArrayList<Class<? extends ExternalReferenceSPI>>(),
					null);
		}
	}

	private void buildOutputPorts() throws ActivityConfigurationException {
		for (DataflowOutputPort dataflowOutputPort : dataflow.getOutputPorts()) {
			addOutput(dataflowOutputPort.getName(), dataflowOutputPort
					.getDepth(), dataflowOutputPort.getGranularDepth());
		}
	}

	public Dataflow getNestedDataflow() {
		return getConfiguration();
	}

}
