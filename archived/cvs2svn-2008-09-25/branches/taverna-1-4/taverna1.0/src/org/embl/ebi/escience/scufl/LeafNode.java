/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import javax.swing.tree.*;

/**
 * A leaf node in an iterator tree model
 * 
 * @author Tom Oinn
 */
public class LeafNode extends DefaultMutableTreeNode {
	public LeafNode(String name) {
		super(name);
	}
}
