package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

public class UpdateDataflowInternalIdentifierEdit extends AbstractDataflowEdit {

	private String newId;
	private String oldId;

	public UpdateDataflowInternalIdentifierEdit(Dataflow dataflow,String newId) {
		super(dataflow);
		this.newId=newId;
		this.oldId=dataflow.getInternalIdentier();
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.internalIdentifier=newId;
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		dataflow.internalIdentifier=oldId;
	}
	

}
