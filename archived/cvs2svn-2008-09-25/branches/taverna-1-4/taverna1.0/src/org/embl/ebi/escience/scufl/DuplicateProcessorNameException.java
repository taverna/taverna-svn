/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the constructor of the Processor class when a duplicate name is
 * added to a particular ScuflModel
 * 
 * @author Tom Oinn
 */
public class DuplicateProcessorNameException extends ScuflException {

	public DuplicateProcessorNameException(String the_message) {
		super(the_message);
	}

}
