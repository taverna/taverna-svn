package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Merge;

/**
 * An Edit class responsible for adding a Merge to the dataflow.
 * 
 * @author Tom Oinn
 *
 */
public class AddMergeEdit extends AbstractDataflowEdit{
	
	private Merge merge;
	
	protected AddMergeEdit(Dataflow dataflow, Merge merge) {
		super(dataflow);
		this.merge=merge;
	}

	/**
	 * Adds the Merge instance to the Dataflow
	 * 
	 * @throws EditException if the edit has already taken place (without an intermediate undo) or a processor with that name already exists.
	 */
	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		if (merge instanceof MergeImpl) {
			dataflow.addMerge((MergeImpl)merge);
		}
		else {
			throw new EditException("The Merge is of the wrong implmentation, it should be of type MergeImpl");
		}
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		dataflow.removeMerge(merge);
	}
}
