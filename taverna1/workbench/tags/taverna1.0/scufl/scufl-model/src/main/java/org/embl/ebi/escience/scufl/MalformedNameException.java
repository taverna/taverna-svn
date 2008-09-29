/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Thrown by the locatePort method in ScuflModel when the supplied name doesn't
 * conform to the naming rules.
 * 
 * @author Tom Oinn
 */
public class MalformedNameException extends ScuflException {

	public MalformedNameException(String the_message) {
		super(the_message);
	}

}
