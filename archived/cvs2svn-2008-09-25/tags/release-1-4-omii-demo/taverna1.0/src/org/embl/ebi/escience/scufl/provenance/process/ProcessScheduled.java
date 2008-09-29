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
 * Event corresponding to a processor instance being created by the enactor's
 * scheduling mechanism.
 * 
 * @author Tom Oinn
 */
public class ProcessScheduled extends ProcessEvent {

	private Processor processor;

	/**
	 * Create a new event with no information about the processor being
	 * scheduled. Should use ProcessScheduled(Processor) by preference.
	 */
	public ProcessScheduled() {
		super();
	}

	/**
	 * Create a new event corresponding to the scheduling of the specified
	 * processor
	 */
	public ProcessScheduled(Processor p) {
		super();
		this.processor = p;
	}

	/**
	 * Return an Element with the processor spec attached, if present.
	 */
	public Element eventElement() {
		Element e = super.eventTopLevelElement();
		e.addContent(ProcessorHelper.elementForProcessor(processor));
		return e;
	}

}
