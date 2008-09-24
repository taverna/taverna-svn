package org.embl.ebi.escience.scuflui.spi;

import org.embl.ebi.escience.scufl.SemanticMarkup;

/**
 * View and or Control over a SemanticMarkup model object
 * @author Tom Oinn
 */
public interface SemanticMarkupViewSPI extends UIComponentSPI {

	/**
	 * Directs the implementing component to bind to the specified SemanticMarkup
	 * instance, refresh its internal state from the model and commence
	 * listening to events, maintaining its state as these events dictate.
	 */
	public void attachToModel(SemanticMarkup model);

	/**
	 * Directs the implementing component to detach from the model, set its
	 * internal state to some suitable blank (i.e. blank image, no text in a
	 * text field etc) and desist from listening to model events.
	 */
	public void detachFromModel();
	
}
