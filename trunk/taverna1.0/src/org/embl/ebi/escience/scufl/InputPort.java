/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;
import java.awt.datatransfer.*;
import java.io.*;
import org.embl.ebi.escience.baclava.DataThing;

/**
 * A port that consumes data on behalf of a processor
 * @author Tom Oinn
 */
public class InputPort extends Port implements Serializable, Transferable {
    
    final public static DataFlavor FLAVOR =
	new DataFlavor(InputPort.class, "Input Port");
    static DataFlavor[] flavors = { FLAVOR };
    private DataThing defaultValue;
    private boolean isOptional = false;

    /**
     * Does this input port have a default value?
     */
    public boolean hasDefaultValue() {
	return (this.defaultValue != null);
    }
    
    /**
     * Get the default value for this port, or
     * null if there is no default
     */
    public DataThing getDefaultValue() {
	return this.defaultValue;
    }
    
    /**
     * Set the default value for this port
     */
    public void setDefaultValue(DataThing defaultValue) {
	this.defaultValue = defaultValue;
    }

    /**
     * Is this input optional?
     */
    public boolean isOptional() {
	return this.isOptional;
    }

    /**
     * Set whether the input is optional (default value if
     * never set is that the input is mandatory)
     */
    public void setOptional(boolean optional) {
	this.isOptional = optional;
    }

    /**
     * Implements transferable interface
     */
    public Object getTransferData(DataFlavor df) 
	throws UnsupportedFlavorException, IOException {
	if (df.equals(FLAVOR)) {
	    return this;
	}
	else {
	    throw new UnsupportedFlavorException(df);
	}
    }
    
    /**
     * Implements transferable interface
     */
    public boolean isDataFlavorSupported(DataFlavor df) {
	return df.equals(FLAVOR);
    }

    /**
     * Implements transferable interface
     */
    public DataFlavor[] getTransferDataFlavors() {
	return flavors;
    }
    
    public InputPort(Processor processor, String name) 
	throws DuplicatePortNameException,
	       PortCreationException {
	super(processor, name);
    }

}
