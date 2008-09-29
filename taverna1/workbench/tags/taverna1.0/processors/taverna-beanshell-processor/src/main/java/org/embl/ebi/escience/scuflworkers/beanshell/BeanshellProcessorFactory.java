/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;



/**
 * Implementation of ProcessorFactory that creates
 * Beanshell nodes
 * @author Tom Oinn
 */
public class BeanshellProcessorFactory extends ProcessorFactory {

    private BeanshellProcessor prototype = null;
    
    /**
     * Create a new factory
     */
    public BeanshellProcessorFactory() {
	setName("Beanshell scripting host");

    }
    
    public BeanshellProcessorFactory(BeanshellProcessor prot) {
	setName("Beanshell scripting host");
	this.prototype = prot;
    }

    public BeanshellProcessor getPrototype() {
	return this.prototype;
    }
    
    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor that allows arbitrary Java scripts";
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor.class;
    }
    
}
