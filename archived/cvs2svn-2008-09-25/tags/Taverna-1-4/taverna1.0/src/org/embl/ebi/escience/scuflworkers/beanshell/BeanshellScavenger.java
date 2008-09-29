/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessorFactory;
/**
 * A scavenger that knows how to create beanshell processors
 * @author Tom Oinn
 */
public class BeanshellScavenger extends Scavenger {
    
    /**
     * Create a new Beanshell scavenger
     */
    public BeanshellScavenger()
	throws ScavengerCreationException {
	super(new BeanshellProcessorFactory());
    }
}
	
