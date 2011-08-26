/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import javax.swing.tree.DefaultMutableTreeNode;

import java.lang.Object;



/**
 * A subclass of DefaultMutableTreeNode that should
 * be subclassed to create particular scavengers for
 * different types of processor
 * @author Tom Oinn
 */
public class Scavenger extends DefaultMutableTreeNode {

    public Scavenger(Object userObject) {
	super(userObject);
    }

}
