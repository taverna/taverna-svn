/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the constructor of the Port class when
 * a duplicate name is added to a particular processor
 * @author Tom Oinn
 */
public class DuplicatePortNameException extends Exception {

    public DuplicatePortNameException(String the_message) {
	super(the_message);
    }

}
