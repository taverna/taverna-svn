/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.biomoby.shared.NoSuccessException;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

/**
 * 
 * @author Eddie An action that for BioMobyProcessors
 */
public class BiomobyAction extends AbstractProcessorAction {

	JProgressBar progressBar = new JProgressBar();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction#getComponent(org.embl.ebi.escience.scufl.Processor)
	 */

	public JComponent getComponent(Processor processor) {
		// variables i need
		BiomobyProcessor theProcessor = (BiomobyProcessor) processor;
		// Central central = theProcessor.getCentralWorker();
		final Processor theproc = processor;
		final String endpoint = ((BiomobyProcessor) processor).getMobyEndpoint();
		final ScuflModel scuflModel = processor.getModel();
		// set up the root node
		String serviceName = theProcessor.getMobyService().getName();
		String description = theProcessor.getDescription();
		MobyServiceTreeNode service = new MobyServiceTreeNode(serviceName, description);
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(service);

		// now add the child nodes containing useful information about the
		// service
		DefaultMutableTreeNode input = new DefaultMutableTreeNode("Inputs");
		DefaultMutableTreeNode output = new DefaultMutableTreeNode("Outputs");
		rootNode.add(input);
		rootNode.add(output);

		// process inputs
		MobyData[] inputs = theProcessor.getMobyService().getPrimaryInputs();
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i] instanceof MobyPrimaryDataSimple) {
				MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) inputs[i];
				StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
				MobyNamespace[] namespaces = simple.getNamespaces();
				for (int j = 0; j < namespaces.length; j++) {
					sb.append(namespaces[j].getName() + " ");
				}
				if (namespaces.length == 0)
					sb.append(" ANY ");
				MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType()
						.getName()
						+ "('" + simple.getName() + "')", sb.toString());
				input.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), input.getChildCount());
			} else {
				// we have a collection
				MobyPrimaryDataSet collection = (MobyPrimaryDataSet) inputs[i];
				DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode("Collection('"
						+ collection.getName() + "')");
				input.insert(collectionNode, input.getChildCount());
				MobyPrimaryDataSimple[] simples = collection.getElements();
				for (int j = 0; j < simples.length; j++) {
					MobyPrimaryDataSimple simple = simples[j];
					StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
					MobyNamespace[] namespaces = simple.getNamespaces();
					for (int k = 0; k < namespaces.length; k++) {
						sb.append(namespaces[k].getName() + " ");
					}
					if (namespaces.length == 0)
						sb.append(" ANY ");
					MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple
							.getDataType().getName()
							+ "('" + simple.getName() + "')", sb.toString());
					collectionNode.insert(new DefaultMutableTreeNode(mobyObjectTreeNode),
							collectionNode.getChildCount());
				}

			}
		}
		if (inputs.length == 0) {
			input.add(new DefaultMutableTreeNode(" None "));
		}

		// process outputs
		MobyData[] outputs = theProcessor.getMobyService().getPrimaryOutputs();
		for (int i = 0; i < outputs.length; i++) {
			if (outputs[i] instanceof MobyPrimaryDataSimple) {
				MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) outputs[i];
				StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
				MobyNamespace[] namespaces = simple.getNamespaces();
				for (int j = 0; j < namespaces.length; j++) {
					sb.append(namespaces[j].getName() + " ");
				}
				if (namespaces.length == 0)
					sb.append(" ANY ");
				MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple.getDataType()
						.getName()
						+ "('" + simple.getName() + "')", sb.toString());
				mobyObjectTreeNode.setNamespaces(simple.getNamespaces());
				output.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), output
						.getChildCount());
			} else {
				// we have a collection
				MobyPrimaryDataSet collection = (MobyPrimaryDataSet) outputs[i];
				DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode("Collection('"
						+ collection.getName() + "')");
				output.insert(collectionNode, output.getChildCount());
				MobyPrimaryDataSimple[] simples = collection.getElements();
				for (int j = 0; j < simples.length; j++) {
					MobyPrimaryDataSimple simple = simples[j];
					StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
					MobyNamespace[] namespaces = simple.getNamespaces();
					for (int k = 0; k < namespaces.length; k++) {
						sb.append(namespaces[k].getName() + " ");
					}
					if (namespaces.length == 0)
						sb.append("ANY ");
					MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple
							.getDataType().getName()
							+ "('" + simple.getName() + "')", sb.toString());
					mobyObjectTreeNode.setNamespaces(simple.getNamespaces());
					collectionNode.insert(new DefaultMutableTreeNode(mobyObjectTreeNode),
							collectionNode.getChildCount());
				}

			}
		}
		if (outputs.length == 0) {
			output.add(new DefaultMutableTreeNode(" None "));
		}

		// finally return a tree describing the object
		final JTree tree = new JTree(rootNode);
		tree.setCellRenderer(new BioMobyServiceTreeCustomRenderer());
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent me) {
			}

			public void mousePressed(MouseEvent me) {
				mouseReleased(me);
			}

			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) // right click, show popup menu
				{
					TreePath path = tree.getPathForLocation(me.getX(), me.getY());
					if (path == null)
						return;
					if (path.getPathCount() >= 3) {
						if (path.getPathCount() == 4
								&& path.getParentPath().getLastPathComponent().toString()
										.startsWith("Collection(")
								&& (path.getParentPath().toString()).indexOf("Inputs") > 0) {
							// we have a collection input
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
									.getLastSelectedPathComponent();
							final String selectedObject = node.toString();
							// ensure that the last selected item is an object!
							if (!selectedObject.equals(path.getLastPathComponent().toString()))
								return;
							String collectionName = "";
							if (path.getParentPath().getLastPathComponent().toString()
									.indexOf("('") > 0
									&& path.getParentPath().getLastPathComponent().toString()
											.indexOf("')") > 0) {
								collectionName = path.getParentPath().getLastPathComponent()
										.toString().substring(
												path.getParentPath().getLastPathComponent()
														.toString().indexOf("('") + 2,
												path.getParentPath().getLastPathComponent()
														.toString().indexOf("')"));
							}
							final String theCollectionName = collectionName;
							final JPopupMenu menu = new JPopupMenu();
							// Create and add a menu item for adding to the item
							// to the workflow
							JMenuItem item = new JMenuItem("Add Datatype - " + selectedObject
									+ " to the workflow?");
							item
									.setIcon(getIcon("org/biomoby/client/taverna/plugin/Add24.gif"));
							item.addActionListener(new ActionListener() {
								// private boolean added = false;

								public void actionPerformed(ActionEvent ae) {
									String defaultName = selectedObject;
									defaultName = defaultName.split("\\(")[0];
									String validName = theproc.getModel().getValidProcessorName(
											defaultName);
									Processor bop;
									try {
										bop = new BiomobyObjectProcessor(
												((BiomobyProcessor) theproc).getModel(), validName,
												"", defaultName, ((BiomobyProcessor) theproc)
														.getMobyEndpoint(), false);
										((BiomobyProcessor) theproc).getModel().addProcessor(bop);
									} catch (ProcessorCreationException pce) {
										JOptionPane.showMessageDialog(null,
												"Processor creation exception : \n"
														+ pce.getMessage(), "Exception!",
												JOptionPane.ERROR_MESSAGE);
										return;
									} catch (DuplicateProcessorNameException dpne) {
										JOptionPane.showMessageDialog(null, "Duplicate name : \n"
												+ dpne.getMessage(), "Exception!",
												JOptionPane.ERROR_MESSAGE);
										return;
									}
									try {
										if (scuflModel != null) {
											Port theServiceport = null;
											theServiceport = theproc
													.locatePort(defaultName
															+ "(Collection - '"
															+ (theCollectionName.equals("") ? "MobyCollection"
																	: theCollectionName) + "')");
											if (theServiceport == null)
												return;
											scuflModel.addDataConstraint(new DataConstraint(
													scuflModel, bop.locatePort("mobyData"),
													theServiceport));
										} else {
											System.out.println("Null model");
										}
									} catch (DataConstraintCreationException dcce) {
										// dcce.printStackTrace();
										return;
									} catch (UnknownPortException e) {
										// e.printStackTrace();
										return;
									}
								}
							});
							// Create and add a menu item for service details
							JMenuItem details = new JMenuItem("Find out about " + selectedObject);
							details
									.setIcon(getIcon("org/biomoby/client/taverna/plugin/Information24.gif"));
							details.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									Dimension loc = new Dimension(100, 100);
									Dimension size = new Dimension(450, 450);
									UIComponentSPI c = new MobyPanel(
									// TODO create a valid description
											selectedObject, "A BioMoby Object Description", "");
									UIUtils.createFrame((ScuflModel) null, c, (int) loc.getWidth(),
											(int) loc.getHeight(), (int) size.getWidth(),
											(int) size.getHeight());
								}
							});
							// add the components to the menu
							menu.add(new JLabel("Add to workflow ... ", JLabel.CENTER));
							menu.add(new JSeparator());
							menu.add(item);
							menu.add(new JSeparator());
							menu.add(new JLabel("Datatype Details ... ", JLabel.CENTER));
							menu.add(new JSeparator());
							menu.add(details);
							// show the window
							menu.show(me.getComponent(), me.getX(), me.getY());
						} else if (path.getPathCount() == 3
								&& path.getParentPath().getLastPathComponent().toString()
										.startsWith("Inputs")
								&& !path.getLastPathComponent().toString()
										.startsWith("Collection(")
								&& !path.getLastPathComponent().toString().equals(" None ")) {
							// we have a simple input
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
									.getLastSelectedPathComponent();
							if (node == null)
								return;
							final String selectedObject = node.toString();
							// ensure that the last selected item is an object!
							if (!selectedObject.equals(path.getLastPathComponent().toString()))
								return;

							final JPopupMenu menu = new JPopupMenu();
							// Create and add a menu item for adding to the item
							// to the workflow
							JMenuItem item = new JMenuItem("Add Datatype - " + selectedObject
									+ " to the workflow?");
							item
									.setIcon(getIcon("org/biomoby/client/taverna/plugin/Add24.gif"));
							item.addActionListener(new ActionListener() {
								// private boolean added = false;

								public void actionPerformed(ActionEvent ae) {
									String defaultName = selectedObject;
									defaultName = defaultName.split("\\(")[0];
									String validName = theproc.getModel().getValidProcessorName(
											defaultName);
									Processor bop;
									try {
										bop = new BiomobyObjectProcessorFactory(
												((BiomobyProcessor) theproc).getMobyEndpoint(), "",
												defaultName).createProcessor(validName, theproc
												.getModel());
									} catch (ProcessorCreationException pce) {
										JOptionPane.showMessageDialog(null,
												"Processor creation exception : \n"
														+ pce.getMessage(), "Exception!",
												JOptionPane.ERROR_MESSAGE);
										return;
									} catch (DuplicateProcessorNameException dpne) {
										JOptionPane.showMessageDialog(null, "Duplicate name : \n"
												+ dpne.getMessage(), "Exception!",
												JOptionPane.ERROR_MESSAGE);
										return;
									}

									try {
										if (scuflModel != null) {
											Port theServiceport = null;
											String inputPortName = selectedObject.replaceAll("'",
													"");
											if (inputPortName.indexOf("()") > 0)
												inputPortName = inputPortName.replaceAll("\\(\\)",
														"\\(_ANON_\\)");
											theServiceport = theproc.locatePort(inputPortName);
											if (theServiceport == null)
												return;
											scuflModel.addDataConstraint(new DataConstraint(
													scuflModel, bop.locatePort("mobyData"),
													theServiceport));
										} else {
											System.out.println("Null model");
										}
									} catch (DataConstraintCreationException dcce) {
										// dcce.printStackTrace();
										return;
									} catch (UnknownPortException e) {
										// e.printStackTrace();
										return;
									}
								}
							});

							// Create and add a menu item for service details
							JMenuItem details = new JMenuItem("Find out about " + selectedObject);
							details
									.setIcon(getIcon("org/biomoby/client/taverna/plugin/Information24.gif"));
							details.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									Dimension loc = new Dimension(100, 100);
									Dimension size = new Dimension(450, 450);
									UIComponentSPI c = new MobyPanel(
									// TODO create a valid description
											selectedObject, "A BioMoby Object Description",
											createDataDescription(selectedObject.split("\\(")[0],
													((BiomobyProcessor) theproc).getMobyEndpoint()));
									UIUtils.createFrame((ScuflModel) null, c, (int) loc.getWidth(),
											(int) loc.getHeight(), (int) size.getWidth(),
											(int) size.getHeight());
								}

								private String createDataDescription(String dataName,
										String mobyEndpoint) {
									MobyDataType data;
									try {
										Central central = new CentralImpl(mobyEndpoint);
										data = central.getDataType(dataName);

									} catch (MobyException e) {
										return "Couldn't retrieve a description on the BioMoby service '"
												+ dataName + "'";
									} catch (NoSuccessException e) {
										return "Couldn't retrieve a description on the BioMoby service '"
												+ dataName + "'";
									}
									return data.toString();
								}
							});
							// add the components to the menu
							menu.add(new JLabel("Add to workflow ... ", JLabel.CENTER));
							menu.add(new JSeparator());
							menu.add(item);
							menu.add(new JSeparator());
							menu.add(new JLabel("Datatype Details ... ", JLabel.CENTER));
							menu.add(new JSeparator());
							menu.add(details);
							// show the window
							menu.show(me.getComponent(), me.getX(), me.getY());

						} else if (path.getParentPath().toString().indexOf("Outputs") >= 0
								&& path.getLastPathComponent().toString().indexOf(" None ") == -1) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
									.getLastSelectedPathComponent();
							if (node == null)
								return;
							final String selectedObject = node.toString();
							if (!selectedObject.equals(path.getLastPathComponent().toString()))
								return;

							if ((path.getPathCount() == 4
									&& path.getParentPath().getLastPathComponent().toString()
											.startsWith("Collection(") && (path.getParentPath()
									.toString()).indexOf("Outputs") > 0)
									|| (path.toString().indexOf("Collection(") < 0)) {
								final JPopupMenu menu = new JPopupMenu();
								JMenuItem item = new JMenuItem("Find Services that Consume "
										+ selectedObject + " - brief search");
								item
										.setIcon(getIcon("org/biomoby/client/taverna/plugin/Information24.gif"));
								final String potentialCollectionString = path.getParentPath()
										.getLastPathComponent().toString();
								final boolean isCollection = potentialCollectionString
										.indexOf("Collection('") >= 0;
								final Object selectedMobyObjectTreeNodeHolder = (DefaultMutableTreeNode) tree
										.getLastSelectedPathComponent();
								item.addActionListener(new ActionListener() {

									public void actionPerformed(ActionEvent ae) {
										// you would like to search for
										// selectedObject
										new Thread("Finding biomoby services") {
											public void run() {
												try {
													String name = selectedObject;
													if (name.indexOf("(") > 0)
														name = name.substring(0, name.indexOf("("));
													String articlename = "";
													if (isCollection) {
														articlename = potentialCollectionString
																.substring(
																		potentialCollectionString
																				.indexOf("('") + 2,
																		potentialCollectionString
																				.lastIndexOf("'"));
													} else {
														articlename = selectedObject.substring(
																selectedObject.indexOf("'") + 1,
																selectedObject.lastIndexOf("'"));
													}

													BiomobyObjectProcessor bop = new BiomobyObjectProcessor(
															scuflModel, scuflModel
																	.getValidProcessorName(name),
															new MobyDataType(name), endpoint);
													Port theServiceport = null;

													try {
														if (isCollection)
															theServiceport = theproc
																	.locatePort(
																			name
																					+ "(Collection - '"
																					+ (articlename
																							.equals("") ? "MobyCollection"
																							: articlename)
																					+ "' As Simples)", false);
														else
															theServiceport = theproc.locatePort(
																	name + "(" + articlename + ")",
																	false);
													} catch (Exception except) {
													}
													BiomobyObjectAction boa = null;

													if (theServiceport == null) {
														boa = new BiomobyObjectAction(false);
													} else {
														boa = new BiomobyObjectAction(
																theServiceport, false);
													}

													if (selectedMobyObjectTreeNodeHolder instanceof DefaultMutableTreeNode
															&& ((DefaultMutableTreeNode) selectedMobyObjectTreeNodeHolder)
																	.getUserObject() instanceof MobyObjectTreeNode) {
														DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectedMobyObjectTreeNodeHolder;
														MobyObjectTreeNode motn = (MobyObjectTreeNode) dmtn
																.getUserObject();
														boa.setNamespaces(motn.getNamespaces());
													}
													PopupThread popupthread = new PopupThread(bop,
															boa);
													progressBar.setStringPainted(true);
													progressBar.setVisible(true);
													popupthread.start();

													while (popupthread.isAlive()) {
														Thread.sleep(4000);
													}

													progressBar.setVisible(false);
													Component c = popupthread.getComponent();
													Dimension loc = BiomobyAction.this
															.getFrameLocation();
													Dimension size = BiomobyAction.this
															.getFrameSize();
													UIComponentSPI frame = new SimpleActionFrame(c, BiomobyAction.this, "Moby Object Details");
													UIUtils.createFrame((ScuflModel) null, frame,
															(int) loc.getWidth(), (int) loc
																	.getHeight(), (int) size
																	.getWidth(), (int) size
																	.getHeight());
													/*
													 * Component c =
													 * boa.getComponent(bop);
													 * Dimension loc =
													 * BiomobyAction.this.getFrameLocation();
													 * Dimension size =
													 * BiomobyAction.this.getFrameSize();
													 * ScuflUIComponent frame =
													 * new SimpleFrame(c, bop);
													 * UIUtils.createFrame((ScuflModel)
													 * null, frame, (int) loc
													 * .getWidth(), (int)
													 * loc.getHeight(), (int)
													 * size .getWidth(), (int)
													 * size.getHeight());
													 */
												} catch (Exception e) {
												}
											}
										}.start();

									}
								});

								JMenuItem item2 = new JMenuItem("Find Services that Consume "
										+ selectedObject + " - semantic search");
								item2
										.setIcon(getIcon("org/biomoby/client/taverna/plugin/Search24.gif"));
								item2.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ae) {
										// you would like to search for
										// selectedObject
										new Thread("Finding BioMoby services") {

											public void run() {
												try {
													String name = selectedObject;
													if (name.indexOf("(") > 0)
														name = name.substring(0, name.indexOf("("));
													String articlename = "";
													if (isCollection) {
														articlename = potentialCollectionString
																.substring(
																		potentialCollectionString
																				.indexOf("('") + 2,
																		potentialCollectionString
																				.lastIndexOf("'"));
													} else {
														articlename = selectedObject.substring(
																selectedObject.indexOf("'") + 1,
																selectedObject.lastIndexOf("'"));
													}
													BiomobyObjectProcessor bop = new BiomobyObjectProcessor(
															scuflModel, scuflModel
																	.getValidProcessorName(name),
															new MobyDataType(name), endpoint);
													Port theServiceport = null;
													try {
														if (isCollection)
															theServiceport = theproc
																	.locatePort(
																			name
																					+ "(Collection - '"
																					+ (articlename
																							.equals("") ? "MobyCollection"
																							: articlename)
																					+ "' As Simples)", false);
														else
															theServiceport = theproc.locatePort(
																	name + "(" + articlename + ")",
																	false);
													} catch (Exception except) {
													}
													BiomobyObjectAction boa = null;

													if (theServiceport == null)
														boa = new BiomobyObjectAction(true);
													else
														boa = new BiomobyObjectAction(
																theServiceport, true);
													if (selectedMobyObjectTreeNodeHolder instanceof DefaultMutableTreeNode
															&& ((DefaultMutableTreeNode) selectedMobyObjectTreeNodeHolder)
																	.getUserObject() instanceof MobyObjectTreeNode) {
														DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) selectedMobyObjectTreeNodeHolder;
														MobyObjectTreeNode motn = (MobyObjectTreeNode) dmtn
																.getUserObject();
														boa.setNamespaces(motn.getNamespaces());
													}
													PopupThread popupthread = new PopupThread(bop,
															boa);
													progressBar.setStringPainted(true);
													progressBar.setVisible(true);
													popupthread.start();

													while (popupthread.isAlive()) {
														Thread.sleep(4000);
													}

													progressBar.setVisible(false);
													Component c = popupthread.getComponent();
													Dimension loc = BiomobyAction.this
															.getFrameLocation();
													Dimension size = BiomobyAction.this
															.getFrameSize();
													UIComponentSPI frame = new SimpleActionFrame(c,BiomobyAction.this, "Moby Object Details");
													UIUtils.createFrame((ScuflModel) null, frame,
															(int) loc.getWidth(), (int) loc
																	.getHeight(), (int) size
																	.getWidth(), (int) size
																	.getHeight());
												} catch (Exception e) {
												}
											}
										}.start();
									}
								});

								// string may be needed to extract the
								// collection article name
								// final String potentialCollectionString =
								// path.getParentPath()
								// .getLastPathComponent().toString();
								// final boolean isCollection =
								// potentialCollectionString
								// .indexOf("Collection('") >= 0;

								JMenuItem item3 = new JMenuItem("Add parser for " + selectedObject
										+ " to the workflow");
								item3
										.setIcon(getIcon("org/biomoby/client/taverna/plugin/Cut24.gif"));
								item3.addActionListener(new ActionListener() {

									public void actionPerformed(ActionEvent ae) {
										// you would like to search for
										// selectedObject
										try {
											String name = selectedObject;
											if (name.indexOf("(") > 0)
												name = name.substring(0, name.indexOf("("));
											String workflowName = theproc.getModel()
													.getValidProcessorName(
															"Parse Moby Data(" + name + ")");
											String articlename = "";
											if (isCollection) {
												articlename = potentialCollectionString
														.substring(potentialCollectionString
																.indexOf("('") + 2,
																potentialCollectionString
																		.lastIndexOf("'"));
											} else {
												articlename = selectedObject.substring(
														selectedObject.indexOf("'") + 1,
														selectedObject.lastIndexOf("'"));
											}
											Processor parser;
											try {
												parser = new MobyParseDatatypeProcessor(scuflModel,
														workflowName, name, articlename,
														((BiomobyProcessor) theproc)
																.getMobyEndpoint());
												scuflModel.addProcessor(parser);
											} catch (ProcessorCreationException pce) {
												JOptionPane.showMessageDialog(null,
														"Processor creation exception : \n"
																+ pce.getMessage(), "Exception!",
														JOptionPane.ERROR_MESSAGE);
												return;
											} catch (DuplicateProcessorNameException dpne) {
												JOptionPane.showMessageDialog(null,
														"Duplicate name : \n" + dpne.getMessage(),
														"Exception!", JOptionPane.ERROR_MESSAGE);
												return;
											}

											try {
												if (scuflModel != null) {
													Port theServiceport = null;
													if (isCollection)
														theServiceport = theproc
																.locatePort(
																		name
																				+ "(Collection - '"
																				+ (articlename
																						.equals("") ? "MobyCollection"
																						: articlename)
																				+ "' As Simples)", false);
													else
														theServiceport = theproc.locatePort(name
																+ "(" + articlename + ")", false);
													if (theServiceport == null)
														return;
													scuflModel
															.addDataConstraint(new DataConstraint(
																	scuflModel, theServiceport,
																	parser.getInputPorts()[0]));
												} else {
													System.out.println("Null model");
												}
											} catch (DataConstraintCreationException dcce) {
												// dcce.printStackTrace();
												return;
											} catch (UnknownPortException e) {
												// e.printStackTrace();
												return;
											}

										} catch (Exception e) {
											e.printStackTrace();
										}

									}
								});

								menu.add(new JLabel("Moby Service Discovery ... ", JLabel.CENTER));
								menu.add(new JSeparator());
								menu.add(item);
								menu.add(new JSeparator());
								menu.add(item2);
								menu.add(new JLabel("Parse Moby Data ... ", JLabel.CENTER));
								menu.add(new JSeparator());
								menu.add(item3);

								menu.show(me.getComponent(), me.getX(), me.getY());
							}
						}
					}
				}
			}

			public void mouseEntered(MouseEvent me) {
			}

			public void mouseExited(MouseEvent me) {
			}
		});

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		JScrollPane jsp = new JScrollPane(tree);
		JPanel thePanel = new JPanel(new BorderLayout());
		thePanel.add(jsp, BorderLayout.CENTER);
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setString("Finding Services ... ");
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		thePanel.add(progressBar, BorderLayout.PAGE_END);
		return thePanel;
	}

