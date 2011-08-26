/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

/**
 * Thrown when the serialisation or deserialisation
 * of the myGrid data document format fails because
 * the framework cannot locate an appropriate plugin
 * to handle a particular type of data.
 * @author Tom Oinn
 */
public class NoHandlerFoundException extends Exception {
    
    public NoHandlerFoundException() {
	super();
    }

    public NoHandlerFoundException(String message) {
	super(message);
    }

    public NoHandlerFoundException(String message, Exception cause) {
	super(message, cause);
    }

    public NoHandlerFoundException(Exception cause) {
	super(cause);
    }

}
