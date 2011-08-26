package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Adds a new output port to a dataflow
 * 
 * @author David Withers
 * 
 */
public class CreateDataflowOutputPortEdit extends AbstractDataflowEdit {

	private String newPortName;

	public CreateDataflowOutputPortEdit(Dataflow dataflow, String portName) {
		super(dataflow);
		this.newPortName = portName;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.createOutputPort(newPortName);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		try {
			dataflow.removeDataflowOutputPort(newPortName);
		} catch (EditException e) {
			// shouldn't happen as port should exist
		}
	}

}
