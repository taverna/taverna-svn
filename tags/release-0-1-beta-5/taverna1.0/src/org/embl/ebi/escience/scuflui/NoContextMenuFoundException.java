/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

/**
 * Thrown by the ScuflContextMenuFactory if it can't locate
 * an appropriate popup menu for the supplied object.
 * @author Tom Oinn
 */
public class NoContextMenuFoundException extends Exception {

    public NoContextMenuFoundException(String the_message) {
	super(the_message);
    }

}
