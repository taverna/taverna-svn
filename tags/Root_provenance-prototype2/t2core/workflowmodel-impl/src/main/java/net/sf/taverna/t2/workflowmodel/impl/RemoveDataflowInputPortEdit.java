package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Removes a dataflow input port from a dataflow.
 * 
 * @author David Withers
 *
 */
public class RemoveDataflowInputPortEdit extends AbstractDataflowEdit {

	private DataflowInputPort dataflowInputPort;

	public RemoveDataflowInputPortEdit(Dataflow dataflow, DataflowInputPort dataflowInputPort) {
		super(dataflow);
		this.dataflowInputPort = dataflowInputPort;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.removeDataflowInputPort(dataflowInputPort);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		if (dataflowInputPort instanceof DataflowInputPortImpl) {
			try {
				dataflow.addInputPort((DataflowInputPortImpl) dataflowInputPort);
			} catch (EditException e) {
				//shouldn't happen as a port with this name has been removed
			}
		}
	}

}
