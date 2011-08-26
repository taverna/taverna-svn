/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Signifies a change in the model that might
 * be of interest to a view
 * @author Tom Oinn
 */
public class ScuflModelEvent {

    Object source = null;
    private String message = null;

    /**
     * Construct a new event, the object generating the
     * event should put itself in the source field, and
     * may optionally supply a textual message as well
     */
    public ScuflModelEvent(Object source, String message) {
	this.source = source;
	if (message != null) {
	    this.message = message;
	}
	else {
	    this.message = "ScuflModelEvent : No message provided";
	}
    }

    /**
     * Get the message
     */
    public String getMessage() {
	return this.message;
    }

    /**
     * Get the source of the event
     */
    public Object getSource() {
	return this.source;
    }

}
