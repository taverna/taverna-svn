/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.embl.ebi.escience.scufl.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.embl.ebi.escience.scuflui.workbench.*;

/**
 * Wraps a ScavengerTree to provide a toolbar including a search
 * by regular expression within the tree option.
 * @author Tom Oinn
 */
public class ScavengerTreePanel extends JPanel
    implements ScuflUIComponent {
    
    ScavengerTree tree;
    JTextField regex = null;
    JButton find;

    public ScavengerTreePanel() {
	super();
	setLayout(new BorderLayout());
	tree = new ScavengerTree(true);
	JScrollPane treePane = new JScrollPane(tree);
	treePane.setPreferredSize(new Dimension(0,0));
	add(treePane, BorderLayout.CENTER);
	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.add(new JLabel(" Search list "));
	regex = new JTextField();
	regex.setPreferredSize(new Dimension(100,25));
	regex.setMaximumSize(new Dimension(100,25));
	toolbar.add(regex);
	find = new JButton(ScuflIcons.findIcon);
	find.setPreferredSize(new Dimension(25,25));
	find.setEnabled(false);
	toolbar.add(find);
	toolbar.addSeparator();
	toolbar.add(Box.createHorizontalGlue());
	add(toolbar, BorderLayout.PAGE_START);
	
	// Add an action listener to the button to find the
	// nodes matching the supplied regex.
	find.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    jumpToAndHighlight();
		}
	    });
	// Add an action listener to the text field to catch
	// return being hit with it in focus
	regex.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (regex.getText().equals("") == false) {
			jumpToAndHighlight();
		    }
		}
	    });
	// Add a document listener to the text field to enable
	// the regex search if there's any text there
	regex.getDocument().addDocumentListener(new DocumentListener() {
		public void insertUpdate(DocumentEvent e) {
		    checkStatus();
		}
		public void removeUpdate(DocumentEvent e) {
		    checkStatus();
		}
		public void changedUpdate(DocumentEvent e) {
		    checkStatus();
		}
		private void checkStatus() {
		    // Always remove highlight information as it is now
		    // no longer in synch with the regex in the text field
		    cancelHighlight();
		    // Check whether the search button should be enabled
		    if (regex.getText().equals("") == false) {
			find.setEnabled(true);
		    }
		    else {
			find.setEnabled(false);
		    }
		}
	    });
	
    }

    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
	// Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
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

    private void cancelHighlight() {
	ScuflModelExplorerRenderer r = (ScuflModelExplorerRenderer)tree.getCellRenderer();
	r.setPattern(null);
	tree.repaint();
    }

    private void jumpToAndHighlight() {
	String regexString = ".*"+regex.getText().toLowerCase()+".*";
	// Update the renderer to colour the cells correctly based on match
	ScuflModelExplorerRenderer r = (ScuflModelExplorerRenderer)tree.getCellRenderer();
	r.setPattern(regexString);
	expandAll(tree, false);
	DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot(); 
	Enumeration en = rootNode.depthFirstEnumeration();
	while (en.hasMoreElements()) {
	    DefaultMutableTreeNode theNode = (DefaultMutableTreeNode)en.nextElement();
	    if (theNode.getUserObject().toString().toLowerCase().matches(regexString)) {
		TreePath path = new TreePath(treeModel.getPathToRoot(theNode));
		tree.makeVisible(path);
	    }
	}
    }

    public void attachToModel(ScuflModel model) {
	tree.attachToModel(model);
    }
    
    public void detachFromModel() {
	tree.detachFromModel();
    }

    public String getName() {
	return "Available services";
    }

}
	
