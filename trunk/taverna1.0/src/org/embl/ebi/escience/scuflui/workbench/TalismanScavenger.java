/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import javax.swing.tree.DefaultMutableTreeNode;




/**
 * A Scavenger that knows how to load Talisman scripts
 * @author Tom Oinn
 */
public class TalismanScavenger extends Scavenger {

    /**
     * Create a new Talisman scavenger, the single parameter
     * should be resolvable to a location from which the 
     * tscript could be fetched.
     */
    public TalismanScavenger(String scriptURL)
	throws ScavengerCreationException {
	super("TScript @ "+scriptURL);
	TalismanProcessorFactory tpf = new TalismanProcessorFactory(scriptURL);
	DefaultMutableTreeNode factoryNode = new DefaultMutableTreeNode(tpf);
	add(factoryNode);
    }
}
	
