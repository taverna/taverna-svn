/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import org.embl.ebi.escience.scufl.ScuflException;

/**
 * Signifies that a scavenger failed during instantiation, most commonly because
 * of network unavailability.
 * 
 * @author Tom Oinn
 */
public class ScavengerCreationException extends ScuflException {

	public ScavengerCreationException(String message) {
		super(message);
	}

}
