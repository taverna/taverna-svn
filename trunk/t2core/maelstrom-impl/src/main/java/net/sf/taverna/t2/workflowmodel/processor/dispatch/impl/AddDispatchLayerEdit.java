package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;

/**
 * Edit object to add a new DispatchLayer instance to a DispatchStackImpl
 * 
 * @author Tom Oinn
 * 
 */
public class AddDispatchLayerEdit implements Edit {

	private DispatchLayer layer;

	private int index;

	private boolean applied = false;

	private DispatchStackImpl stack;

	public AddDispatchLayerEdit(DispatchStackImpl stack2,
			DispatchLayer newLayer, int index) {
		this.layer = newLayer;
		this.index = index;
		this.stack = stack2;
	}

	public void doEdit() throws EditException {
		if (applied) {
			throw new EditException("Cannot re-apply edit");
		}
		stack.addLayer(this.layer, this.index);
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
			stack.removeLayer(layer);
			applied = false;
		}
	}

}
