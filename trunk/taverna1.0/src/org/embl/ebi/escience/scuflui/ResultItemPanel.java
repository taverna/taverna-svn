/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import org.embl.ebi.escience.scuflui.workbench.*;

/**
 * A JPanel to represent a single result DataThing
 * to the user at the end of the workflow
 * @author Tom Oinn
 */
public class ResultItemPanel extends JPanel {
    
    final JFileChooser fc = new JFileChooser();

    public ResultItemPanel(DataThing theDataThing) {
	super(new BorderLayout());
	// Construct the scrollable view of the structure
	// of the DataThing
	final JTree structureTree = new JTree(DataThingTreeFactory.getTree(theDataThing));
	structureTree.setCellRenderer(DataThingTreeFactory.getRenderer());
	JLabel label = new JLabel("Select results from the tree to the left");
	label.setPreferredSize(new Dimension(400,40));
	label.setBackground(Color.white);
	final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						    new JScrollPane(structureTree),
						    label);
	// Add an action listener to display the data, currently
	// handle text and image types only (reasonable enough)
	structureTree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    DataThingTreeNode node = (DataThingTreeNode)structureTree.getLastSelectedPathComponent();
		    if (node != null) {
			if (node.isLeaf()) {
			    // Only interested in leaf nodes as they contain the data
			    Object userObject = node.getUserObject();
			    DataThing theDataThing = node.getDataThing();
			    String syntacticType = theDataThing.getSyntacticTypeForObject(userObject);
			    String mimeTypes = syntacticType.split("'")[1].toLowerCase();
			    if (mimeTypes.matches(".*text/.*")) {
				// Create a new text area
				JTextArea theTextArea = new JTextArea((String)userObject);
				theTextArea.setFont(new Font("Monospaced",Font.PLAIN,12));
				splitPane.setRightComponent(new JScrollPane(theTextArea));
			    }
			    else if (mimeTypes.matches(".*image/.*")) {
				// Create a new image
				ImageIcon theImage = new ImageIcon((byte[])userObject);
				JPanel theImagePanel = new JPanel();
				theImagePanel.add(new JLabel(theImage));
				theImagePanel.setPreferredSize(new Dimension(theImage.getIconWidth(), theImage.getIconHeight()));
				splitPane.setRightComponent(new JScrollPane(theImagePanel));
			    }
			    
			}
		    }
		}
	    });
	// Add a mouse listener to allow the user to save results to disc
	structureTree.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    if (e.isPopupTrigger()) {
			doEvent(e);
		    }
		}
		public void mouseReleased(MouseEvent e) {
		    if (e.isPopupTrigger()) {
			doEvent(e);
		    }
		}
		void doEvent(MouseEvent e) {
		    final DataThingTreeNode node = 
			(DataThingTreeNode)(structureTree.getPathForLocation(e.getX(),e.getY()).getLastPathComponent());
		    if (node.isLeaf()) {
			final Object theDataObject = node.getUserObject();
			// Can only save on leaf nodes
			JPopupMenu theMenu = new JPopupMenu();
			JMenuItem saveAction = new JMenuItem("Save to file",Workbench.saveIcon);
			saveAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
				    try {
					// Popup a save dialog and allow the user to store
					// the data to disc
					int returnVal = fc.showSaveDialog(ResultItemPanel.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
					    File file = fc.getSelectedFile();
					    FileOutputStream fos = new FileOutputStream(file);
					    if (theDataObject instanceof byte[]) {
						fos.write((byte[])theDataObject);
					    }
					    else {
						fos.write(((String)theDataObject).getBytes());
					    }
					    fos.flush();
					    fos.close();
					}
				    }
				    catch (IOException ioe) {
					JOptionPane.showMessageDialog(null,
								      "Problem saving data : \n"+ioe.getMessage(),
								      "Exception!",
								      JOptionPane.ERROR_MESSAGE);
				    }
				}
			    });
			
			theMenu.add(saveAction);
			theMenu.show(structureTree, e.getX(), e.getY());
		    }
		}
	    });
	add(splitPane, BorderLayout.CENTER);
    }

}
