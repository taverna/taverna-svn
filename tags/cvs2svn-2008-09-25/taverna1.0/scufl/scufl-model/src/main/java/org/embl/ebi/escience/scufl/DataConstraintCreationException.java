/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the DataConstraint constructor if the arguments passed in are
 * invalid.
 * 
 * @author Tom Oinn
 */
public class DataConstraintCreationException extends ScuflException {

	public DataConstraintCreationException(String the_message) {
		super(the_message);
	}

}
