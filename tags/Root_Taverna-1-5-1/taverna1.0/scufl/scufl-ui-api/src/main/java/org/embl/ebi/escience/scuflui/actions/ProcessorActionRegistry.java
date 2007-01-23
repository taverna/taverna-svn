/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.actions;

import java.util.ArrayList;
import java.util.List;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.spi.ProcessorActionSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * Registry containing all registered ProcessorActionSPI implementations
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class ProcessorActionRegistry extends TavernaSPIRegistry<ProcessorActionSPI> {

	private static ProcessorActionRegistry instance = null;

	private ProcessorActionRegistry() {
		super(ProcessorActionSPI.class);
	}

	/**
	 * Return a static instance of the registry loaded with all available
	 * instances of the ProcessorActionSPI
	 */
	public static synchronized ProcessorActionRegistry instance() {
		if (instance == null) {
			instance = new ProcessorActionRegistry();
		}
		return instance;
	}

	/**
	 * Return a List containing all instances of the ProcessorActionSPI which
	 * think they can operate on the specified processor
	 */
	public List<ProcessorActionSPI> getActions(Processor processor) {
		List<ProcessorActionSPI> result = new ArrayList<ProcessorActionSPI>();
		for (ProcessorActionSPI spi : findComponents()) {
			if (spi.canHandle(processor)) {
				result.add(spi);
			}
		}
		return result;
	}

	/**
	 * Get all registered ProcessorActionSPI instances
	 */
	public List getAllActions() {
		return instance().findComponents();
	}

}
