/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the Processor classes when then can't find a 
 * particular named port.
 * @author Tom Oinn
 */
public class UnknownPortException extends Exception {

    public UnknownPortException(String the_message) {
	super(the_message);
    }

}
