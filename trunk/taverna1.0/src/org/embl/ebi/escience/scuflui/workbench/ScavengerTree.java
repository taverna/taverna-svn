/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;
import org.biomoby.client.taverna.plugin.BiomobyProcessor;
import org.biomoby.client.taverna.plugin.BiomobyScavenger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ExtendedJTree;
import org.embl.ebi.escience.scuflui.ScavengerTreePanel;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.ProcessorSpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerScavenger;
import org.embl.ebi.escience.scuflworkers.biomart.BiomartScavenger;
import org.embl.ebi.escience.scuflworkers.seqhound.SeqhoundScavenger;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabScavenger;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessor;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanScavenger;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedScavenger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * A JTree subclass showing available processors from some set of external
 * libraries or searches. Nodes corresponding to a single potential processor
 * instance should contain a user object implementing ProcessorFactory.
 * 
 * @author Tom Oinn
 */
public class ScavengerTree extends ExtendedJTree implements ScuflUIComponent, DragSourceListener, DragGestureListener,
		DropTargetListener {

	private static final long serialVersionUID = -5395001232451125620L;

	private static Logger logger = Logger.getLogger(ScavengerTree.class);

	private ScavengerTreePanel parentPanel = null;

	public ScavengerTreePanel getParentPanel() {
		return parentPanel;
	}

	/**
	 * The model that this scavenger will create processor for
	 */
	ScuflModel model = null;

	/**
	 * The root node
	 */
	DefaultMutableTreeNode root = null;

	/**
	 * The tree model
	 */
	DefaultTreeModel treeModel = null;

	/**
	 * A list of the names of all the scavengers contained within this tree
	 */
	ArrayList scavengerList = null;

	/**
	 * A private count to avoid name duplication on created nodes
	 */
	private int count = 0;

	/**
	 * Get the next available count and increment the counter
	 */
	public int getNextCount() {
		return count++;
	}

	/**
	 * Recognize the drag gesture, only allow if there's a processor factory
	 * node here, and export the XML fragment from the node as the transferable
	 */
	public void dragGestureRecognized(DragGestureEvent e) {
		// Get the node that was dragged
		Point l = e.getDragOrigin();
		TreePath dragSourcePath = getPathForLocation((int) l.getX(), (int) l.getY());
		if (dragSourcePath != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) dragSourcePath.getLastPathComponent();
			Object userObject = node.getUserObject();
			if (userObject instanceof ProcessorFactory) {
				Element el = ((ProcessorFactory) userObject).getXMLFragment();
				String name = ((ProcessorFactory) userObject).getName();
				FactorySpecFragment fsf = new FactorySpecFragment(el, name);
				Transferable t = new SpecFragmentTransferable(fsf);
				e.startDrag(DragSource.DefaultCopyDrop, t, this);
			}
		}
	}

	public void dragDropEnd(DragSourceDropEvent e) {
		//
	}

	public void dragEnter(DragSourceDragEvent e) {
		//
	}

	public void dragExit(DragSourceEvent e) {
		//
	}

	public void dragOver(DragSourceDragEvent e) {
		//
	}

	public void dropActionChanged(DragSourceDragEvent e) {
		//
	}

	public void dragEnter(DropTargetDragEvent e) {
		//
	}

	public void dragExit(DropTargetEvent e) {
		//
	}

	public void dragOver(DropTargetDragEvent e) {
		//
	}

	public void dropActionChanged(DropTargetDragEvent e) {
		//
	}

	public void drop(DropTargetDropEvent e) {
		try {
			DataFlavor f = SpecFragmentTransferable.processorSpecFragmentFlavor;
			Transferable t = e.getTransferable();
			if (e.isDataFlavorSupported(f)) {
				ProcessorSpecFragment psf = (ProcessorSpecFragment) t.getTransferData(f);
				XMLOutputter xo = new XMLOutputter();
				// Remove the various fault tolerance etc attributes
				Element processorElement = psf.getElement();
				List attributes = processorElement.getAttributes();
				for (Iterator i = attributes.iterator(); i.hasNext();) {
					Attribute att = (Attribute) i.next();
					processorElement.removeAttribute(att);
				}
				String searchString = xo.outputString(psf.getElement());
				DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
				Enumeration en = rootNode.depthFirstEnumeration();
				while (en.hasMoreElements()) {
					DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) en.nextElement();
					Object o = theNode.getUserObject();
					if (o instanceof ProcessorFactory) {
						String compare = xo.outputString(((ProcessorFactory) o).getXMLFragment());
						if (searchString.equals(compare)) {
							String selectedProcessorString = theNode.getUserObject().toString().toLowerCase();
							TreePath path = new TreePath(treeModel.getPathToRoot(theNode));
							setExpansion(false);
							setPattern(selectedProcessorString);
							makeVisible(path);
						}
					}
				}

			}
		} catch (Exception ex) {
			e.rejectDrop();
		}
	}

	/**
	 * Create a new scavenger tree, if the boolean 'populate' flag is true then
	 * load the default service set from the system properties, otherwise start
	 * with a completely blank panel.
	 */
	public ScavengerTree(boolean populate, ScavengerTreePanel parentPanel) {
		super();
		synchronized (this.getModel()) {
			this.parentPanel = parentPanel;
			setRowHeight(18);
			setLargeModel(true);
			wsdlURLList = System.getProperty("taverna.defaultwsdl");
			soaplabDefaultURLList = System.getProperty("taverna.defaultsoaplab");
			biomobyDefaultURLList = System.getProperty("taverna.defaultbiomoby");
			webURLList = System.getProperty("taverna.defaultweb");
			martRegistryList = System.getProperty("taverna.defaultmartregistry");
			DragSource dragSource = DragSource.getDefaultDragSource();
			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
			new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
			scavengerList = new ArrayList();
			root = new DefaultMutableTreeNode("Available Processors");
			treeModel = (DefaultTreeModel) this.getModel();
			treeModel.setRoot(this.root);
			putClientProperty("JTree.lineStyle", "Angled");
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			ScavengerTreeRenderer renderer = new ScavengerTreeRenderer();
			this.setCellRenderer(renderer);
			this.addMouseListener(new ScavengerTreePopupHandler(this));
			// Add the simple scavengers, should such exist, but only
			// do this if the populate flag is set to true.

			if (populate) {
				Set simpleScavengers = ProcessorHelper.getSimpleScavengerSet();
				if (simpleScavengers.isEmpty() == false) {
					DefaultMutableTreeNode t = new DefaultMutableTreeNode("Local Services");
					this.treeModel.insertNodeInto(t, (MutableTreeNode) this.treeModel.getRoot(), this.treeModel
							.getChildCount(this.treeModel.getRoot()));
					for (Iterator i = simpleScavengers.iterator(); i.hasNext();) {
						Scavenger s = (Scavenger) i.next();
						treeModel.insertNodeInto(s, t, treeModel.getChildCount(t));
						treeModel.nodeStructureChanged(t);
					}
				}
				// Add the default soaplab installation if this is defined
				new DefaultScavengerLoaderThread(this);
			} else {
				setExpansion(true);
			}
		}
	}

	String biomobyDefaultURLList, soaplabDefaultURLList, wsdlURLList, webURLList, martRegistryList;

	class DefaultScavengerLoaderThread extends Thread {

		ScavengerTree scavengerTree;

		public DefaultScavengerLoaderThread(ScavengerTree scavengerTree) {
			this.scavengerTree = scavengerTree;
			start();
		}

		public void run() {
			synchronized (this.scavengerTree.getModel()) {
				if (getParentPanel() != null)
					getParentPanel().startProgressBar("Populating service list");
				// Do web scavenger based locations
				if (webURLList != null) {
					String[] urls = webURLList.split("\\s*,\\s*");
					for (int i = 0; i < urls.length; i++) {
						try {
							scavengerTree.addScavenger(new WebScavenger(urls[i], (DefaultTreeModel) scavengerTree
									.getModel()));
						} catch (ScavengerCreationException sce) {
							logger.error(sce);
						}
					}
				}

				// Do mart registries
				if (martRegistryList != null) {
					String[] urls = martRegistryList.split("\\s*,\\s*");
					for (int i = 0; i < urls.length; i++) {
						try {
							scavengerTree.addScavenger(new BiomartScavenger(urls[i]));
						} catch (ScavengerCreationException sce) {
							sce.printStackTrace();
							logger.error(sce);
						}
					}
				}

				// String wsdlURLList =
				// System.getProperty("taverna.defaultwsdl");
				if (wsdlURLList != null) {
					String[] urls = wsdlURLList.split("\\s*,\\s*");
					for (int i = 0; i < urls.length; i++) {
						try {
							scavengerTree.addScavenger(new WSDLBasedScavenger(urls[i]));
						} catch (ScavengerCreationException sce) {
							logger.error(sce);
						}
					}
				}
				// String soaplabDefaultURLList =
				// System.getProperty("taverna.defaultsoaplab");
				if (soaplabDefaultURLList != null) {
					String[] urls = soaplabDefaultURLList.split("\\s*,\\s*");
					for (int i = 0; i < urls.length; i++) {
						try {
							logger.debug("Creating soaplab scavenger : '" + urls[i] + "'");
							scavengerTree.addScavenger(new SoaplabScavenger(urls[i]));
						} catch (ScavengerCreationException sce) {
							logger.error(sce);
						}
					}
				}
				// String biomobyDefaultURLList =
				// System.getProperty("taverna.defaultbiomoby");
				if (biomobyDefaultURLList != null) {
					String[] urls = biomobyDefaultURLList.split("\\s*,\\s*");
					for (int i = 0; i < urls.length; i++) {
						try {
							logger.debug("Creating biomoby scavenger : '" + urls[i] + "'");
							scavengerTree.addScavenger(new BiomobyScavenger(urls[i]));
						} catch (ScavengerCreationException sce) {
							logger.error(sce);
						}
					}
				}

				// Find all apiconsumer.xml files in the classpath root and
				// load them as API Consumer scavengers
				try {
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					Enumeration en = loader.getResources("apiconsumer.xml");
					while (en.hasMoreElements()) {
						URL resourceURL = (URL) en.nextElement();
						scavengerTree.addScavenger(new APIConsumerScavenger(resourceURL));
					}
				} catch (Exception ex) {
					logger.error(ex);
				}

				// Add the seqhound scavenger to the end of the list
				try {
					scavengerTree.addScavenger(new SeqhoundScavenger());
				} catch (ScavengerCreationException sce) {
					logger.error(sce);
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					//
				}
				// I want to disable the reload() as it collapse the tree, even
				// though
				// the user had started exploring it at this point. But I don't
				// know
				// what is the effect of not reload-ing, the API says it should
				// be called when the TreeModel has been modified. --Stian
				treeModel.reload();
				// FIXME: Make this a user setable property. By default now we
				// don't expand the full scavenger tree, as it is too massive
				// scavengerTree.setExpansion(true);
				if (getParentPanel() != null)
					getParentPanel().stopProgressBar();
			}
		}
	}

	/**
	 * Examine the model, create any scavengers that would have been required to
	 * populate the model with its existing processors. Now handles all three
	 * processor types.
	 */
	public void addScavengersFromModel() throws ScavengerCreationException {
		synchronized (this.getModel()) {
			if (this.model != null) {
				addScavengersFromModel(this.model);
			}
		}
	}

	private void addScavengersFromModel(ScuflModel theModel) throws ScavengerCreationException {
		if (theModel != null) {

			// Get all WSDL processors
			Map wsdlLocations = new HashMap();
			Map talismanLocations = new HashMap();
			Map soaplabInstallations = new HashMap();
			Set biomobyCentralLocations = new HashSet();
			Processor[] p = theModel.getProcessors();
			for (int i = 0; i < p.length; i++) {
				// If the processor is a WSDLBasedProcessor then get
				// the wsdl location and add it to the map.
				if (p[i] instanceof WSDLBasedProcessor) {
					String wsdlLocation = ((WSDLBasedProcessor) p[i]).getWSDLLocation();
					wsdlLocations.put(wsdlLocation, null);
				} else if (p[i] instanceof TalismanProcessor) {
					String tscriptLocation = ((TalismanProcessor) p[i]).getTScriptURL();
					talismanLocations.put(tscriptLocation, null);
				} else if (p[i] instanceof SoaplabProcessor) {
					String endpoint = ((SoaplabProcessor) p[i]).getEndpoint().toString();
					String[] parts = endpoint.split("/");
					String base = "";
					for (int j = 0; j < parts.length - 1; j++) {
						base = base + parts[j] + "/";
					}
					soaplabInstallations.put(base, null);
				} else if (p[i] instanceof BiomobyProcessor) {
					String mobyCentralLocation = ((BiomobyProcessor) p[i]).getMobyEndpoint();
					biomobyCentralLocations.add(mobyCentralLocation);
				}
				// Recurse into nested workflows
				else if (p[i] instanceof WorkflowProcessor) {
					addScavengersFromModel(((WorkflowProcessor) p[i]).getInternalModel());
				}
			}
			// Now iterate over all the wsdl locations found and
			// create new WSDL scavengers, adding them to the
			// scavenger tree.
			for (Iterator i = wsdlLocations.keySet().iterator(); i.hasNext();) {
				String wsdlLocation = (String) i.next();
				addScavenger(new WSDLBasedScavenger(wsdlLocation));
			}
			for (Iterator i = talismanLocations.keySet().iterator(); i.hasNext();) {
				String tscriptURL = (String) i.next();
				addScavenger(new TalismanScavenger(tscriptURL));
			}
			for (Iterator i = soaplabInstallations.keySet().iterator(); i.hasNext();) {
				String base = (String) i.next();
				addScavenger(new SoaplabScavenger(base));
			}
			for (Iterator i = biomobyCentralLocations.iterator(); i.hasNext();) {
				String mobyCentralLocation = (String) i.next();
				addScavenger(new BiomobyScavenger(mobyCentralLocation));
			}

		}
	}

	/**
	 * Add a new scavenger to the tree, firing appropriate model events as we
	 * do.
	 */
	public void addScavenger(Scavenger theScavenger) {
		synchronized (getModel()) {
			// Check to see we don't already have a scavenger with this name
			String newName = theScavenger.getUserObject().toString();
			for (Iterator i = scavengerList.iterator(); i.hasNext();) {
				String name = (String) i.next();
				if (name.equals(newName)) {
					// Exit if we already have a scavenger by that name
					return;
				}
			}
			this.scavengerList.add(theScavenger.getUserObject().toString());
			treeModel.insertNodeInto(theScavenger, (MutableTreeNode) this.treeModel.getRoot(), this.treeModel
					.getChildCount(this.treeModel.getRoot()));
			treeModel.nodeStructureChanged(theScavenger);
			// Set the visibility sensibly so that the root node
			// is expanded and visible
			TreePath path = new TreePath(this.root);
			expandPath(path);
		}
	}

	/**
	 * Listen for model bind requests to set the internal ScuflModel field
	 */
	public void attachToModel(ScuflModel theModel) {
		this.model = theModel;
	}

	/**
	 * When unbound from a model, set internal model field to null
	 */
	public void detachFromModel() {
		this.model = null;
	}

	/**
	 * Return an apppropriate title for windows
	 */
	public String getName() {
		return "Available services";
	}

	public javax.swing.ImageIcon getIcon() {
		return ScuflIcons.windowScavenger;
	}

}
