/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the ConcurrencyConstraint constructor if the arguments passed in
 * are invalid.
 * 
 * @author Tom Oinn
 */
public class ConcurrencyConstraintCreationException extends ScuflException {

	public ConcurrencyConstraintCreationException(String the_message) {
		super(the_message);
	}

}
