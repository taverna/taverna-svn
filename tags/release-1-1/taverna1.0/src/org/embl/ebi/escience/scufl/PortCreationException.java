/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the Port constructor if either of the
 * arguments passed are null, or if the name is
 * the empty string.
 * @author Tom Oinn
 */
public class PortCreationException extends Exception {

    public PortCreationException(String the_message) {
	super(the_message);
    }

}
