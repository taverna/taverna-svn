/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that instantiates the Parser.
 * 
 * @author Eddie Kawas
 */
public class MobyParseDatatypeScavenger extends Scavenger {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new MobyParseDatatype scavenger
	 */
	public MobyParseDatatypeScavenger() throws ScavengerCreationException {
		super(new MobyParseDatatypeProcessorFactory());
	}
}
