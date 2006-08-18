/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

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
import javax.swing.tree.MutableTreeNode;
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
import org.biomoby.shared.MobyService;
import org.biomoby.shared.data.MobyDataInstance;
import org.biomoby.shared.data.MobyDataObject;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction;

public class BiomobyObjectAction extends AbstractProcessorAction {

	private boolean searchParentTypes = false;

	private Port outputPort = null;

	private MobyNamespace[] namespaces = null;

	public BiomobyObjectAction(boolean searchParentTypes) {
		super();
		this.searchParentTypes = searchParentTypes;
	}

	public BiomobyObjectAction(Port outputPort, boolean searchParentTypes) {
		super();
		this.searchParentTypes = searchParentTypes;
		this.outputPort = outputPort;
	}

	public BiomobyObjectAction() {
		super();
	}

	private class Worker extends Thread {
		private BiomobyObjectProcessor theProcessor;

		private BiomobyObjectAction action;

		private JPanel panel = new JPanel(new BorderLayout());

		private JProgressBar bar = new JProgressBar();

		public Worker(BiomobyObjectProcessor bop, BiomobyObjectAction object) {
			this.theProcessor = bop;
			this.action = object;
			bar.setIndeterminate(true);
			bar.setValue(0);
			bar.setStringPainted(true);
			bar.setVisible(true);
			bar.setString("Getting BioMOBY details for " + theProcessor.getName() + " ...");
			panel.add(bar, BorderLayout.PAGE_END);
		}

		public JPanel getPanel() {
			return this.panel;
		}

		public void run() {
			Central central = theProcessor.getCentral();
			MobyDataType object = theProcessor.getMobyObject();
			MobyService template = new MobyService("dummy");

			// strip the lsid portion of the name
			String name = object.getName();
			if (name.indexOf(":") > 0) {
				name = name.substring(name.lastIndexOf(":") + 1);
			}
			// initialize a data object to pass into the service template
			MobyDataObject data = new MobyDataObject("");
			data.setDataType(new MobyDataType(name));
			data.setXmlMode(MobyDataInstance.CENTRAL_XML_MODE);
			if (action.namespaces != null)
				data.setNamespaces(action.namespaces);
			// create the nodes for the tree
			MutableTreeNode parent = new DefaultMutableTreeNode(name);
			MutableTreeNode inputNode = new DefaultMutableTreeNode("Feeds into");
			MutableTreeNode outputNode = new DefaultMutableTreeNode("Produced by");

			// what services does this object feed into?
			template.setInputs(new MobyData[] { data });
			MobyService[] services = null;
			try {
				services = central.findService(template, null, true, action.searchParentTypes);
			} catch (MobyException e) {
				panel.add(new JTree(new String[] { "Error finding services",
						"TODO: create a better Error" }), BorderLayout.CENTER);
				panel.updateUI();
				return;
			}
			createTreeNodes(inputNode, services);
			if (inputNode.getChildCount() == 0)
				inputNode.insert(new DefaultMutableTreeNode(
						"Object Doesn't Currently Feed Into Any Services"), 0);

			// what services return this object?
			template = null;
			template = new MobyService("dummy");

			template.setOutputs(new MobyData[] { data });
			services = null;
			try {
				services = central.findService(template, null, true, action.searchParentTypes);
			} catch (MobyException e) {
				panel.add(new JTree(new String[] { "Error finding services",
						"TODO: create a better Error" }), BorderLayout.CENTER);
				panel.updateUI();
				return;
			}
			createTreeNodes(outputNode, services);
			if (outputNode.getChildCount() == 0)
				outputNode.insert(new DefaultMutableTreeNode(
						"Object Isn't Produced By Any Services"), 0);
			// what kind of object is this?

			// set up the nodes
			parent.insert(inputNode, 0);
			parent.insert(outputNode, 1);

			// finally return a tree describing the object
			final JTree tree = new JTree(parent);
			tree.setCellRenderer(new BioMobyObjectTreeCustomRenderer());
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
						if (path.getPathCount() == 4) {
							if (path.getParentPath().getParentPath().getLastPathComponent()
									.toString().equals("Feeds into")) {

								DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
										.getLastSelectedPathComponent();
								if (node == null)
									return;
								final String selectedService = node.toString();
								// ensure that the last selected item is a
								// service!
								if (!selectedService.equals(path.getLastPathComponent().toString()))
									return;
								final String selectedAuthority = path.getParentPath()
										.getLastPathComponent().toString();
								final JPopupMenu menu = new JPopupMenu();
								// Create and add a menu item for adding to the
								// item
								// to the workflow
								JMenuItem item = new JMenuItem("Add service - " + selectedService
										+ " to the workflow?");
								item
										.setIcon(getIcon("org/biomoby/client/ui/graphical/applets/img/toolbarButtonGraphics/general/Add24.gif"));
								item.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ae) {
										String defaultName = selectedService;
										String validName = theProcessor.getModel()
												.getValidProcessorName(defaultName);
										Processor bpf = null;
										try {
											bpf = new BiomobyProcessorFactory(theProcessor
													.getMobyEndpoint(), selectedAuthority,
													selectedService).createProcessor(validName,
													theProcessor.getModel());
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
										// bpf is not null
										if (outputPort != null) {
											InputPort inputPorts[] = bpf.getInputPorts();
											int index = 0;
											if (inputPorts.length == 2) {
												if (!inputPorts[1].getName().equals("input"))
													index = 1;
											} else
												return;
											try {
												theProcessor.getModel().addDataConstraint(
														new DataConstraint(theProcessor.getModel(),
																outputPort, inputPorts[index]));
											} catch (DataConstraintCreationException dcee) {
											}
										} else {
											InputPort inputPorts[] = bpf.getInputPorts();
											int index = 0;
											if (inputPorts.length == 2) {
												if (!inputPorts[1].getName().equals("input"))
													index = 1;
											} else
												return;
											try {
												theProcessor.getModel().addDataConstraint(
														new DataConstraint(theProcessor.getModel(),
																theProcessor.getOutputPorts()[0],
																inputPorts[index]));
											} catch (DataConstraintCreationException dcee) {
											}
										}
									}
								});
								// Create and add a menu item for service
								// details
								JMenuItem details = new JMenuItem("Find out about "
										+ selectedService);
								details
										.setIcon(getIcon("org/biomoby/client/ui/graphical/applets/img/toolbarButtonGraphics/general/Information24.gif"));
								details.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent ae) {
										Dimension loc = new Dimension(100, 100);
										Dimension size = new Dimension(450, 450);
										ScuflUIComponent c = new MobyPanel(selectedService,
												"A BioMoby Service Description",
												createServiceDescription(selectedService,
														selectedAuthority, theProcessor
																.getMobyEndpoint()));
										UIUtils.createFrame((ScuflModel) null, c, (int) loc
												.getWidth(), (int) loc.getHeight(), (int) size
												.getWidth(), (int) size.getHeight());
									}

