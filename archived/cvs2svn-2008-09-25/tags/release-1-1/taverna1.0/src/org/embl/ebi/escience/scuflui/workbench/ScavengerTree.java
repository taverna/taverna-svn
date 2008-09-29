/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.*;
import javax.swing.tree.TreeSelectionModel;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabScavenger;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessor;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanScavenger;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedScavenger;
import org.embl.ebi.escience.scuflworkers.seqhound.SeqhoundScavenger;
import org.embl.ebi.escience.scuflworkers.apiconsumer.APIConsumerScavenger;
import java.net.URL;
import org.embl.ebi.escience.scuflworkers.biomoby.*;

import org.jdom.output.*;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.List;

import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreePopupHandler;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreeRenderer;
import org.embl.ebi.escience.scuflui.dnd.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import org.jdom.*;
import java.lang.String;
import org.embl.ebi.escience.scuflworkers.*;
import java.awt.*;
import java.io.File;



/**
 * A JTree subclass showing available processors from some
 * set of external libraries or searches. Nodes corresponding
 * to a single potential processor instance should contain
 * a user object implementing ProcessorFactory.
 * @author Tom Oinn
 */
public class ScavengerTree extends ExtendedJTree 
    implements ScuflUIComponent,
	       DragSourceListener,
	       DragGestureListener,
	       DropTargetListener {
    
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
     * Recognize the drag gesture, only allow if there's a
     * processor factory node here, and export the XML
     * fragment from the node as the transferable
     */
    public void dragGestureRecognized(DragGestureEvent e) {
	// Get the node that was dragged
	Point l = e.getDragOrigin();
	TreePath dragSourcePath = getPathForLocation((int)l.getX(), (int)l.getY());
	if(dragSourcePath != null)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)dragSourcePath.getLastPathComponent();
		Object userObject = node.getUserObject();
		if (userObject instanceof ProcessorFactory) {
		    Element el = ((ProcessorFactory)userObject).getXMLFragment();
		    String name = ((ProcessorFactory)userObject).getName();
		    FactorySpecFragment fsf = new FactorySpecFragment(el, name);
		    Transferable t = new SpecFragmentTransferable(fsf);
		    e.startDrag(DragSource.DefaultCopyDrop,
				t,
				this);		
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
		ProcessorSpecFragment psf = (ProcessorSpecFragment)t.getTransferData(f);
		XMLOutputter xo = new XMLOutputter();
		// Remove the various fault tolerance etc attributes
		Element processorElement = psf.getElement();
		List attributes = processorElement.getAttributes();
		for (Iterator i = attributes.iterator(); i.hasNext();) {
		    Attribute att = (Attribute)i.next();
		    processorElement.removeAttribute(att);
		}
		String searchString = xo.outputString(psf.getElement());
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot(); 
		Enumeration en = rootNode.depthFirstEnumeration();
		while (en.hasMoreElements()) {
		    DefaultMutableTreeNode theNode = (DefaultMutableTreeNode)en.nextElement();
		    Object o = theNode.getUserObject();
		    if (o instanceof ProcessorFactory) {
			String compare = xo.outputString(((ProcessorFactory)o).getXMLFragment());
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
	}
	catch (Exception ex) {
	    e.rejectDrop();
	}
    }

    /**
     * Default constructor, equivalent to calling with populate
     * set to 'true'
     */
    public ScavengerTree() {
	this(true);
    }

    /**
     * Create a new scavenger tree, if the boolean 'populate' flag
     * is true then load the default service set from the system
     * properties, otherwise start with a completely blank panel.
     */
    public ScavengerTree(boolean populate) {
	super();
	setRowHeight(0);
	wsdlURLList = System.getProperty("taverna.defaultwsdl");
	soaplabDefaultURLList = System.getProperty("taverna.defaultsoaplab");
	biomobyDefaultURLList = System.getProperty("taverna.defaultbiomoby");
	webURLList = System.getProperty("taverna.defaultweb");
	DragSource dragSource = DragSource.getDefaultDragSource();
	dragSource.createDefaultDragGestureRecognizer(this,
						      DnDConstants.ACTION_COPY_OR_MOVE,
						      this);
	new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
	scavengerList = new ArrayList();
	root = new DefaultMutableTreeNode("Available Processors");
	treeModel = (DefaultTreeModel)this.getModel();
	treeModel.setRoot(this.root);
	putClientProperty("JTree.lineStyle","Angled");
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
		this.treeModel.insertNodeInto(t, 
					      (MutableTreeNode)this.treeModel.getRoot(),
					      this.treeModel.getChildCount(this.treeModel.getRoot()));
		for (Iterator i = simpleScavengers.iterator(); i.hasNext(); ) {
		    Scavenger s = (Scavenger)i.next();
		    this.treeModel.insertNodeInto(s,t,this.treeModel.getChildCount(t));
		}
	    }
	    // Add the default soaplab installation if this is defined
	    new DefaultScavengerLoaderThread(this);
	}
	else {
	    setExpansion(true);
	}
    }

    String biomobyDefaultURLList, soaplabDefaultURLList, wsdlURLList, webURLList;

    class DefaultScavengerLoaderThread extends Thread {
	
	ScavengerTree scavengerTree;
	
	public DefaultScavengerLoaderThread(ScavengerTree scavengerTree) {
	    this.scavengerTree = scavengerTree;
	    start();
	}
	
	public void run() {

	    // Do web scavenger based locations
	    if (webURLList != null) {
		String[] urls = webURLList.split("\\s*,\\s*");
		for (int i = 0; i < urls.length; i++) {
		    try {
			scavengerTree.addScavenger(new WebScavenger(urls[i], (DefaultTreeModel)scavengerTree.getModel()));
		    }
		    catch (ScavengerCreationException sce) {
			sce.printStackTrace();
		    }
		}
	    }
	    
	    //String wsdlURLList = System.getProperty("taverna.defaultwsdl");
	    if (wsdlURLList != null) {
		String[] urls = wsdlURLList.split("\\s*,\\s*");
		for (int i = 0; i < urls.length; i++) {
		    try {
			scavengerTree.addScavenger(new WSDLBasedScavenger(urls[i]));
		    }
		    catch (ScavengerCreationException sce) {
			sce.printStackTrace();
		    }
		}
	    }
	    //String soaplabDefaultURLList = System.getProperty("taverna.defaultsoaplab");
	    if (soaplabDefaultURLList != null) {
		String[] urls = soaplabDefaultURLList.split("\\s*,\\s*");
		for (int i = 0; i < urls.length; i++) {
		    try {
			System.out.println("Creating soaplab scavenger : '"+urls[i]+"'");
			scavengerTree.addScavenger(new SoaplabScavenger(urls[i]));
		    }
		    catch (ScavengerCreationException sce) {
			sce.printStackTrace();
		    }
		}
	    }
	    //String biomobyDefaultURLList = System.getProperty("taverna.defaultbiomoby");
	    if (biomobyDefaultURLList != null) {
		String[] urls = biomobyDefaultURLList.split("\\s*,\\s*");
		for (int i = 0; i < urls.length; i++) {
		    try {
			System.out.println("Creating biomoby scavenger : '"+urls[i]+"'");
			scavengerTree.addScavenger(new BiomobyScavenger(urls[i]));
		    }
		    catch (ScavengerCreationException sce) {
			sce.printStackTrace();
		    }
		}
	    } 

	    // Find all apiconsumer.xml files in the classpath root and
	    // load them as API Consumer scavengers
	    try {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Enumeration en = loader.getResources("apiconsumer.xml");
		while (en.hasMoreElements()) {
		    URL resourceURL = (URL)en.nextElement();
		    scavengerTree.addScavenger(new APIConsumerScavenger(resourceURL));
		}
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
	    }

	    // Add the seqhound scavenger to the end of the list
	    try {
		scavengerTree.addScavenger(new SeqhoundScavenger());
	    }
	    catch (ScavengerCreationException sce) {
		sce.printStackTrace();
	    }
	    scavengerTree.setExpansion(true);
	}
    }

    /**
     * Examine the model, create any scavengers that would have been required
     * to populate the model with its existing processors. Now handles all three
     * processor types.
     */
    public void addScavengersFromModel() 
	throws ScavengerCreationException {
	if (this.model != null) {
	    // Get all WSDL processors
	    Map wsdlLocations = new HashMap();
	    Map talismanLocations = new HashMap();
	    Map soaplabInstallations = new HashMap();
	    Set biomobyCentralLocations = new HashSet();
	    Processor[] p = model.getProcessors();
	    for (int i = 0; i < p.length; i++) {
		// If the processor is a WSDLBasedProcessor then get
		// the wsdl location and add it to the map.
		if (p[i] instanceof WSDLBasedProcessor) {
		    String wsdlLocation = ((WSDLBasedProcessor)p[i]).getWSDLLocation();
		    wsdlLocations.put(wsdlLocation,null);
		}
		else if (p[i] instanceof TalismanProcessor) {
		    String tscriptLocation = ((TalismanProcessor)p[i]).getTScriptURL();
		    talismanLocations.put(tscriptLocation,null);
		}
		else if (p[i] instanceof SoaplabProcessor) {
		    String endpoint = ((SoaplabProcessor)p[i]).getEndpoint().toString();
		    String[] parts = endpoint.split("/");
		    String base = "";
		    for (int j = 0; j < parts.length -1; j++) {
			base = base + parts[j] + "/";
		    }
		    soaplabInstallations.put(base,null);
		}
		else if (p[i] instanceof BiomobyProcessor) {
		    String mobyCentralLocation = ((BiomobyProcessor)p[i]).getMobyEndpoint();
		    biomobyCentralLocations.add(mobyCentralLocation);
		}
	    }
	    // Now iterate over all the wsdl locations found and
	    // create new WSDL scavengers, adding them to the 
	    // scavenger tree.
	    for (Iterator i = wsdlLocations.keySet().iterator(); i.hasNext(); ) {
		String wsdlLocation = (String)i.next();
		addScavenger(new WSDLBasedScavenger(wsdlLocation));
	    }
	    for (Iterator i = talismanLocations.keySet().iterator(); i.hasNext(); ) {
		String tscriptURL = (String)i.next();
		addScavenger(new TalismanScavenger(tscriptURL));
	    }
	    for (Iterator i = soaplabInstallations.keySet().iterator(); i.hasNext(); ) {
		String base = (String)i.next();
		addScavenger(new SoaplabScavenger(base));
	    }
	    for (Iterator i = biomobyCentralLocations.iterator(); i.hasNext();) {
		String mobyCentralLocation = (String)i.next();
		addScavenger(new BiomobyScavenger(mobyCentralLocation));
	    }
	}
    }

    /**
     * Add a new scavenger to the tree, firing appropriate
     * model events as we do.
     */
    public synchronized void addScavenger(Scavenger theScavenger) {
	// Check to see we don't already have a scavenger with this name
	String newName = theScavenger.getUserObject().toString();
	for (Iterator i = scavengerList.iterator(); i.hasNext(); ) {
	    String name = (String)i.next();
	    if (name.equals(newName)) {
		// Exit if we already have a scavenger by that name
		return;
	    }
	}
	this.scavengerList.add(theScavenger.getUserObject().toString());
	this.treeModel.insertNodeInto(theScavenger, 
				      (MutableTreeNode)this.treeModel.getRoot(),
				      this.treeModel.getChildCount(this.treeModel.getRoot()));
	// Set the visibility sensibly so that the root node
	// is expanded and visible
	TreePath path = new TreePath(this.root);
	expandPath(path);
    }
    
    /**
     * Listen for model bind requests to set the internal
     * ScuflModel field
     */
    public void attachToModel(ScuflModel theModel) {
	this.model = theModel;
    }

    /**
     * When unbound from a model, set internal model field
     * to null
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
    
