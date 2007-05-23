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
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.raven.spi.RegistryListener;
import net.sf.taverna.raven.spi.SpiRegistry;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScavengerTreePanel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment;
import org.embl.ebi.escience.scuflui.dnd.ProcessorSpecFragment;
import org.embl.ebi.escience.scuflui.dnd.SpecFragmentTransferable;
import org.embl.ebi.escience.scuflui.shared.ExtendedJTree;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.scavenger.spi.ScavengerRegistry;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.embl.ebi.escience.scuflworkers.web.WebScavengerHelper;
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
public class DefaultScavengerTree extends ExtendedJTree implements WorkflowModelViewSPI, ScavengerTree {

	private static Logger logger = Logger.getLogger(DefaultScavengerTree.class);
	
	private static final long serialVersionUID = -5395001232451125620L;

	private ScavengerTreePanel parentPanel = null;

	private boolean populating = false;
	
	// keeps count of each time scavengerStarting has been called, and decreases when scavengingDone is called.
	// it is used to keep track of when the progress bar should stop.
	private int scavengingInProgressCount = 0; 

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTreeX#getParentPanel()
	 */
	public ScavengerTreePanel getParentPanel() {
		return parentPanel;
	}

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTreeX#setPopulating(boolean)
	 */
	public void setPopulating(boolean populating) {
		this.populating = populating;
	}

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTreeX#isPopulating()
	 */
	public boolean isPopulating() {
		return populating;
	}

	/**
	 * The model that this scavenger will create processor for
	 */
	ScuflModel model = null;

	/**
	 * The root node
	 */
	private DefaultMutableTreeNode root = null;

	/**
	 * The tree model
	 */
	DefaultTreeModel treeModel = null;

	/**
	 * A list of the names of all the scavengers contained within this tree
	 */
	ArrayList<String> scavengerList = null;

	/**
	 * A private count to avoid name duplication on created nodes
	 */
	private int count = 0;

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTreeX#getNextCount()
	 */
	public int getNextCount() {
		return count++;
	}	
	
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#scavengingStarting(java.lang.String)
	 */
	public void scavengingStarting(String message) {
		if (parentPanel != null) {
			if (scavengingInProgressCount==0) { //don't overright the previous message if its already running
				parentPanel.startProgressBar(message);
			}
			scavengingInProgressCount++;
		}
	}

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTree#scavengingDone()
	 */
	public void scavengingDone() {
		if (parentPanel != null) {
			scavengingInProgressCount--;
			if (scavengingInProgressCount==0) {
				parentPanel.stopProgressBar();
			}
		}
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
	
	protected void addFromScavengerHelpers() {
		List<ScavengerHelper> helpers = ScavengerHelperRegistry.instance().getScavengerHelpers();
		ScavengerHelperThreadPool threadPool = new ScavengerHelperThreadPool();
		for (ScavengerHelper helper : helpers) {
//				web scavenger is a special case and requires ScavengerTree to construct the Scavengers
			if (helper instanceof WebScavengerHelper) 
			{
				for (Scavenger scavenger : ((WebScavengerHelper)helper).getDefaults(this)) {
					addScavenger(scavenger);
				}
			}
			else {
				if (logger.isDebugEnabled()) logger.debug("Adding helper to thread pool...."+helper.getClass().getSimpleName());
				threadPool.addScavengerHelper(helper);
				
				//FIXME: this sleep sadly seems to be necessary to prevent linkage errors in Raven
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
		}
		
		while (!threadPool.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug(threadPool.remaining() +" threads still waiting to complete.");
				logger.debug(threadPool.waiting() +" threads still waiting in the queue.");
			}
			Set<Scavenger> completed = threadPool.getCompleted();
			logger.debug(completed.size() +" completed scavenger threads found");
			for (Scavenger scavenger : completed) {
				addScavenger(scavenger);
			}
			try {
				if (!threadPool.isEmpty()) Thread.sleep(2500);
			} catch (InterruptedException e1) {
				logger.error("Interruption while waiting sleeping",e1);
			}
		}
		logger.info("Scavenger thread pool completed");
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
	public DefaultScavengerTree(boolean populate, ScavengerTreePanel parentPanel) {
		super();
			this.parentPanel = parentPanel;
			setRowHeight(18);
			setLargeModel(true);

			DragSource dragSource = DragSource.getDefaultDragSource();
			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
			new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
			scavengerList = new ArrayList<String>();
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
				List<Scavenger> simpleScavengers = ScavengerRegistry.instance().getScavengers();
				if (simpleScavengers.isEmpty() == false) {
					DefaultMutableTreeNode t = new DefaultMutableTreeNode("Local Services");
					this.treeModel.insertNodeInto(t, (MutableTreeNode) this.treeModel.getRoot(), this.treeModel
							.getChildCount(this.treeModel.getRoot()));
					for (Scavenger scavenger : simpleScavengers) {
						if (!(scavenger instanceof URLBasedScavenger)) {
							treeModel.insertNodeInto(scavenger, t, treeModel.getChildCount(t));
							treeModel.nodeStructureChanged(t);
						}
					}
				}
				// Add the default soaplab installation if this is defined
				setPopulating(true);
				new DefaultScavengerLoaderThread(this);
				ScavengerHelperRegistry.instance().addRegistryListener(new RegistryListener() {					
					
					public void spiRegistryUpdated(SpiRegistry registry) {
						logger.info("Registry updated for class:"+registry.getClassName());
						new DefaultScavengerLoaderThread(DefaultScavengerTree.this);												
					}
					
				});
			} else {
				setPopulating(false);
				setExpansion(true);
			}
	}	
	
	
	
	

	class DefaultScavengerLoaderThread extends Thread {

		ScavengerTree scavengerTree;

		public DefaultScavengerLoaderThread(ScavengerTree scavengerTree) {
			this.scavengerTree = scavengerTree;
			start();
		}

		public void run() {
				scavengingStarting("Populating service list");											

				addFromScavengerHelpers();
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
				scavengingDone();
				scavengerTree.setPopulating(false);
		}

		
	}

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTreeX#addScavengersFromModel()
	 */
	public void addScavengersFromModel() throws ScavengerCreationException {
		getParentPanel().startProgressBar("Adding scavengers from model.");
			if (this.model != null) {
				addScavengersFromModel(this.model);
			}
		getParentPanel().stopProgressBar();
	}

	private void addScavengersFromModel(ScuflModel theModel) throws ScavengerCreationException {
		if (theModel != null) {

			List<ScavengerHelper> helpers = ScavengerHelperRegistry.instance().getScavengerHelpers();
			for (ScavengerHelper helper : helpers) {
				Set<Scavenger> scavengers = helper.getFromModel(theModel);
				for (Scavenger scavenger : scavengers) {
					addScavenger(scavenger);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflui.workbench.ScavengerTreeX#addScavenger(org.embl.ebi.escience.scuflui.workbench.Scavenger)
	 */
	public void addScavenger(Scavenger theScavenger) {
		synchronized (getModel()) {
			// Check to see we don't already have a scavenger with this name
			String newName = theScavenger.getUserObject().toString();
			if (!scavengerList.contains(newName)) {			
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
	}

	/**
	 * Listen for model bind requests to set the internal ScuflModel field
	 */
	public void attachToModel(ScuflModel theModel) {
		if (this.model!=null) {
			logger.warn("Did not detachFromModel() before attachToModel()");
			detachFromModel();
		}
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
		return TavernaIcons.windowScavenger;
	}

	public void onDisplay() {
		
	}

	public void onDispose() {
		detachFromModel();		
	}

}

	