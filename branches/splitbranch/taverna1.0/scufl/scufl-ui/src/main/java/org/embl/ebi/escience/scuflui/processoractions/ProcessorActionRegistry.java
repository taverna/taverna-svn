/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.processoractions;

import java.util.ArrayList;
import java.util.List;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * Registry containing all registered ProcessorActionSPI implementations
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class ProcessorActionRegistry extends TavernaSPIRegistry<ProcessorActionSPI> {

	private static ProcessorActionRegistry instance = null;

	private List<ProcessorActionSPI> actions = new ArrayList<ProcessorActionSPI>();

	private ProcessorActionRegistry() {
		// Prevent this class from being instantiated except through
		// the static instance() method
		super(ProcessorActionSPI.class);
	}

	/**
	 * Return a static instance of the registry loaded with all available
	 * instances of the ProcessorActionSPI
	 */
	public static synchronized ProcessorActionRegistry instance() {
		if (instance == null) {
			instance = new ProcessorActionRegistry();
			instance.loadInstances(ProcessorActionRegistry.class.getClassLoader());
		}
		return instance;
	}

	/**
	 * Discover and load instances
	 */
	private void loadInstances(ClassLoader classLoader) {
		actions = findComponents(classLoader);
	}

	/**
	 * Return a List containing all instances of the ProcessorActionSPI which
	 * think they can operate on the specified processor
	 */
	public List getActions(Processor processor) {
		List result = new ArrayList();
		for (ProcessorActionSPI spi : actions) {
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
		return this.actions;
	}

}
