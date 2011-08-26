/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the ScuflModel when it can't find a 
 * particular named processor.
 * @author Tom Oinn
 */
public class UnknownProcessorException extends Exception {

    public UnknownProcessorException(String the_message) {
	super(the_message);
    }

}
