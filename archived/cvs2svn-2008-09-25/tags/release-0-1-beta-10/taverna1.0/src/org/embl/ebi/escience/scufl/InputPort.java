/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;
import java.awt.datatransfer.*;
import java.io.*;

/**
 * A port that consumes data on behalf of a processor
 * @author Tom Oinn
 */
public class InputPort extends Port implements Serializable, Transferable {
    
    final public static DataFlavor FLAVOR =
	new DataFlavor(InputPort.class, "Input Port");
    static DataFlavor[] flavors = { FLAVOR };
    
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
