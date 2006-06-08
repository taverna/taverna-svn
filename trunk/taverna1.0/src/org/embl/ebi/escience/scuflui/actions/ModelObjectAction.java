package org.embl.ebi.escience.scuflui.actions;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * Abstract class for action performed on an object within the ScuflModel, for
 * instance a Processor or a Port.
 * 
 * @author Stian Soiland
 * 
 */
public abstract class ModelObjectAction extends ScuflModelAction {

	/**
	 * Object the action is performed on
	 */
	protected Object modelObject;

	/**
	 * Create an action for the given modelObject.
	 * 
	 * @param model
	 *            Model containing modelObject
	 * @param modelObject
	 *            Object in the model which the action is to be performed on.
	 */
	public ModelObjectAction(ScuflModel model, Object modelObject) {
		super(model);
		this.modelObject = modelObject;
	}

}
