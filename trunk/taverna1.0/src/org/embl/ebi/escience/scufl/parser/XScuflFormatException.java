/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.parser;

import org.embl.ebi.escience.scufl.ScuflException;

/**
 * Thrown by the ScuflModel when it can't find a particular named processor.
 * 
 * @author Tom Oinn
 */
public class XScuflFormatException extends ScuflException {
	
	private static final long serialVersionUID = -7557424386012201728L;

	public XScuflFormatException(String the_message) {
		super(the_message);
	}

}
