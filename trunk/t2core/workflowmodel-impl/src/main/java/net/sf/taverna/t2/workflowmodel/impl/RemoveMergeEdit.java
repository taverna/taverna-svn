package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.NamingException;
import net.sf.taverna.t2.workflowmodel.Merge;

/**
 * Removes a merge from a dataflow.
 * 
 * @author David Withers
 */
public class RemoveMergeEdit extends AbstractDataflowEdit{
	
	private Merge merge;
	
	public RemoveMergeEdit(Dataflow dataflow, Merge merge) {
		super(dataflow);
		this.merge = merge;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.removeMerge(merge);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		if (merge instanceof MergeImpl) {
			try {
				dataflow.addMerge((MergeImpl) merge);
			} catch (NamingException e) {
				//a merge with this name has already been removed
			}
		}
	}
	
}
