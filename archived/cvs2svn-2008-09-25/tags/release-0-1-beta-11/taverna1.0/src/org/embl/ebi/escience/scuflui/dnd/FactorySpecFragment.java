/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.dnd;

import org.jdom.*;
import java.io.*;

/**
 * Contains a JDOM Element and methods to build processors
 * and processor factories from same, specialized to be
 * from the Factory objects for purposes of drag specialization
 * @author Tom Oinn
 */
public class FactorySpecFragment extends SpecFragment implements Serializable {
    
    private String name;

    public FactorySpecFragment(Element element, String name) {
	super(element);
	this.name = name;
    }
    
    /**
     * Will contain the original node name of the processor
     * factory that produced this object, the idea being that
     * the model explorer can use this as the default name
     * for a drop event
     */
    public String getFactoryNodeName() {
	return this.name;
    }
    
}
