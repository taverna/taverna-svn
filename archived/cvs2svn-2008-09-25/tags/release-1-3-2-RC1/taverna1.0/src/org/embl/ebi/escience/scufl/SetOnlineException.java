/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown from the model when there is a problem going
 * from offline to online mode - normally this is 
 * a similar problem to the workflow load but I guess
 * there could be additional factors.
 * @author Tom Oinn
 */
public class SetOnlineException extends Exception {

    public SetOnlineException() {
	//
    }
    
    public SetOnlineException(String message) {
	super(message);
    }

}
