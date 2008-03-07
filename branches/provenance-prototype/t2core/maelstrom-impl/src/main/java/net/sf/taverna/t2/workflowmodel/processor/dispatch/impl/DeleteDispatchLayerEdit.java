package net.sf.taverna.t2.workflowmodel.processor.dispatch.impl;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

/**
 * Edit implementation to remove a DispatchLayer from a DispatchStackImpl
 * 
 * @author Tom Oinn
 * 
 */
public class DeleteDispatchLayerEdit extends AbstractDispatchLayerEdit {

	private DispatchLayer<?> layer;

	private int index;

	public DeleteDispatchLayerEdit(DispatchStack stack,
			DispatchLayer<?> removeLayer) {
		super(stack);
		this.layer = removeLayer;
	}

	@Override
	protected void doEditAction(DispatchStackImpl stack) throws EditException {
		index = stack.removeLayer(layer);		
	}

	@Override
	protected void undoEditAction(DispatchStackImpl stack) {
		stack.addLayer(layer, index);
	}

}
