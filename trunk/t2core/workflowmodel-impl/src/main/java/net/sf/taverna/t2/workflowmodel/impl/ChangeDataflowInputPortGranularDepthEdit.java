package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Change the granular depth of the specified DataflowInputPort.
 * 
 * @author David Withers
 * 
 */
public class ChangeDataflowInputPortGranularDepthEdit extends AbstractDataflowInputPortEdit {

	private int newGranularDepth;

	private int oldGranularDepth;

	public ChangeDataflowInputPortGranularDepthEdit(DataflowInputPort dataflowInputPort, int newGranularDepth) {
		super(dataflowInputPort);
		this.newGranularDepth = newGranularDepth;
	}

	@Override
	protected void doEditAction(DataflowInputPortImpl dataflowInputPort) throws EditException {
		oldGranularDepth = dataflowInputPort.getGranularInputDepth();
		dataflowInputPort.setGranularDepth(newGranularDepth);
	}

	@Override
	protected void undoEditAction(DataflowInputPortImpl dataflowInputPort) {
		dataflowInputPort.setGranularDepth(oldGranularDepth);
	}

}
