package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

/**
 * Edit object to add a new DispatchLayer instance to a DispatchStackImpl
 * 
 * @author Tom Oinn
 * 
 */
public class AddDispatchLayerEdit extends AbstractDispatchLayerEdit {

	private DispatchLayer<?> layer;

	private int index;

	public AddDispatchLayerEdit(DispatchStack stack, DispatchLayer<?> newLayer,
			int index) {
		super(stack);
		this.layer = newLayer;
		this.index = index;
	}

	@Override
	protected void doEditAction(DispatchStackImpl stack) throws EditException {
		stack.addLayer(this.layer, this.index);
	}

	@Override
	protected void undoEditAction(DispatchStackImpl stack) {
		stack.removeLayer(layer);
	}

}
