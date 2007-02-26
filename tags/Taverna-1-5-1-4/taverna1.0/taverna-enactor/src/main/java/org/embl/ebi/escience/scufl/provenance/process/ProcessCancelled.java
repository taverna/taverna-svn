/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package org.embl.ebi.escience.scufl.provenance.process;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Element;

/**
 * Event corresponding to a processor instance being created by the enactor's
 * scheduling mechanism.
 * 
 * @author Nikolaos Matskanis
 */
public class ProcessCancelled extends ProcessEvent {

	private Processor processor;

	/**
	 * Create a new event with no information about the processor.
	 */
	public ProcessCancelled() {
		super();
	}

	/**
	 * Create a new event corresponding to the pausing of the specified
	 * processor
	 */
	public ProcessCancelled(Processor p) {
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
