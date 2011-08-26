package org.embl.ebi.escience.scuflui.spi;

import org.embl.ebi.escience.scufl.Processor;

/**
 * Interface for classes which should be notified about
 * changes to the current active processor object
 * within the workbench UI
 * @author Tom Oinn
 */
public interface ProcessorViewSPI extends UIComponentSPI {
	/**
	 * Directs the implementing component to bind to the specified Processor
	 * instance, refresh its internal state from the model and commence
	 * listening to events, maintaining its state as these events dictate.
	 */
	public void attachToModel(Processor p);

	/**
	 * Directs the implementing component to detach from the model, set its
	 * internal state to some suitable blank (i.e. blank image, no text in a
	 * text field etc) and desist from listening to model events.
	 */
	public void detachFromModel();
	
}
