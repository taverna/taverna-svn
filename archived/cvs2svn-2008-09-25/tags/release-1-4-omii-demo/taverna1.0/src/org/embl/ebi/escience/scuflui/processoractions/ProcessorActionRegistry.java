/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.processoractions;

import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;

import java.util.*;

import org.embl.ebi.escience.scufl.Processor;

/**
 * Registry containing all registered ProcessorActionSPI implementations
 * @author Tom Oinn
 */
public class ProcessorActionRegistry {
    
    private static ProcessorActionRegistry instance = null;
    private List actions = new ArrayList();

    private ProcessorActionRegistry() {
	// Prevent this class from being instantiated except through
	// the static instance() method
    }

    /**
     * Return a static instance of the registry loaded with 
     * all available instances of the ProcessorActionSPI
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
	SPInterface spif = new SPInterface(ProcessorActionSPI.class);
	ClassLoaders loaders = new ClassLoaders();
	loaders.put(classLoader);
	Enumeration spe = Service.providers(spif, loaders);
	while (spe.hasMoreElements()) {
	    ProcessorActionSPI spi = (ProcessorActionSPI)spe.nextElement();
	    actions.add(spi);
	}
    }
    
    /**
     * Return a List containing all instances of the ProcessorActionSPI
     * which think they can operate on the specified processor
     */
    public List getActions(Processor processor) {
	List result = new ArrayList();
	for (Iterator i = actions.iterator(); i.hasNext();) {
	    ProcessorActionSPI spi = (ProcessorActionSPI)i.next();
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