									@SuppressWarnings("unchecked")
									private String createServiceDescription(String selectedService,
											String selectedAuthority, String endpoint) {
										StringBuffer sb = new StringBuffer();
										String newline = System.getProperty("line.separator");
										MobyService service = new MobyService(selectedService);
										try {
											Central central = new CentralImpl(endpoint);
											service.setAuthority(selectedAuthority);
											MobyService[] services = central.findService(service);
											if (services == null || services.length != 1) {
												return "Couldn't retrieve a description on the BioMoby service '"
														+ selectedService + "'";
											}
											service = services[0];

										} catch (MobyException e) {
											e.printStackTrace();
											return "Couldn't retrieve a description on the BioMoby service '"
													+ selectedService + "'";
										}
										sb.append("Service Contact: " + newline + "\t"
												+ service.getEmailContact() + newline);
										sb.append("Service Category: " + newline + "\t"
												+ service.getCategory() + newline);
										sb.append("Service Authority: " + newline + "\t"
												+ service.getAuthority() + newline);
										sb.append("Service Type: " + newline + "\t"
												+ service.getType() + newline);
										sb.append("Service Description: " + newline + "\t"
												+ service.getDescription() + newline);
										sb.append("Location of Service: " + newline + "\t"
												+ service.getURL() + newline);
										sb.append("Service Signature RDF Document is located at: "
												+ newline + "\t" + service.getSignatureURL()
												+ newline);
										MobyData[] data = service.getPrimaryInputs();
										Vector primaryDataSimples = new Vector();
										Vector primaryDataSets = new Vector();
										for (int x = 0; x < data.length; x++) {
											if (data[x] instanceof MobyPrimaryDataSimple)
												primaryDataSimples.add(data[x]);
											else
												primaryDataSets.add(data[x]);
										}
										// describe inputs simple then
										// collections
										sb.append("Inputs:" + newline);
										if (primaryDataSimples.size() == 0) {
											sb.append("\t\tNo Simple input datatypes consumed."
													+ newline);
										} else {
											Iterator it = primaryDataSimples.iterator();
											sb
													.append("\t\tService consumes the following Simple(s):"
															+ newline);
											while (it.hasNext()) {
												MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) it
														.next();
												MobyNamespace[] namespaces = simple.getNamespaces();
												sb.append("\t\tData type: "
														+ simple.getDataType().getName() + newline);
												sb.append("\t\t\tArticle name: " + simple.getName()
														+ newline);
												if (namespaces.length == 0) {
													sb.append("\t\t\tValid Namespaces: ANY"
															+ newline);
												} else {
													sb.append("\t\t\tValid Namespaces: ");
													for (int x = 0; x < namespaces.length; x++)
														sb.append(namespaces[x].getName() + " ");
													sb.append(newline);
												}
											}
										}
										if (primaryDataSets.size() == 0) {
											sb.append(newline
													+ "\t\tNo Collection input datatypes consumed."
													+ newline);
										} else {
											Iterator it = primaryDataSets.iterator();
											sb
													.append(newline
															+ "\t\tService consumes the following collection(s) of datatypes:"
															+ newline);
											while (it.hasNext()) {
												MobyPrimaryDataSet set = (MobyPrimaryDataSet) it
														.next();
												MobyPrimaryDataSimple simple = null;
												sb.append("\t\tCollection Name:" + set.getName()
														+ newline);
												MobyPrimaryDataSimple[] simples = set.getElements();
												for (int i = 0; i < simples.length; i++) {
													simple = simples[i];
													MobyNamespace[] namespaces = simple
															.getNamespaces();
													// iterate through set and
													// do
													// the following
													sb.append("\t\tData type: "
															+ simple.getDataType().getName()
															+ newline);
													sb.append("\t\t\tArticle name: "
															+ simple.getName() + newline);
													if (namespaces.length == 0) {
														sb.append("\t\t\tValid Namespaces: ANY"
																+ newline);
													} else {
														sb.append("\t\t\tValid Namespaces: ");
														for (int x = 0; x < namespaces.length; x++)
															sb
																	.append(namespaces[x].getName()
																			+ " ");
														sb.append(newline);
													}
												}
											}
										}
										// describe secondary inputs
										// describe outputs simple then
										// collections
										data = service.getPrimaryOutputs();
										primaryDataSimples = new Vector();
										primaryDataSets = new Vector();
										for (int x = 0; x < data.length; x++) {
											if (data[x] instanceof MobyPrimaryDataSimple)
												primaryDataSimples.add(data[x]);
											else
												primaryDataSets.add(data[x]);
										}
										sb.append("Outputs:" + newline);
										if (primaryDataSimples.size() == 0) {
											sb.append("\t\tNo Simple output datatypes produced."
													+ newline);
										} else {
											Iterator it = primaryDataSimples.iterator();
											sb
													.append("\t\tService produces the following Simple(s):"
															+ newline);
											while (it.hasNext()) {
												MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) it
														.next();
												MobyNamespace[] namespaces = simple.getNamespaces();
												sb.append("\t\tData type: "
														+ simple.getDataType().getName() + newline);
												sb.append("\t\t\tArticle name: " + simple.getName()
														+ newline);
												if (namespaces.length == 0) {
													sb.append("\t\t\tValid Namespaces: ANY"
															+ newline);
												} else {
													sb.append("\t\t\tValid Namespaces: ");
													for (int x = 0; x < namespaces.length; x++)
														sb.append(namespaces[x].getName() + " ");
													sb.append(newline);
												}
											}
										}
										if (primaryDataSets.size() == 0) {
											sb
													.append(newline
															+ "\t\tNo Collection output datatypes produced."
															+ newline);
										} else {
											Iterator it = primaryDataSets.iterator();
											sb
													.append(newline
															+ "\t\tService produces the following collection(s) of datatypes:"
															+ newline);
											while (it.hasNext()) {
												MobyPrimaryDataSet set = (MobyPrimaryDataSet) it
														.next();
												MobyPrimaryDataSimple simple = null;
												sb.append("\t\tCollection Name:" + set.getName()
														+ newline);
												MobyPrimaryDataSimple[] simples = set.getElements();
												for (int i = 0; i < simples.length; i++) {
													simple = simples[i];
													MobyNamespace[] namespaces = simple
															.getNamespaces();
													// iterate through set and
													// do
													// the following
													sb.append("\t\tData type: "
															+ simple.getDataType().getName()
															+ newline);
													sb.append("\t\t\tArticle name: "
															+ simple.getName() + newline);
													if (namespaces.length == 0) {
														sb.append("\t\t\tValid Namespaces: ANY"
																+ newline);
													} else {
														sb.append("\t\t\tValid Namespaces: ");
														for (int x = 0; x < namespaces.length; x++)
															sb
																	.append(namespaces[x].getName()
																			+ " ");
														sb.append(newline);
													}
												}
											}
										}
										sb.append((service.isAuthoritative()) ? newline
												+ "The service belongs to this author." + newline
												: newline
														+ "The service was wrapped by it's author."
														+ newline);
										return sb.toString();
									}
								});
								// add the components to the menus
								menu.add(new JLabel("Add to workflow ... ", JLabel.CENTER));
								menu.add(new JSeparator());
								menu.add(item);
								menu.add(new JSeparator());
								menu.add(new JLabel("Service Details ... ", JLabel.CENTER));
								menu.add(new JSeparator());
								menu.add(details);
								// show the window
								menu.show(me.getComponent(), me.getX(), me.getY());
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
			panel.add(new JScrollPane(tree), BorderLayout.CENTER);
			panel.remove(bar);
			panel.updateUI();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction#getComponent(org.embl.ebi.escience.scufl.Processor)
	 */
	public JComponent getComponent(Processor processor) {
		// variables i need

		final BiomobyObjectProcessor theProcessor = (BiomobyObjectProcessor) processor;
		// this was done so that for longer requests, something is shown visually and the user then wont think that nothing happened.
		Worker worker = new Worker(theProcessor, this);
		worker.start();
		return worker.getPanel();
	}

	/*
	 * method that processes the services returned by findService and adds them
	 * to the TreeNode parentNode, sorted by authority
	 */
	@SuppressWarnings("unchecked")
	private void createTreeNodes(MutableTreeNode parentNode, MobyService[] services) {
		HashMap inputHash;
		inputHash = new HashMap();
		for (int x = 0; x < services.length; x++) {
			DefaultMutableTreeNode authorityNode = null;
			if (!inputHash.containsKey(services[x].getAuthority())) {
				authorityNode = new DefaultMutableTreeNode(services[x].getAuthority());
			} else {
				authorityNode = (DefaultMutableTreeNode) inputHash.get(services[x].getAuthority());
			}
			MobyServiceTreeNode serv = new MobyServiceTreeNode(services[x].getName(), services[x]
					.getDescription());
			MutableTreeNode temp = new DefaultMutableTreeNode(serv);
			DefaultMutableTreeNode objects = new DefaultMutableTreeNode("Produces");
			// add to this node the MobyObjectTreeNodes that it produces!
			MobyData[] outputs = services[x].getPrimaryOutputs();
			for (int i = 0; i < outputs.length; i++) {
				if (outputs[i] instanceof MobyPrimaryDataSimple) {
					MobyPrimaryDataSimple simple = (MobyPrimaryDataSimple) outputs[i];
					StringBuffer sb = new StringBuffer("Namespaces used by this object: ");
					MobyNamespace[] namespaces = simple.getNamespaces();
					for (int j = 0; j < namespaces.length; j++) {
						sb.append(namespaces[j].getName() + " ");
					}
					if (namespaces.length == 0)
						sb.append("ANY ");
					MobyObjectTreeNode mobyObjectTreeNode = new MobyObjectTreeNode(simple
							.getDataType().getName()
							+ "('" + simple.getName() + "')", sb.toString());
					objects.insert(new DefaultMutableTreeNode(mobyObjectTreeNode), objects
							.getChildCount());
				} else {
					// we have a collection
					MobyPrimaryDataSet collection = (MobyPrimaryDataSet) outputs[i];
					DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode(
							"Collection('" + collection.getName() + "')");
					objects.insert(collectionNode, objects.getChildCount());
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
						collectionNode.insert(new DefaultMutableTreeNode(mobyObjectTreeNode),
								collectionNode.getChildCount());
					}

				}
			}

			temp.insert(objects, temp.getChildCount());

			authorityNode.insert(temp, authorityNode.getChildCount());
			inputHash.put(services[x].getAuthority(), authorityNode);

		}
		Set set = inputHash.keySet();
		SortedSet sortedset = new TreeSet(set);
		for (Iterator it = sortedset.iterator(); it.hasNext();) {
			parentNode.insert((DefaultMutableTreeNode) inputHash.get((String) it.next()),
					parentNode.getChildCount());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#canHandle(org.embl.ebi.escience.scufl.Processor)
	 */
	public boolean canHandle(Processor processor) {
		return (processor instanceof BiomobyObjectProcessor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getDescription()
	 */
	public String getDescription() {
		return "Moby Object Details";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getIcon()
	 */
	public ImageIcon getIcon() {
		Class cls = this.getClass();
		URL url = cls.getClassLoader().getResource(
				"org/biomoby/client/taverna/plugin/moby_small.gif");
		return new ImageIcon(url);
	}

	/**
	 * Return an Icon to represent this action
	 * 
	 * @param loc
	 *            the location of the image to use as an icon
	 */
	public ImageIcon getIcon(String loc) {
		Class cls = this.getClass();
		URL url = cls.getClassLoader().getResource(loc);
		return new ImageIcon(url);
	}

	/**
	 * returns the frame size as a dimension for the content pane housing this
	 * action
	 */
	public Dimension getFrameSize() {
		return new Dimension(450, 450);
	}

	public void setNamespaces(MobyNamespace[] namespaces) {
		if (namespaces != null && namespaces.length == 0)
			this.namespaces = null;
		else
			this.namespaces = namespaces;
	}
}