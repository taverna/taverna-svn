/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.tree.*;
import java.util.*;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.*;

/**
 * A demo of the ScuflModelExplorer component
 * @author Tom Oinn
 */
public class ScuflModelExplorerDemo extends JFrame {
    
    ScuflModelExplorer explorer = new ScuflModelExplorer();
    
    ScuflModel model = new ScuflModel();

    public ScuflModelExplorerDemo() {
	super("Scufl Model Explorer Demo Application");
	JScrollPane view = new JScrollPane(explorer);
	getContentPane().add(view);
    }
    
    /**
     * Load the model definition from the XScufl file
     * specified as the first argument
     */
    public static void main(String[] args) throws Exception {
	// Create a new ScuflModelExplorer frame
	ScuflModelExplorerDemo frame = new ScuflModelExplorerDemo();
	frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
	String filename = args[0];
	File inputFile = new File(filename);
	XScuflParser.populate(inputFile.toURL().openStream(), frame.model, null);
	frame.explorer.attachToModel(frame.model);
	TreeNode root = (TreeNode)(frame.explorer).getModel().getRoot();
	// Traverse tree from root
        frame.expandAll(frame.explorer, new TreePath(root), true);
	frame.pack();
	frame.setVisible(true);
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
	// Ignores nodes who's userObject is a Processor type to
	// avoid overloading the UI with nodes at startup.
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0 && (((DefaultMutableTreeNode)node).getUserObject() instanceof Processor == false)) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
		if (((DefaultMutableTreeNode)n).getUserObject() instanceof Processor) {
		}
		else {
		    expandAll(tree, path, expand);
		}
	    }
        }
	// Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    

}
	
