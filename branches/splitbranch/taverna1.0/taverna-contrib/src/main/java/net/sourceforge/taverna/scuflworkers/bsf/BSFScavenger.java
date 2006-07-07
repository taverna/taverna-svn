/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.bsf;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that knows how to create bsf processors
 * 
 * @author mfortner
 */
public class BSFScavenger extends Scavenger {

	/**
	 * Create a new Beanshell scavenger
	 */
	public BSFScavenger() throws ScavengerCreationException {
		super(new BSFProcessorFactory());
	}
}
