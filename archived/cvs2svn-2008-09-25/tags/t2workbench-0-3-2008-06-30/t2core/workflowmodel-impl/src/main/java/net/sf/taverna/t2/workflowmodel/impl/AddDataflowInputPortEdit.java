package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Adds a dataflow input port to a dataflow.
 * 
 * @author David Withers
 */
public class AddDataflowInputPortEdit extends AbstractDataflowEdit {

	private DataflowInputPort dataflowInputPort;

	public AddDataflowInputPortEdit(Dataflow dataflow, DataflowInputPort dataflowInputPort) {
		super(dataflow);
		this.dataflowInputPort = dataflowInputPort;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		if (dataflowInputPort instanceof DataflowInputPortImpl) {
			dataflow.addInputPort((DataflowInputPortImpl) dataflowInputPort);
		}
		else {
			throw new EditException("The DataflowInputPort is of the wrong implmentation, it should be of type DataflowInputPortImpl");
		}
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		try {
			dataflow.removeDataflowInputPort(dataflowInputPort);
		} catch (EditException e1) {
			//this port has already been added
		}
	}

}
