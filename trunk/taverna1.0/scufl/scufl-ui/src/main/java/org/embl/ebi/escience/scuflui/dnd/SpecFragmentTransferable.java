/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.jdom.Element;

/**
 * This transferable data type encapsulates a SpecFragment
 * object, and is used for drag and drop of the processor
 * spec fragments used to instantiate both Processor and
 * ProcessorFactory classes.
 * @author Tom Oinn
 */
public class SpecFragmentTransferable implements Transferable {
    
    public static DataFlavor specFragmentFlavor = null;
    public static DataFlavor processorSpecFragmentFlavor = null;
    public static DataFlavor factorySpecFragmentFlavor = null;
    
    static {
	try {
	    specFragmentFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						"; class=org.embl.ebi.escience.scuflui.dnd.SpecFragment",
						"Taverna Spec Element Object");
	    processorSpecFragmentFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
							 "; class=org.embl.ebi.escience.scuflui.dnd.ProcessorSpecFragment",
							 "Taverna Processor Spec Element Object");
	    factorySpecFragmentFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
						       "; class=org.embl.ebi.escience.scuflui.dnd.FactorySpecFragment",
						       "Taverna Factory Spec Element Object");
	}
	catch (Exception e) {
	    System.err.println(e);
	}
    }

    private SpecFragment data;
    
    /** 
     * Construct a transferable directly from the spec element,
     * using the constructor in the SpecFragment class to build
     * the internal data
     */
    public SpecFragmentTransferable(Element specElement) {
	this.data = new SpecFragment(specElement);
    }
    
    /**
     * Construct a transferable from the spec fragment object
     */
    public SpecFragmentTransferable(SpecFragment fragment) {
	this.data = fragment;
    }
    
    /**
     * Return the single local flavor
     */
    public DataFlavor[] getTransferDataFlavors() {
	DataFlavor[] flavors = new DataFlavor[2];
	// Generic spec fragment is always available
	flavors[1] = SpecFragmentTransferable.specFragmentFlavor;
	// More specific one depends on what we're constructed with
	if (this.data instanceof ProcessorSpecFragment) {
	    flavors[0] = SpecFragmentTransferable.processorSpecFragmentFlavor;
	    return flavors;
	}
	else if (this.data instanceof FactorySpecFragment) {
	    flavors[0] = SpecFragmentTransferable.factorySpecFragmentFlavor;
	    return flavors;
	}
	// If neither is true then just return a single element list with
	// the base flavor in
	return new DataFlavor[]{SpecFragmentTransferable.specFragmentFlavor};
    }
    
    /**
     * True if the supplied flavor is equal to the supported one
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
	// Always support the basic spec element flavor
	if (flavor.equals(SpecFragmentTransferable.specFragmentFlavor)) {
	    return true;
	}
	// If the contained spec element is from a processor and
	// the requested flavor is the processor flavor return true
	if (this.data instanceof ProcessorSpecFragment &&
	    flavor.equals(SpecFragmentTransferable.processorSpecFragmentFlavor)) {
	    return true;
	}
	// If from a factory and requested flavor is the factory
	// one then return true
	if (this.data instanceof FactorySpecFragment &&
	    flavor.equals(SpecFragmentTransferable.factorySpecFragmentFlavor)) {
	    return true;
	}
	// Otherwise return false
	return false;
    }

    /**
     * Return the spec fragment if requested
     */
    public Object getTransferData(DataFlavor flavor) 
	throws UnsupportedFlavorException {
	if (isDataFlavorSupported(flavor)) {
	    return this.data;
	}
	else {
	    throw new UnsupportedFlavorException(flavor);
	}
    }
    

}
