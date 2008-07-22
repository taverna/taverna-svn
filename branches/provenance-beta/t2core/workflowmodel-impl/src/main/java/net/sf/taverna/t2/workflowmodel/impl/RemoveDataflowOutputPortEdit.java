package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Removes a dataflow output port from a dataflow.
 * 
 * @author David Withers
 *
 */
public class RemoveDataflowOutputPortEdit extends AbstractDataflowEdit {

	private DataflowOutputPort dataflowOutputPort;

	public RemoveDataflowOutputPortEdit(Dataflow dataflow, DataflowOutputPort dataflowOutputPort) {
		super(dataflow);
		this.dataflowOutputPort = dataflowOutputPort;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.removeDataflowOutputPort(dataflowOutputPort);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		if (dataflowOutputPort instanceof DataflowOutputPortImpl) {
			try {
				dataflow.addOutputPort((DataflowOutputPortImpl) dataflowOutputPort);
			} catch (EditException e) {
				//shouldn't happen as a port with this name has been removed
			}
		}
	}

}
