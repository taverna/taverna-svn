/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.tools.apiconsumer;

import javax.swing.tree.*;
import javax.swing.*;
import com.sun.javadoc.*;
import java.awt.*;
import java.util.*;

/**
 * A JTree to wrap a ClassTreeModel instance providing
 * sensible icons and name handling for the types within
 * that data structure.
 * @author Tom Oinn
 */
public class ClassTree extends JTree {
    
    public ClassTree(ClassTreeModel model, APIDescription description) {
	super(model);
	putClientProperty("JTree.lineStyle", "Angled");
	setCellRenderer(new ClassTree.Renderer(description));
	setRootVisible(false);
	setExpansion(true);
    }

    /**
     * Expand or collapse all nodes, expand if the flag is true and
     * collapse otherwise
     */
    public void setExpansion(boolean expand) {
	synchronized (getModel()) {
	    TreeNode root = (TreeNode)getModel().getRoot();
	    expandAll(this, new TreePath(root), expand);
	}
    }

    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
	// Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    class Renderer extends DefaultTreeCellRenderer {
	
	ImageIcon leafIcon, folderExpanded, folderClosed;
	APIDescription description;

	public Renderer(APIDescription theDescription) {
	    super();
	    this.description = theDescription;
	    leafIcon = new ImageIcon(getClass().getResource("icons/bean.gif"));
	    folderExpanded = new ImageIcon(getClass().getResource("icons/folder-open.png"));
	    folderClosed = new ImageIcon(getClass().getResource("icons/folder-closed.png"));
	}
	
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
	    if (!leaf) {
		if (expanded) {
		    setIcon(folderExpanded);
		}
		else {
		    setIcon(folderClosed);
		}
		setText("<html><font color=\"#666666\">"+userObject.toString()+"</font></html>");
	    }
	    else {
		setIcon(leafIcon);
		if (userObject instanceof ClassDoc) {
		    StringBuffer name = new StringBuffer();
		    // Does this ClassDoc have methods expose in the APIDescription?
		    // If so then how many?
		    ClassDoc cd = (ClassDoc)userObject;
		    int numberOfMethodsUsed = description.getNumberOfMethodsUsed(cd);
		    String className = cd.typeName();
		    if (cd.isAbstract()) {
			className = className + "&nbsp;&lt;abstract&gt;";
		    }
		    name.append("<html><body>");
		    if (numberOfMethodsUsed > 0) {
			name.append("<font color=\"blue\">");
		    }
		    name.append(className);
		    if (numberOfMethodsUsed > 0) {
			name.append("&nbsp;"+numberOfMethodsUsed);
		    }
		    if (numberOfMethodsUsed > 0) {
			name.append("</font>");
		    }
		    name.append("</body></html>");
		    String nameString = name.toString();
		    setText(nameString);
		}
	    }
	    return this;
	}
    }

}
