/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the constructor of the ConcurrencyConstraint class when
 * a duplicate name is added to a particular ScuflModel
 * @author Tom Oinn
 */
public class DuplicateConcurrencyConstraintNameException extends Exception {

    public DuplicateConcurrencyConstraintNameException(String the_message) {
	super(the_message);
    }

}
