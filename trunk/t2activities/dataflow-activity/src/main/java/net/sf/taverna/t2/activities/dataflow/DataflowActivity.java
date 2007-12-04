package net.sf.taverna.t2.activities.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.HealthReportImpl;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
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

					public void resultTokenProduced(WorkflowDataToken dataToken, String port, String owningProcess) {
						Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();
						outputData.put(port, dataToken.getData());
						callback.receiveResult(outputData, dataToken.getIndex());
						if (dataToken.getIndex().length == 0) {
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
	
	public HealthReport checkActivityHealth() {
		Status status = Status.OK;
		String message = "Everything seems fine";
		List<HealthReport> subReports = new ArrayList<HealthReport>();
		for (Processor processor : dataflow.getProcessors()) {
			HealthReport subReport = processor.checkProcessorHealth();
			if (subReport.getStatus().equals(Status.WARNING)) {
				if (status.equals(Status.OK)) {
					status = Status.WARNING;
					message = "Some warnings reported";
				}
			} else if (subReport.getStatus().equals(Status.SEVERE)) {
				status = Status.SEVERE;
				message = "We have a problem";
			}
			subReports.add(subReport);
		}
		return new HealthReportImpl("Dataflow Activity", message, status, subReports);
	}

}
