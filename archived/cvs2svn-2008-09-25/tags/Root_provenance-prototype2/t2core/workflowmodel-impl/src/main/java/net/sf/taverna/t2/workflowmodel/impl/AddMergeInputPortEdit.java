package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;

/**
 * Adds a merge input port to a merge.
 * 
 * @author David Withers
 */
public class AddMergeInputPortEdit extends AbstractMergeEdit {

	private MergeInputPort mergeInputPort;
	
	public AddMergeInputPortEdit(Merge merge, MergeInputPort mergeInputPort) {
		super(merge);
		this.mergeInputPort = mergeInputPort;
	}

	@Override
	protected void doEditAction(MergeImpl mergeImpl) throws EditException {
		if (mergeInputPort instanceof MergeInputPortImpl) {
			mergeImpl.addInputPort((MergeInputPortImpl) mergeInputPort);
		}
		else {
			throw new EditException("The MergeInputPort is of the wrong implmentation, it should be of type MergeInputPortImpl");
		}
	}

	@Override
	protected void undoEditAction(MergeImpl mergeImpl) {
		if (mergeInputPort instanceof MergeInputPortImpl) {
			mergeImpl.removeInputPort((MergeInputPortImpl) mergeInputPort);
		}
	}

}