/*	private class SimpleFrame extends JPanel implements UIComponentSPI {

		private static final long serialVersionUID = -6611234116434482238L;

		Processor processor = null;

		public SimpleFrame(Component c, Processor p) {
			super(new BorderLayout());
			add(c, BorderLayout.CENTER);
			this.processor = p;
		}

		public ImageIcon getIcon() {
			return BiomobyAction.this.getIcon();
		}

		public String getName() {
			return "Moby Object Details";
		}

		public void onDisplay() {
			
		}

		public void onDispose() {
			BiomobyAction.this.frameClosing();
		}
	}
*/
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#canHandle(org.embl.ebi.escience.scufl.Processor)
	 */
	public boolean canHandle(Processor processor) {
		return (processor instanceof BiomobyProcessor) && ((((BiomobyProcessor) processor)).getMobyService() != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getDescription()
	 */
	public String getDescription() {
		return "Moby Service Details";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getIcon()
	 */
	public ImageIcon getIcon() {
		Class<?> cls = this.getClass();
		URL url = cls.getClassLoader().getResource(
				"org/biomoby/client/taverna/plugin/moby_small.gif");
		return new ImageIcon(url);
	}

	/**
	 * returns the frame size as a dimension for the content pane housing this
	 * action
	 */
	public Dimension getFrameSize() {
		return new Dimension(450, 450);
	}

	/**
	 * Return an Icon to represent this action
	 * 
	 * @param loc
	 *            the location of the image to use as an icon
	 */
	public ImageIcon getIcon(String loc) {
		Class<?> cls = this.getClass();
		URL url = cls.getClassLoader().getResource(loc);
		return new ImageIcon(url);
	}
}