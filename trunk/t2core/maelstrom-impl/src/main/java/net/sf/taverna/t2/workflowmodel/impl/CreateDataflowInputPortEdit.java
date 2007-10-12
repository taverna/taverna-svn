package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Adds a new input port to a dataflow
 * 
 * @author David Withers
 *
 */
public class CreateDataflowInputPortEdit extends AbstractDataflowEdit {

	private String newPortName;

	private int newPortDepth;
	
	private int newPortGranularDepth;

	public CreateDataflowInputPortEdit(Dataflow dataflow, String portName,
			int portDepth, int portGranularDepth) {
		super(dataflow);
		this.newPortName = portName;
		this.newPortDepth = portDepth;
		this.newPortGranularDepth = portGranularDepth;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.createInputPort(newPortName, newPortDepth, newPortGranularDepth);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		try {
			dataflow.removeDataflowInputPort(newPortName);
		} catch (EditException e) {
			//shouldn't happen as port should exist
		}
	}

}
