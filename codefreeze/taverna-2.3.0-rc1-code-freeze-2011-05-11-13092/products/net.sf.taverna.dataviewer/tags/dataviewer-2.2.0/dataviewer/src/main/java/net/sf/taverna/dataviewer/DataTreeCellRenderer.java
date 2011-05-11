/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.dataviewer;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.dataviewer.DataTreeNode.DataTreeNodeState;

/**
 * Inspired by {@link net.sf.taverna.t2.workbench.views.results.workflow.PortResultCellRenderer}
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class DataTreeCellRenderer extends DefaultTreeCellRenderer {
	
	public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		
		Component result = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof DataTreeNode) {
			DataTreeNode value2 = (DataTreeNode) value;
			String text = "";
			DataTreeNode parent = (DataTreeNode) value2.getParent();
			if (value2.getState().equals(DataTreeNodeState.DATA_LIST)) {
				if (value2.getChildCount() == 0) {
					text = "Empty list";
				} else {
					text = "List";
					if (!parent.getState().equals(DataTreeNodeState.ROOT)) {
						text += " " + (parent.getIndex(value2) + 1);
					}
					text += " with " + value2.getValueCount() + " value";
					if (value2.getValueCount() != 1) {
						text += "s";
					}
					if (value2.getSublistCount() > 0) {
						text += " in " + value2.getSublistCount() + " sublists";
					}
				}
			} else if (value2.getState().equals(DataTreeNodeState.DATA_ITEM)) {
				text = "Value " + (parent.getIndex(value2) + 1);
			}
			((JLabel) result).setText(text);
		}
		return result;
	}

}
