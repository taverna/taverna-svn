/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import org.embl.ebi.escience.scufl.ScuflModel;


/**
 * The interface implemented by all Scufl UI widgets 
 * specifying how they bind and detach from an instance
 * of a ScuflModel.
 * @author Tom Oinn
 */
public interface ScuflUIComponent {

    /**
     * Directs the implementing component to bind to the
     * specified ScuflModel instance, refresh its internal
     * state from the model and commence listening to events,
     * maintaining its state as these events dictate.
     */
    public void attachToModel(ScuflModel model);
    
    /**
     * Directs the implementing component to detach from the 
     * model, set its internal state to some suitable blank
     * (i.e. blank image, no text in a text field etc) and 
     * desist from listening to model events.
     */
    public void detachFromModel();

}
