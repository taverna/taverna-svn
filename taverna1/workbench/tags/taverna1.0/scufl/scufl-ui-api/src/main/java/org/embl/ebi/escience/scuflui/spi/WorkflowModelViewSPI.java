package org.embl.ebi.escience.scuflui.spi;

import org.embl.ebi.escience.scufl.ScuflModel;

/**
 * Interface for classes which should be notified about
 * changes to the current active workflow model 
 * (ModelMap.CURRENT_WORKFLOW) object
 * within the workbench UI
 * @author Tom Oinn
 */
public interface WorkflowModelViewSPI extends UIComponentSPI {

	/**
	 * Directs the implementing component to bind to the specified ScuflModel
	 * instance, refresh its internal state from the model and commence
	 * listening to events, maintaining its state as these events dictate.
	 */
	public void attachToModel(ScuflModel model);

	/**
	 * Directs the implementing component to detach from the model, set its
	 * internal state to some suitable blank (i.e. blank image, no text in a
	 * text field etc) and desist from listening to model events.
	 */
	public void detachFromModel();
	
}
