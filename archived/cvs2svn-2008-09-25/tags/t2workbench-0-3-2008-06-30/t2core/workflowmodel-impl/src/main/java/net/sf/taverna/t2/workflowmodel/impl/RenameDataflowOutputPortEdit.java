package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * Rename the specified DataflowOutputPort
 * 
 * @author David Withers
 * 
 */
public class RenameDataflowOutputPortEdit extends AbstractDataflowOutputPortEdit {

	private String newName;

	private String oldName = null;

	public RenameDataflowOutputPortEdit(DataflowOutputPort dataflowOutputPort, String newName) {
		super(dataflowOutputPort);
		this.newName = newName;
	}

	@Override
	protected void doEditAction(DataflowOutputPortImpl dataflowOutputPort) throws EditException {
		oldName = dataflowOutputPort.getName();
		dataflowOutputPort.setName(newName);
	}

	@Override
	protected void undoEditAction(DataflowOutputPortImpl dataflowOutputPort) {
		dataflowOutputPort.setName(oldName);
	}

}
