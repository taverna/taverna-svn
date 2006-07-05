/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Stian Soiland, myGrid
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that knows how to create Rserv processors
 * 
 * @author Stian Soiland
 */
public class RservScavenger extends Scavenger {

	public RservScavenger() throws ScavengerCreationException {
		super(new RservProcessorFactory());
	}
}
