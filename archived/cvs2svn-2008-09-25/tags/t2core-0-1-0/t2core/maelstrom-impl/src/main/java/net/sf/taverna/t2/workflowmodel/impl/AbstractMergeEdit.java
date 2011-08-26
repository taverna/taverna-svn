package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Merge;

public abstract class AbstractMergeEdit implements Edit<Merge>{
	
	Merge merge;
	boolean applied=false;
	
	public AbstractMergeEdit(Merge merge) {
		if (merge==null) throw new RuntimeException("Cannot construct a merge edit with a null merge");
		this.merge=merge;
	}

	public Merge doEdit() throws EditException {
		if (applied) throw new EditException("Edit has already been applied!");
		if (!(merge instanceof MergeImpl)) throw new EditException("Merge must be an instanceof MergeImpl");
		MergeImpl mergeImpl = (MergeImpl)merge;
		try {
			synchronized (mergeImpl) {
				doEditAction(mergeImpl);
				applied = true;
			}
		} catch (EditException ee) {
			applied = false;
			throw ee;
		}
		
		return this.merge;
	}

	protected abstract void doEditAction(MergeImpl mergeImpl) throws EditException;
	protected abstract void undoEditAction(MergeImpl mergeImpl);
	
	public Object getSubject() {
		return merge;
	}

	public boolean isApplied() {
		return applied;
	}

	public void undo() {
		if (!applied) {
			throw new RuntimeException(
					"Attempt to undo edit that was never applied");
		}
		MergeImpl mergeImpl = (MergeImpl) merge;
		synchronized (mergeImpl) {
			undoEditAction(mergeImpl);
			applied = false;
		}
	}

}
