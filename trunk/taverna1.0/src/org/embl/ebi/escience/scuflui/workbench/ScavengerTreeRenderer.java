/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import org.embl.ebi.escience.scuflui.*;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.embl.ebi.escience.scufl.*;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Object;



/**
 * A cell renderer that paints the appropriate icons depending on the
 * component of the model being displayed. This renderer is for the
 * ScavengerTree class.
 * @author Tom Oinn
 */
public class ScavengerTreeRenderer extends ScuflModelExplorerRenderer {
    
    /**
     * Return a custom renderer to draw the cell correctly for each node type
     */
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, sel,
					   expanded, leaf, row,
					   hasFocus);
	Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
	if (userObject instanceof ProcessorFactory) {
	    if (userObject instanceof SoaplabProcessorFactory) {
		setIcon(soaplabicon);
	    }
	}
	return this;
    }
}
