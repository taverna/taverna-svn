package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

public class UpdateDataflowNameEdit extends AbstractDataflowEdit {

	private String newName;
	private String oldName;

	public UpdateDataflowNameEdit(Dataflow dataflow,String newName) {
		super(dataflow);
		this.newName=newName;
		this.oldName=dataflow.getLocalName();
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.setLocalName(newName);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		dataflow.setLocalName(oldName);
	}
	

}
