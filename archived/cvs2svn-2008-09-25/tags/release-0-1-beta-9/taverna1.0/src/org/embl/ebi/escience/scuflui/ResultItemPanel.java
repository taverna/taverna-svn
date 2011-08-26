/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.*;
import org.embl.ebi.escience.baclava.factory.DataThingTreeFactory;
import org.embl.ebi.escience.baclava.factory.DataThingTreeNode;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.RendererSPI;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.apache.log4j.Logger;

// Utility Imports
import java.util.Iterator;
import java.util.Collection;
import java.util.prefs.Preferences;

// IO Imports
import java.io.*;

import java.lang.Object;
import java.lang.String;


/**
 * A JPanel to represent a single result DataThing
 * to the user at the end of the workflow
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public class ResultItemPanel extends JPanel {
	Logger LOG = Logger.getLogger(ResultItemPanel.class);
    final JFileChooser fc = new JFileChooser();
    final RendererRegistry renderers;

    public ResultItemPanel(DataThing theDataThing)
    {
        this(theDataThing, RendererRegistry.instance());
    }

    public ResultItemPanel(DataThing theDataThing, RendererRegistry renderers) {
        super(new BorderLayout());

        this.renderers = renderers;

        // Construct the scrollable view of the structure
        // of the DataThing
        final JTree structureTree = new JTree(DataThingTreeFactory.getTree(theDataThing)) {
		public Dimension getMinimumSize() {
		    return getPreferredSize();
		}
	    };
		// Fix for look and feel problems with multiline labels.
		structureTree.setRowHeight(0);
        structureTree.setCellRenderer(DataThingTreeFactory.getRenderer());
        new DataThingTreeTransferHandler(structureTree, DnDConstants.ACTION_COPY);
        JLabel label = new JLabel("Select results from the tree to the left");
        label.setPreferredSize(new Dimension(400,40));
        label.setBackground(Color.white);
		JScrollPane foo = new JScrollPane(label);
		foo.setPreferredSize(new Dimension(100,100));
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                                    new JScrollPane(structureTree),
                                                    foo);
	splitPane.setDividerLocation(-1);
        boolean isEmptyCollection = false;
        Object data = theDataThing.getDataObject();
        if(data instanceof Collection) {
            isEmptyCollection = ((Collection) data).isEmpty();
        }

        if(!isEmptyCollection) {
            // Add an action listener to display the data
            structureTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DataThingTreeNode node = (DataThingTreeNode)structureTree.getLastSelectedPathComponent();
                    if (node != null /*&& node.isLeaf()*/) {
                        // Only interested in leaf nodes as they contain the data
                        DataThing dataThing = node.getNodeThing();
                        RendererSPI renderer =
                                ResultItemPanel.this.renderers.getRenderer(dataThing);

                        if (renderer != null) {
                          try {
                            JComponent component = renderer.getComponent(
                                    ResultItemPanel.this.renderers, dataThing);
                            if (component != null) {
				JScrollPane foo = new JScrollPane(component);
				foo.setPreferredSize(new Dimension(100,100));
                                splitPane.setRightComponent(foo);
				// Reset the widths of the split Pane to show the entire tree
				splitPane.setDividerLocation(-1);
                            }
                          } catch (RendererException re) {
                            // we should print up some message about the problem

                            // and then log this
                            LOG.error("Problem loading renderer", re);
                          }
                        }
                    }
                }
            });
        }

	    // Add a mouse listener to allow the user to save results to disc
        // and chose renderers
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
                final Object theDataObject = node.getUserObject();
                // Can only save on leaf nodes
                JPopupMenu theMenu = new JPopupMenu();
                JMenuItem saveAction = new JMenuItem("Save to file",Workbench.saveIcon);
                saveAction.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            // Popup a save dialog and allow the user to store
                            // the data to disc
                            Preferences prefs = Preferences.userNodeForPackage(
                                    ResultItemPanel.class);
                            String curDir = prefs.get(
                                    "currentDir",
                                    System.getProperty("user.home"));
                            fc.setCurrentDirectory(new File(curDir));
                            int returnVal = fc.showSaveDialog(ResultItemPanel.this);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                prefs.put("currentDir",
                                          fc.getCurrentDirectory().toString());
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
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Problem saving data : \n"+ioe.getMessage(),
                                    "Exception!",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
		if (node.isLeaf()) {
		    theMenu.add(saveAction);
		}
                // all possible viewers
                final DataThing nodeThing = node.getNodeThing();
                boolean isEmptyCollection = false;
                Object data = nodeThing.getDataObject();
                if (data instanceof Collection) {
                    isEmptyCollection = ((Collection) data).isEmpty();
                }

                Iterator renderers = ResultItemPanel.this.renderers.getRenderers(
                        nodeThing).iterator();
                if(!isEmptyCollection && renderers.hasNext()) {
                    JMenu viewers = new JMenu("Viewers");

                    while(renderers.hasNext()) {
                        final RendererSPI renderer =
                                (RendererSPI) renderers.next();
                        viewers.add(new JMenuItem(new AbstractAction(
                                renderer.getName(),
                                renderer.getIcon(ResultItemPanel.this.renderers, nodeThing))
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                              try {
                                JComponent component = renderer.getComponent(ResultItemPanel.this.renderers, nodeThing);
                                if (ui != null) {
                                  splitPane.setRightComponent(new JScrollPane(component));
                                }
                              } catch (RendererException re) {
                                // should be informing the user something is wrong

                                // log this
                                LOG.error("Unable to load renderer", re);
                              }
                            }
                        }));
                    }

                    theMenu.add(viewers);
                }

                theMenu.show(structureTree, e.getX(), e.getY());
            }
        });
        add(splitPane, BorderLayout.CENTER);
    }

}
