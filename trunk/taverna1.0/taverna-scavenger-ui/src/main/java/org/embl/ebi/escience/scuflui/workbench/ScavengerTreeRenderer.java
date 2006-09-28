/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scuflui.treeview.ScuflModelExplorerRenderer;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * A cell renderer that paints the appropriate icons depending on the component
 * of the model being displayed. This renderer is for the ScavengerTree class.
 * 
 * @author Tom Oinn
 */
public class ScavengerTreeRenderer extends ScuflModelExplorerRenderer {

	/**
	 * Return a custom renderer to draw the cell correctly for each node type
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		setVerticalTextPosition(SwingConstants.BOTTOM);
		if (userObject instanceof ProcessorFactory) {
			ProcessorFactory pf = (ProcessorFactory) userObject;
			Class processorClass = pf.getProcessorClass();
			String tagName = ProcessorHelper
					.getTagNameForClassName(processorClass.getName());
			ImageIcon icon = ProcessorHelper.getIconForTagName(tagName);
			if (icon != null) {
				setIcon(icon);
			}
		}
		return this;
	}
}
