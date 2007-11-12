package net.sf.taverna.t2.activities.dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.AbstractEventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPortBuilder;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityPortBuilderImpl;

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

	private static final Logger logger = Logger.getLogger(DataflowActivity.class);

	private DataflowActivityConfigurationBean configurationBean;

	private Dataflow dataflow;

	private Edits edits = new EditsImpl();
	
	private AsynchronousActivityCallback callback;
	
	private AtomicLong runIndex = new AtomicLong(0);

	@Override
	protected ActivityPortBuilder getPortBuilder() {
		// FIXME: remove this dependency on the maelstrom-impl. This is
		// currently the only link between the 2.
		// There are easy ways to do this, but non of them particularly elegant.
		// Passing the builder to configure involves passing it around all over
		// the place.
		return ActivityPortBuilderImpl.getInstance();
	}

	@Override
	public void configure(DataflowActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		this.dataflow = configurationBean.getDataflow();
		buildInputPorts();
		buildOutputPorts();

		for (final DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
			Datalink datalink = edits.createDatalink(outputPort,
					new AbstractEventHandlingInputPort(outputPort
							.getName(), outputPort.getDepth()) {

						public void receiveEvent(WorkflowDataToken t) {
							logger.info("Received event for " + outputPort.getName());
							Map<String, EntityIdentifier> outputData = new HashMap<String, EntityIdentifier>();
							outputData.put(getName(), t.getData());
							callback.receiveResult(outputData, t.getIndex());
						}

					});
			try {
				logger.info("Connect link: " + datalink);
				edits.getConnectDatalinkEdit(datalink).doEdit();
			} catch (EditException e) {
				callback.fail("", e);
			}
		}

	}

	@Override
	public DataflowActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, EntityIdentifier> data,
			final AsynchronousActivityCallback callback) {
		this.callback = callback;
		logger.info("executeAsynch dataflow");
		callback.requestRun(new Runnable() {

			public void run() {
				String owningProcess = dataflow.getLocalName() + runIndex.getAndIncrement();
				logger.info("Run dataflow");
				for (DataflowInputPort inputPort : dataflow.getInputPorts()) {
					if (data.containsKey(inputPort.getName())) {
						EntityIdentifier id =  data.get(inputPort.getName());
						logger.info("Port = " + inputPort.getName());
						logger.info("Depth = " + id.getDepth());
						inputPort.receiveEvent(new WorkflowDataToken(owningProcess, new int[0], id));
					}
				}
				
				for (Processor processor : dataflow.getProcessors()) {
					if (processor.getInputPorts().size() == 0 && processor.getPreconditionList().size() == 0) {
						processor.fire(owningProcess);
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

}
