/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import javax.swing.tree.DefaultMutableTreeNode;




/**
 * A Scavenger that knows how to load nested workflow scripts
 * @author Tom Oinn
 */
public class WorkflowScavenger extends Scavenger {

    /**
     * Create a new Talisman scavenger, the single parameter
     * should be resolvable to a location from which the 
     * tscript could be fetched.
     */
    public WorkflowScavenger(String definitionURL)
	throws ScavengerCreationException {
	super("XScufl @ "+definitionURL);
	WorkflowProcessorFactory wpf = new WorkflowProcessorFactory(definitionURL);
	DefaultMutableTreeNode factoryNode = new DefaultMutableTreeNode(wpf);
	add(factoryNode);
    }
}
	
