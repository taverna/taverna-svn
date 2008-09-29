package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Rename the specified DataflowInputPort
 * 
 * @author David Withers
 * 
 */
public class RenameDataflowInputPortEdit extends AbstractDataflowInputPortEdit {

	private String newName;

	private String oldName = null;

	public RenameDataflowInputPortEdit(DataflowInputPort dataflowInputPort, String newName) {
		super(dataflowInputPort);
		this.newName = newName;
	}

	@Override
	protected void doEditAction(DataflowInputPortImpl dataflowInputPort) throws EditException {
		oldName = dataflowInputPort.getName();
		dataflowInputPort.setName(newName);
	}

	@Override
	protected void undoEditAction(DataflowInputPortImpl dataflowInputPort) {
		dataflowInputPort.setName(oldName);
	}

}
