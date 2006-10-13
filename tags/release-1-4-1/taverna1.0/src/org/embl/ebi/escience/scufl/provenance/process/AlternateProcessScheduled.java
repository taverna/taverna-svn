/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.jdom.*;

/**
 * Event corresponding to an alternate processor instance being created by the
 * enactor's scheduling mechanism.
 * 
 * @author Tom Oinn
 */
public class AlternateProcessScheduled extends ProcessScheduled {

	/**
	 * Create a new event with no information about the processor being
	 * scheduled. Should use ProcessScheduled(Processor) by preference.
	 */
	public AlternateProcessScheduled() {
		super();
	}

	/**
	 * Create a new event corresponding to the scheduling of the specified
	 * processor
	 */
	public AlternateProcessScheduled(Processor p) {
		super(p);
	}

}
