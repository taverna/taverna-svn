/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingTreeFactory;
import org.embl.ebi.escience.baclava.factory.DataThingTreeNode;
import org.embl.ebi.escience.baclava.factory.DataThingTreeTransferHandler;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.RendererSPI;

/**
 * A JPanel to represent a single result DataThing to the user at the end of the
 * workflow
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public class ResultItemPanel extends JPanel {
	Logger LOG = Logger.getLogger(ResultItemPanel.class);

	final JFileChooser fc = new JFileChooser();

	final RendererRegistry renderers;

	WorkflowInstance workflowInstance;

	String processorName = null;

	String portName = null;

	private static WorkflowEventDispatcher DISPATCHER = WorkflowEventDispatcher.DISPATCHER;

	public void setSelectedPort(String processor, String port) {
		processorName = processor;
		portName = port;
	}

	public ResultItemPanel(DataThing theDataThing) {
		this(theDataThing, RendererRegistry.instance());
	}

	public ResultItemPanel(DataThing theDataThing,
			WorkflowInstance workflowInstance) {
		this(theDataThing, RendererRegistry.instance());
		this.workflowInstance = workflowInstance;
	}

	public ResultItemPanel(final DataThing theDataThing,
			RendererRegistry renderers) {
		super(new BorderLayout());

		this.renderers = renderers;

		// Construct the scrollable view of the structure
		// of the DataThing by using a copy.
		final DataThing newDataThing = (DataThing) theDataThing.clone();
		final TreeNode tn = DataThingTreeFactory.getTree(newDataThing);
		final JTree structureTree = new JTree(tn) {
			// public Dimension getMinimumSize() {
			// return getPreferredSize();
			// }
		};
		// Fix for look and feel problems with multiline labels.
		structureTree.setRowHeight(0);
		structureTree.setCellRenderer(DataThingTreeFactory.getRenderer());
		// structureTree.setModel(tm);
		new DataThingTreeTransferHandler(structureTree,
				DnDConstants.ACTION_COPY);
		String viewerHelp = "<h2>Result browser</h2>Click on items in the tree to the left of this panel to select them and show their values in this area. Right clicking on an item within the tree will allow you to select different rendering options that might be available, for example displaying an XML file as text or as a navigable tree.";
		JEditorPane help = new JEditorPane("text/html", viewerHelp);
		help.setPreferredSize(new Dimension(200, 100));
		help.setEditable(false);
		JScrollPane helpPanel = new JScrollPane(help);
		helpPanel.getViewport().setBackground(java.awt.Color.WHITE);
		final JSplitPane splitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(structureTree),
				helpPanel);
		splitPane.setDividerLocation(-1);
		boolean isEmptyCollection = false;
		Object data = newDataThing.getDataObject();
		if (data instanceof Collection) {
			isEmptyCollection = ((Collection) data).isEmpty();
		}

		if (!isEmptyCollection) {
			// Add an action listener to display the data
			structureTree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					DataThingTreeNode node = (DataThingTreeNode) structureTree
							.getLastSelectedPathComponent();
					if (node != null /* && node.isLeaf() */) {
						// Only interested in leaf nodes as they contain the
						// data
						DataThing dataThing = node.getNodeThing();
						RendererSPI renderer = ResultItemPanel.this.renderers
								.getRenderer(dataThing);

						if (renderer != null) {
							try {
								final JComponent component = renderer
										.getComponent(
												ResultItemPanel.this.renderers,
												dataThing);
								if (component != null) {
									JScrollPane foo = new JScrollPane(component);
									foo.getViewport().setBackground(
											java.awt.Color.WHITE);
									foo
											.setPreferredSize(new Dimension(
													100, 100));
									// Add the update button and the text area.
									JPanel panel = new JPanel(
											new java.awt.BorderLayout());
									JPanel rightPanel = new JPanel(
											new java.awt.FlowLayout());
									JPanel leftPanel = new JPanel(
											new java.awt.BorderLayout());
									JButton cmdEdit = new JButton("Change");
									cmdEdit
											.addActionListener(new ActionListener() {
												public void actionPerformed(
														ActionEvent ae) {
													DataThingTreeNode node = (DataThingTreeNode) structureTree
															.getLastSelectedPathComponent();
													if (node != null /*
																		 * &&
																		 * node.isLeaf()
																		 */)
														if (component instanceof javax.swing.text.JTextComponent) {
															DataThing dataThing = node
																	.getNodeThing();
															String oldData = (String) dataThing
																	.getDataObject();
															String newData = ((javax.swing.text.JTextComponent) component)
																	.getText();
															if (!oldData
																	.equals(newData)) {
																String oldLSID = newDataThing
																		.getLSID(newDataThing
																				.getDataObject());
																DataThing thing = newDataThing
																		.drillAndSet(
																				dataThing,
																				newData);
																if (thing != null
																		&& workflowInstance
																				.changeOutputPortTaskData(
																						processorName,
																						portName,
																						newDataThing)) {
																	DISPATCHER
																			.fireUserChangedData(new UserChangedDataEvent(
																					workflowInstance,
																					oldLSID,
																					newDataThing));
																	// System.out.println("DATATHIN
																	// CHANGED
																	// EVENT!!!!");

																	// Update
																	// the JTree
																	structureTree
																			.setEditable(true);
																	((DefaultMutableTreeNode) tn)
																			.removeAllChildren();
																	DefaultMutableTreeNode root = (DefaultMutableTreeNode) structureTree
																			.getModel()
																			.getRoot();
																	root
																			.removeAllChildren();
																	DataThingTreeNode rootDataThing = DataThingTreeFactory
																			.getTree(newDataThing);
																	DefaultTreeModel newTree = new DefaultTreeModel(
																			rootDataThing);
																	structureTree
																			.setModel(newTree);
																	structureTree
																			.setEditable(false);
																	structureTree
																			.repaint();
																}
															}
														} else {
															// TODO for other
															// renderers
															// newDataThing.setDataObject(((javax.swing.JTable)component).getValueAt(0,0));
														}
												}
											});
									leftPanel.add(foo);
									rightPanel.add(cmdEdit);
									panel.add(leftPanel, BorderLayout.CENTER);
									if (processorName != null
											&& workflowInstance
													.isDataNonVolatile(processorName))
										panel
												.add(rightPanel,
														BorderLayout.EAST);
									panel.setPreferredSize(new Dimension(200,
											80));
									splitPane.setRightComponent(panel);
									// Reset the widths of the split Pane to
									// show the entire tree
									splitPane.setDividerLocation(-1);
								}
							} catch (RendererException re) {
								// we should print up some message about the
								// problem

								// and then log this
								LOG.error("Problem loading renderer", re);
							} catch (Throwable otherError) {
								LOG
										.error(
												"Unexpected error occured during panel construction",
												otherError);
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
				final DataThingTreeNode node = (DataThingTreeNode) (structureTree
						.getPathForLocation(e.getX(), e.getY())
						.getLastPathComponent());
				final Object theDataObject = node.getUserObject();
				// Can only save on leaf nodes
				JPopupMenu theMenu = new JPopupMenu();
				JMenuItem saveAction = new JMenuItem("Save to file",
						TavernaIcons.saveIcon);
				saveAction.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						try {
							// Popup a save dialog and allow the user to store
							// the data to disc
							Preferences prefs = Preferences
									.userNodeForPackage(ResultItemPanel.class);
							String curDir = prefs.get("currentDir", System
									.getProperty("user.home"));
							fc.setCurrentDirectory(new File(curDir));
							int returnVal = fc
									.showSaveDialog(ResultItemPanel.this);
							if (returnVal == JFileChooser.APPROVE_OPTION) {
								prefs.put("currentDir", fc
										.getCurrentDirectory().toString());
								File file = fc.getSelectedFile();
								FileOutputStream fos = new FileOutputStream(
										file);
								if (theDataObject instanceof byte[]) {
									// Byte
									fos.write((byte[]) theDataObject);
									fos.flush();
									fos.close();
								} else {
									// String
									Writer out = new BufferedWriter(
											new OutputStreamWriter(fos));
									out.write((String) theDataObject);
									out.flush();
									out.close();
								}
							}
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null,
									"Problem saving data : \n"
											+ ioe.getMessage(), "Exception!",
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

				Iterator renderers = ResultItemPanel.this.renderers
						.getRenderers(nodeThing).iterator();
				if (!isEmptyCollection && renderers.hasNext()) {
					JMenu viewers = new JMenu("Viewers");

					while (renderers.hasNext()) {
						final RendererSPI renderer = (RendererSPI) renderers
								.next();
						viewers.add(new JMenuItem(new AbstractAction(renderer
								.getName(), renderer.getIcon(
								ResultItemPanel.this.renderers, nodeThing)) {
							public void actionPerformed(ActionEvent e) {
								try {
									final JComponent component = renderer
											.getComponent(
													ResultItemPanel.this.renderers,
													nodeThing);
									if (ui != null) {
										JScrollPane jp = new JScrollPane(
												component);
										jp.getViewport().setBackground(
												java.awt.Color.WHITE);

										// Add the update button and the text
										// area.
										JPanel panel = new JPanel(
												new java.awt.BorderLayout());
										JPanel leftPanel = new JPanel(
												new java.awt.BorderLayout());
										JPanel rightPanel = new JPanel();
										JButton cmdEdit = new JButton("Change");
										cmdEdit
												.addActionListener(new ActionListener() {
													public void actionPerformed(
															ActionEvent ae) {
														DataThingTreeNode node = (DataThingTreeNode) structureTree
																.getLastSelectedPathComponent();
														if (node != null /*
																			 * &&
																			 * node.isLeaf()
																			 */)
															if (component instanceof javax.swing.text.JTextComponent) {
																DataThing dataThing = node
																		.getNodeThing();
																String oldData = (String) dataThing
																		.getDataObject();
																String newData = ((javax.swing.text.JTextComponent) component)
																		.getText();
																if (!oldData
																		.equals(newData)) {
																	String oldLSID = newDataThing
																			.getLSID(newDataThing
																					.getDataObject());
																	DataThing thing = newDataThing
																			.drillAndSet(
																					dataThing,
																					newData);
																	if (thing != null
																			&& workflowInstance
																					.changeOutputPortTaskData(
																							processorName,
																							portName,
																							newDataThing)) {
																		DISPATCHER
																				.fireUserChangedData(new UserChangedDataEvent(
																						workflowInstance,
																						oldLSID,
																						newDataThing));
																		// System.out.println("DATATHIN
																		// CHANGED
																		// EVENT!!!!");

																		// Update
																		// the
																		// JTree
																		structureTree
																				.setEditable(true);
																		((DefaultMutableTreeNode) tn)
																				.removeAllChildren();
																		DefaultMutableTreeNode root = (DefaultMutableTreeNode) structureTree
																				.getModel()
																				.getRoot();
																		root
																				.removeAllChildren();
																		DataThingTreeNode rootDataThing = DataThingTreeFactory
																				.getTree(newDataThing);
																		DefaultTreeModel newTree = new DefaultTreeModel(
																				rootDataThing);
																		structureTree
																				.setModel(newTree);
																		structureTree
																				.setEditable(false);
																		structureTree
																				.repaint();
																		// structureTree.update(structureTree.getGraphics());
																	}
																}
															} else {
																// TODO for
																// other
																// renderers
																// newDataThing.setDataObject(((javax.swing.JTable)component).getValueAt(0,0));
															}
													}
												});
										panel.removeAll();
										leftPanel.add(jp);
										rightPanel.add(cmdEdit);
										panel.add(leftPanel,
												BorderLayout.CENTER);
										if (processorName != null
												&& workflowInstance
														.isDataNonVolatile(processorName))
											panel.add(rightPanel,
													BorderLayout.EAST);
										panel.setPreferredSize(new Dimension(
												200, 80));

										splitPane.setRightComponent(panel);
									}
								} catch (RendererException re) {
									// should be informing the user something is
									// wrong

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

	// VERY TEMP
	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public void expandAll(JTree tree, boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (java.util.Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
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

}
