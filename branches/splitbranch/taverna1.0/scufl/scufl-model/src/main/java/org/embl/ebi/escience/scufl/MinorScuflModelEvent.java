/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Signifies a change in the model that might be of interest to a view. This
 * subclass indicates that the event does not change the major contents of the
 * workflow, so tree views etc will not have to update their structure.
 * 
 * @author Tom Oinn
 */
public class MinorScuflModelEvent extends ScuflModelEvent {

	public MinorScuflModelEvent(Object source, String message) {
		super(source, message);
	}

}
