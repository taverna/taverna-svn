package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.AbstractDispatchLayer;

/**
 * Edit implementation to remove a DispatchLayer from a DispatchStackImpl
 * 
 * @author Tom Oinn
 * 
 */
public class DeleteDispatchLayerEdit implements Edit {

	private AbstractDispatchLayer layer;

	private int index;

	private boolean applied = false;

	private DispatchStackImpl stack;

	public DeleteDispatchLayerEdit(DispatchStackImpl stack,
			AbstractDispatchLayer removeLayer) {
		this.layer = removeLayer;
		this.stack = stack;
	}

	public void doEdit() throws EditException {
		if (applied) {
			throw new EditException("Cannot re-apply edit");
		}
		index = stack.removeLayer(layer);
		applied = true;
	}

	public Object getSubject() {
		return stack;
	}

	public boolean isApplied() {
		return applied;
	}

	public void undo() {
		if (applied) {
			stack.addLayer(layer, index);
			applied = false;
		}
	}

}
