/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman;

import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessorFactory;
import java.lang.String;



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
	
