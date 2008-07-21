package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Adds a dataflow output port to a dataflow.
 * 
 * @author David Withers
 */
public class AddDataflowOutputPortEdit extends AbstractDataflowEdit {

	private DataflowOutputPort dataflowOutputPort;

	public AddDataflowOutputPortEdit(Dataflow dataflow, DataflowOutputPort dataflowOutputPort) {
		super(dataflow);
		this.dataflowOutputPort = dataflowOutputPort;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		if (dataflowOutputPort instanceof DataflowOutputPortImpl) {
			dataflow.addOutputPort((DataflowOutputPortImpl) dataflowOutputPort);
		}
		else {
			throw new EditException("The DataflowOutputPort is of the wrong implmentation, it should be of type DataflowOutputPortImpl");
		}
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		try {
			dataflow.removeDataflowOutputPort(dataflowOutputPort);
		} catch (EditException e1) {
			//this port has already been added
		}
	}

}
