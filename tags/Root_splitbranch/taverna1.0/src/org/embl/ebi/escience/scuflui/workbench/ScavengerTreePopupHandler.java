/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessor;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessorFactory;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.DataThingConstructionPanel;
import org.embl.ebi.escience.scuflui.EnactorInvocation;
import org.embl.ebi.escience.scuflui.ShadedLabel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflui.WebScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.jdom.Document;
import org.jdom.Element;

/**
 * A class to handle popup menus on nodes on the ScavengerTree tree
 * 
 * @author Tom Oinn
 */
public class ScavengerTreePopupHandler extends MouseAdapter {
	private static Logger logger = Logger.getLogger(ProcessorFactory.class);

	private DefaultScavengerTree scavenger;

	public boolean isPopulating() {
		return scavenger.isPopulating();
	}

	public ScavengerTreePopupHandler(DefaultScavengerTree theTree) {
		this.scavenger = theTree;
	}

	/**
	 * Handle the mouse pressed event in case this is the platform specific
	 * trigger for a popup menu
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * If the popup was over a ProcessorFactory implementation then present the
	 * 'add' option to the user
	 */
	void doEvent(MouseEvent e) {
		TreePath path = scavenger.getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) (path.getLastPathComponent());
			final DefaultTreeModel tmodel = (DefaultTreeModel) scavenger.getModel();
			Object scuflObject = node.getUserObject();
			if (scuflObject != null) {
				boolean addDescribeOption = true;
				JPopupMenu menu = new JPopupMenu();
				// Create the menu item to fetch descriptions
				JMenuItem getDescriptions = new JMenuItem("Fetch descriptions", TavernaIcons.zoomIcon);
				getDescriptions.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						// Create a new thread to iterate over all child
						// items and fetch descriptions where possible
						new Thread() {
							public void run() {
								Enumeration en = node.depthFirstEnumeration();
								List pflist = new ArrayList();
								List nodelist = new ArrayList();
								while (en.hasMoreElements()) {
									DefaultMutableTreeNode tn = (DefaultMutableTreeNode) en.nextElement();
									if (tn.getUserObject() instanceof ProcessorFactory) {
										pflist.add(tn.getUserObject());
										nodelist.add(tn);
									}
								}
								Iterator j = nodelist.iterator();
								for (Iterator i = pflist.iterator(); i.hasNext();) {
									ProcessorFactory pf = (ProcessorFactory) i.next();
									TreeNode tnode = (TreeNode) j.next();
									if (pf.getDescription() == null) {
										// System.out.println(" fetching for
										// "+pf.toString());
										try {
											String description = pf.createProcessor("foo", null).getDescription();
											// System.out.println(description);
											pf.setDescription(description);
											// System.out.println(pf.getDescription());
										} catch (Exception ex) {
											pf.setDescription("<font color=\"red\">Cannot fetch description!</font>");
											ex.printStackTrace();
										} finally {
											tmodel.nodeChanged(tnode);
										}
									}
								}
							}
						}.start();
					}
				});

				// Add a magic rune to create a trivial workflow with only the
				// select processor and enact it
				if (scuflObject instanceof ProcessorFactory) {
					final ProcessorFactory pf = (ProcessorFactory) scuflObject;
					JMenuItem test = new JMenuItem("Invoke", TavernaIcons.windowRun);
					test.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							try {
								final ScuflModel m;
								if (pf instanceof ScuflWorkflowProcessorFactory) {
									ScuflWorkflowProcessor wp = (ScuflWorkflowProcessor) pf.createProcessor("workflow",
											new ScuflModel());
									m = wp.getInternalModel();
								} else {
									m = new ScuflModel();
									Processor p = pf.createProcessor("processor", m);
									// m.addProcessor(p);
									// Iterate over all inputs and create
									// workflow inputs, similarly for all
									// outputs
									InputPort[] ip = p.getInputPorts();
									for (int i = 0; i < ip.length; i++) {
										String portName = ip[i].getName();
										OutputPort port = new OutputPort(m.getWorkflowSourceProcessor(), portName);
										m.getWorkflowSourceProcessor().addPort(port);
										m.addDataConstraint(new DataConstraint(m, port, ip[i]));
									}
									OutputPort[] op = p.getOutputPorts();
									for (int i = 0; i < op.length; i++) {
										String portName = op[i].getName();
										InputPort port = new InputPort(m.getWorkflowSinkProcessor(), portName);
										m.getWorkflowSinkProcessor().addPort(port);
										m.addDataConstraint(new DataConstraint(m, op[i], port));
									}
									// Should have now created a trivial single
									// processor workflow or the directly loaded
									// more complex one.
								}
								if (m.getWorkflowSourcePorts().length != 0) {
									DataThingConstructionPanel thing = new DataThingConstructionPanel() {
										public void launchEnactorDisplay(Map inputObject) {
											try {
												UIUtils.createFrame(m, new EnactorInvocation(FreefluoEnactorProxy
														.getInstance(), m, inputObject), 100, 100, 600, 400);
											} catch (WorkflowSubmissionException wse) {
												JOptionPane.showMessageDialog(null,
														"Problem invoking workflow engine : \n" + wse.getMessage(),
														"Exception!", JOptionPane.ERROR_MESSAGE);
											}
										}
									};
									UIUtils.createFrame(m, thing, 100, 100, 600, 400);
								} else {
									try {
										// No inputs so launch the enactor
										// directly
										UIUtils.createFrame(m, new EnactorInvocation(
												FreefluoEnactorProxy.getInstance(), m, new HashMap()), 100, 100, 600,
												400);
									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, "Unable to run operation : \n" + ex.getMessage(),
										"Exception!", JOptionPane.ERROR_MESSAGE);
								ex.printStackTrace();
							}
						}

					});
					menu.add(new ShadedLabel(pf.getName(), ShadedLabel.TAVERNA_GREEN));
					menu.addSeparator();
					menu.add(test);
				} else {
					menu.add(new ShadedLabel(scuflObject.toString(), ShadedLabel.TAVERNA_BLUE));
					menu.addSeparator();
				}
				if (scuflObject instanceof ProcessorFactory && scavenger.model != null) {
					final ProcessorFactory pf = (ProcessorFactory) scuflObject;
					// Show the popup for adding new processors to the model
					JMenuItem add = new JMenuItem("Add to model", TavernaIcons.importIcon);
					menu.addSeparator();
					menu.add(add);
					JMenuItem addWithName = new JMenuItem("Add to model with name...", TavernaIcons.importIcon);
					menu.add(addWithName);
					// Prepare the 'add as alternate menu'
					Processor[] processors = scavenger.model.getProcessors();
					if (processors.length > 0) {
						JMenu processorList = new JMenu("Add as alternate to...");
						for (int i = 0; i < processors.length; i++) {
							JMenuItem processorItem = new JMenuItem(processors[i].getName(), ProcessorHelper
									.getPreferredIcon(processors[i]));
							processorList.add(processorItem);
							final Processor theProcessor = processors[i];
							processorItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									try {
										int numberOfAlternates = theProcessor.getAlternatesArray().length;
										Processor alternateProcessor = pf.createProcessor("alternate"
												+ (numberOfAlternates + 1), null);
										AlternateProcessor alternate = new AlternateProcessor(alternateProcessor);
										theProcessor.addAlternate(alternate);
										if (theProcessor.getModel() != null) {
											boolean isOffline = theProcessor.getModel().isOffline();
											if (isOffline) {
												alternateProcessor.setOffline();
											} else {
												alternateProcessor.setOnline();
											}
										}
										// Set the appropriate offline /
										// online status

									} catch (Exception ex) {
										ex.printStackTrace();
										JOptionPane.showMessageDialog(null, "Problem creating alternate : \n"
												+ ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
									}
								}

							});
						}
						menu.add(processorList);
					}

					// If this is a workflow factory then we might as well give
					// the user the option to import the complete workflow as
					// well as to wrap it in a processor
					if (scuflObject instanceof ScuflWorkflowProcessorFactory) {
						JMenuItem imp = new JMenuItem("Import workflow...", TavernaIcons.webIcon);
						final String definitionURL = ((ScuflWorkflowProcessorFactory) scuflObject).getDefinitionURL();
						final Element definitionElement = (Element) ((ScuflWorkflowProcessorFactory) scuflObject)
								.getDefinition();
						imp.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent ae) {
								try {
									String prefix = (String) JOptionPane.showInputDialog(null, "Optional name prefix?",
											"Prefix", JOptionPane.QUESTION_MESSAGE, null, null, "");
									if (prefix != null) {
										if (prefix.equals("")) {
											prefix = null;
										}
										if (definitionURL != null) {
											XScuflParser.populate((new URL(definitionURL)).openStream(),
													ScavengerTreePopupHandler.this.scavenger.model, prefix);
										} else {
											// Is a literal definition
											XScuflParser.populate(new Document((Element) definitionElement.clone()),
													ScavengerTreePopupHandler.this.scavenger.model, prefix);
										}
									}
								} catch (Exception ex) {
									JOptionPane.showMessageDialog(null, "Problem opening XScufl from web : \n"
											+ ex.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
								}
							}
						});
						menu.add(imp);
					}
					addWithName.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							String name = (String) JOptionPane.showInputDialog(null, "Name for the new processor?",
									"Name required", JOptionPane.QUESTION_MESSAGE, null, null, "");
							if (name != null) {
								try {
									pf.createProcessor(name, ScavengerTreePopupHandler.this.scavenger.model);
								} catch (ProcessorCreationException pce) {
									JOptionPane.showMessageDialog(null, "Processor creation exception : \n"
											+ pce.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
								} catch (DuplicateProcessorNameException dpne) {
									JOptionPane.showMessageDialog(null, "Duplicate name : \n" + dpne.getMessage(),
											"Exception!", JOptionPane.ERROR_MESSAGE);
								}
							}
						}
					});
					add.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							String defaultName = pf.getName();
							String validName = ScavengerTreePopupHandler.this.scavenger.model
									.getValidProcessorName(defaultName);
							try {
								pf.createProcessor(validName, ScavengerTreePopupHandler.this.scavenger.model);
							} catch (ProcessorCreationException pce) {
								logger.error("Problen creating processor", pce);
								JOptionPane.showMessageDialog(null, "Processor creation exception : \n"
										+ pce.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
							} catch (DuplicateProcessorNameException dpne) {
								logger.error("Problen creating processor", dpne);
								JOptionPane.showMessageDialog(null, "Duplicate name : \n" + dpne.getMessage(),
										"Exception!", JOptionPane.ERROR_MESSAGE);
							}
						}
					});
				} else if (scuflObject instanceof String) {
					// Catch the click on the 'Available Processors' text to add
					// a new scavenger
					String choice = (String) scuflObject;
					if (choice.equals("Available Processors")) {
						ScavengerHelper webScavengerHelper = null;
						addDescribeOption = false;
						menu.setLabel("Create new scavenger");
						// Iterate over the scavenger creator list from the
						// ProcessorHelper class
						for (ScavengerHelper scavengerHelper : ScavengerHelperRegistry.instance().getScavengerHelpers()) {
							// Instantiate a ScavengerHelper...
							try {
								// webscavenger helper is added after the
								// seperator
								if (scavengerHelper instanceof WebScavengerHelper) {
									webScavengerHelper = scavengerHelper;
								} else {
									addScavengerHelperToMenu(menu, scavengerHelper);
								}
							} catch (Exception ex) {
								logger.error("Exception adding scavenger helper to scavenger tree");
							}
						}
						if (!ScavengerTreePopupHandler.this.scavenger.isPopulating()) {

							menu.addSeparator();

							if (webScavengerHelper != null) {
								addScavengerHelperToMenu(menu, webScavengerHelper);
							}

							JMenuItem collect = new JMenuItem("Collect scavengers from model", TavernaIcons.importIcon);
							menu.add(collect);
							collect.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									try {
										ScavengerTreePopupHandler.this.scavenger.addScavengersFromModel();
									} catch (ScavengerCreationException sce) {
										JOptionPane.showMessageDialog(null, "Unable to import scavengers!\n"
												+ sce.getMessage(), "Exception!", JOptionPane.ERROR_MESSAGE);
									}
								}
							});

							menu.addSeparator();
							JMenuItem showAllNodes = new JMenuItem("Expand all");
							menu.add(showAllNodes);
							showAllNodes.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									ScavengerTreePopupHandler.this.scavenger.setExpansion(true);
								}
							});

							JMenuItem hideAllNodes = new JMenuItem("Collapse all");
							menu.add(hideAllNodes);
							hideAllNodes.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									scavenger.setExpansion(false);
									scavenger.expandPath(new TreePath(scavenger.treeModel.getRoot()));
								}
							});
						}
					} else {
						// Wasn't the 'available processors' link, so give the
						// option to remove it
						if (choice.equals("Internal Services") == false && choice.equals("Local Java widgets") == false) {
							// JPopupMenu menu = new JPopupMenu();
							JMenuItem remove = new JMenuItem("Remove from tree", TavernaIcons.deleteIcon);
							final String scavengerName = choice;
							remove.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									DefaultTreeModel treeModel = (DefaultTreeModel) ScavengerTreePopupHandler.this.scavenger.treeModel;
									MutableTreeNode treeNode = node;
									treeModel.removeNodeFromParent(treeNode);
									// Also remove the name from the scavenger
									// list
									ScavengerTreePopupHandler.this.scavenger.scavengerList.remove(scavengerName);
								}
							});
							menu.add(remove);
							// menu.show(scavenger, e.getX(), e.getY());
						}
					}
				}
				if (addDescribeOption == true) {
					menu.addSeparator();
					menu.add(getDescriptions);
				}
				menu.show(scavenger, e.getX(), e.getY());
			}
		}
	}

	private void addScavengerHelperToMenu(JPopupMenu menu, ScavengerHelper scavengerHelper) {
		String scavengerDescription = scavengerHelper.getScavengerDescription();
		if (scavengerDescription != null) {
			JMenuItem scavengerMenuItem = new JMenuItem(scavengerDescription, scavengerHelper.getIcon());
			scavengerMenuItem.addActionListener(scavengerHelper.getListener(ScavengerTreePopupHandler.this.scavenger));
			menu.add(scavengerMenuItem);
		}
	}

}
