/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingTreeFactory;
import org.embl.ebi.escience.baclava.factory.DataThingTreeNode;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import java.util.Collection;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.util.Iterator;

// IO Imports
import java.io.*;
import java.net.*;



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
			    if(userObject instanceof Collection) {
				userObject = "Empty collection, collection type was "+userObject.getClass().getName();
			    }
			    if (mimeTypes.matches(".*text/.*")) {
				// Create a new text area
				if (mimeTypes.matches(".*text/html.*")) {
				    splitPane.setRightComponent(new JScrollPane(new JEditorPane("text/html","<pre>"+(String)userObject+"</pre>")));
				}
				else if (mimeTypes.matches(".*text/rtf.*")) {
				    splitPane.setRightComponent(new JScrollPane(new JEditorPane("text/rtf",(String)userObject)));
				}
				else if (mimeTypes.matches(".*text/x-taverna-web-url.*")) {
				    try {
					JEditorPane jep = new JEditorPane();
					jep.setPage(new URL((String)userObject));
					splitPane.setRightComponent(new JScrollPane(jep));
				    }
				    catch (Exception ex) {
					JTextArea theTextArea = new JTextArea();
					theTextArea.setText((String)userObject);
					theTextArea.setFont(new Font("Monospaced",Font.PLAIN,12));
					splitPane.setRightComponent(new JScrollPane(theTextArea));
				    }
				}
				// Handle graphviz dot file format, see comments at 
				// http://www.research.att.com/lists/graphviz-interest/msg01013.html
				// for discussion of mime types
				else if (mimeTypes.matches(".*text/x-graphviz.*")) {
				    try {
					String dotText = (String)userObject;
					Process dotProcess = Runtime.getRuntime().exec("dot -Tpng");
					OutputStream out = dotProcess.getOutputStream();
					out.write(dotText.getBytes());
					out.flush();
					out.close();
					InputStream in = dotProcess.getInputStream();
					ImageInputStream iis = ImageIO.createImageInputStream(in);
					String suffix = "png";
					Iterator readers = ImageIO.getImageReadersBySuffix( suffix );
					ImageReader imageReader = (ImageReader)readers.next();
					imageReader.setInput(iis, false);
					ImageIcon theImage = new ImageIcon(imageReader.read(0));
					JPanel theImagePanel = new JPanel();
					theImagePanel.add(new JLabel(theImage));
					theImagePanel.setPreferredSize(new Dimension(theImage.getIconWidth(), theImage.getIconHeight()));
					splitPane.setRightComponent(new JScrollPane(theImagePanel));
				    }
				    catch (IOException ioe) {
					JTextArea theTextArea = new JTextArea();
					theTextArea.setText((String)userObject);
					theTextArea.setFont(new Font("Monospaced",Font.PLAIN,12));
					splitPane.setRightComponent(new JScrollPane(theTextArea));
				    }
				}
				else {
				    JTextArea theTextArea = new JTextArea();
				    theTextArea.setText((String)userObject);
				    theTextArea.setFont(new Font("Monospaced",Font.PLAIN,12));
				    splitPane.setRightComponent(new JScrollPane(theTextArea));
				}
				// If text/xml then create a new split pane to show a tree version of it
				if (mimeTypes.matches(".*text/xml.*")) {
				    try {
					Component originalComponent = splitPane.getRightComponent();
					XMLTree xmlTreeDisplay = new XMLTree((String)userObject);
					JSplitPane pane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
									  new JScrollPane(xmlTreeDisplay),
									  originalComponent);
					splitPane.setRightComponent(pane2);
				    }
				    catch (Exception ex) {
					// Probably not valid xml so don't do the display change
				    }
				}
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
						// Byte
						fos.write((byte[])theDataObject);
						fos.flush();
						fos.close();
					    }
					    else {
						// String
						Writer out = new BufferedWriter(new OutputStreamWriter(fos));
						out.write((String)theDataObject);
						out.flush();
						out.close();
					    }
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
