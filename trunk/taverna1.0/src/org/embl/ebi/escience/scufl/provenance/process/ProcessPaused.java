/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package org.embl.ebi.escience.scufl.provenance.process;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflworkers.*;
import org.jdom.*;

/**
 * Event corresponding to a processor instance being created by the enactor's
 * scheduling mechanism.
 * 
 * @author Nikolaos Matskanis
 */
public class ProcessPaused extends ProcessEvent {

	private Processor processor;

	/**
	 * Create a new event with no information about the processor.
	 */
	public ProcessPaused() {
		super();
	}

	/**
	 * Create a new event corresponding to the pausing of the specified
	 * processor
	 */
	public ProcessPaused(Processor p) {
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
