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
    JCheckBox watchLoads = new JCheckBox("Watch loads",true);
    boolean isWatchingLoads = true;
    ScuflModelEventListener eventListener = new ScuflModelEventListener() {
	    public void receiveModelEvent(ScuflModelEvent event) {
		if (event.getEventType() == ScuflModelEvent.LOAD &&
		    ScavengerTreePanel.this.isWatchingLoads) {
		    new Thread() {
			public void run() {
			    try {
				ScavengerTreePanel.this.tree.addScavengersFromModel();
			    }
			    catch (Exception ex) {
				// Ignore silently
			    }
			}
		    }.start();
		}
	    }
	};
    
    public ScavengerTreePanel() {
	this(true);
    }

    public ScavengerTreePanel(boolean populated) {
	super();
	setLayout(new BorderLayout());
	tree = new ScavengerTree(populated);
	JScrollPane treePane = new JScrollPane(tree);
	treePane.setPreferredSize(new Dimension(0,0));
	add(treePane, BorderLayout.CENTER);
	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.add(new JLabel(" Search list "));
	regex = new JTextField();
	regex.setPreferredSize(new Dimension(100,20));
	regex.setMaximumSize(new Dimension(100,20));
	toolbar.add(regex);
	find = new JButton(ScuflIcons.findIcon);
	find.setPreferredSize(new Dimension(20,20));
	find.setEnabled(false);
	toolbar.add(find);
	toolbar.addSeparator();
	toolbar.add(watchLoads);
	toolbar.add(Box.createHorizontalGlue());
	add(toolbar, BorderLayout.PAGE_START);
	// Add the filedrop to the toolbar, we can't add it to the main
	// panel because that's already looking out for drag and drop events
	// from the explorer
	new FileDrop(toolbar, new FileDrop.Listener() {
		public void filesDropped(File[] files) {
		    for (int i = 0; i < files.length; i++) {
			try {
			    ScavengerTreePanel.this.tree.addScavenger(new FileScavenger(files[i]));
			}
			catch (ScavengerCreationException sce) {
			    sce.printStackTrace();
			}
		    }
		}
	    });


	// Add an event listener to kick the contained tree
	// into fetching processor factories from loaded
	// workflows if the watchLoads checkbox is true
	watchLoads.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) {
		    if (e.getStateChange() == ItemEvent.DESELECTED) {
			ScavengerTreePanel.this.isWatchingLoads = false;
		    }
		    else {		
			ScavengerTreePanel.this.isWatchingLoads = true;
		    }
		}
	    });
	
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
	if (regex.getText().equals("")) {
	    r.setPattern(null);
	}
	else {
	    String regexString = ".*"+regex.getText().toLowerCase()+".*";
	    r.setPattern(regexString);
	}
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
    
    private ScuflModel model = null;
    
    public void attachToModel(ScuflModel model) {
	this.model = model;
	tree.attachToModel(model);
	model.addListener(eventListener);
    }
    
    public void detachFromModel() {
	model.removeListener(eventListener);
	tree.detachFromModel();
	this.model = null;
    }

    public String getName() {
	return "Available services";
    }
    
    public javax.swing.ImageIcon getIcon() {
	return ScuflIcons.windowScavenger;
    }

}
	
