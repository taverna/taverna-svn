package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Change the depth of the specified DataflowInputPort.
 * 
 * @author David Withers
 * 
 */
public class ChangeDataflowInputPortDepthEdit extends AbstractDataflowInputPortEdit {

	private int newDepth;

	private int oldDepth;

	public ChangeDataflowInputPortDepthEdit(DataflowInputPort dataflowInputPort, int newDepth) {
		super(dataflowInputPort);
		this.newDepth = newDepth;
	}

	@Override
	protected void doEditAction(DataflowInputPortImpl dataflowInputPort) throws EditException {
		oldDepth = dataflowInputPort.getDepth();
		dataflowInputPort.setDepth(newDepth);
	}

	@Override
	protected void undoEditAction(DataflowInputPortImpl dataflowInputPort) {
		dataflowInputPort.setDepth(oldDepth);
	}

}
