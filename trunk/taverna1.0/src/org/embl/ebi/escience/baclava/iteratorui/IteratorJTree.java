/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.iteratorui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.baclava.*;
import java.util.*;

/**
 * A JTree placed on top of a tree made of iterator
 * nodes from the baclava package, it allows the user
 * to configure the iteration strategy of the underlying
 * processor.
 * @author Tom Oinn
 */
public class IteratorJTree extends JTree {

    static {
	try {
	    Class c = Class.forName("org.embl.ebi.escience.baclava.iteratorui.IteratorJTree");
	    joinIteratorIcon = new ImageIcon(c.getResource("crossproducticon.png"));
	    lockStepIteratorIcon = new ImageIcon(c.getResource("dotproducticon.png"));
	    baclavaIteratorIcon = new ImageIcon(c.getResource("baclavaiteratoricon.png"));
	}
	catch (Exception ex) {
	    //
	}				    
    }

    private static ImageIcon joinIteratorIcon, lockStepIteratorIcon, baclavaIteratorIcon;

    public IteratorJTree(IteratorTreeModel model) {
	super(model);
	this.treeModel = new DefaultTreeModel(new JoinIteratorNode());
	setCellRenderer(new DefaultTreeCellRenderer() {
		public Component getTreeCellRendererComponent(JTree tree,
							      Object value,
							      boolean selected,
							      boolean expanded,
							      boolean leaf,
							      int row,
							      boolean hasFocus) {
		    super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
		    if (value instanceof JoinIteratorNode) {
			setIcon(IteratorJTree.joinIteratorIcon);
		    }
		    else if (value instanceof LockStepIteratorNode) {
			setIcon(IteratorJTree.lockStepIteratorIcon);
		    }
		    else if (value instanceof BaclavaIteratorNode) {
			setIcon(IteratorJTree.baclavaIteratorIcon);
			setText(((BaclavaIteratorNode)value).getName());
		    }
		    return this;
		}		    
	    });
	//
    }

}
