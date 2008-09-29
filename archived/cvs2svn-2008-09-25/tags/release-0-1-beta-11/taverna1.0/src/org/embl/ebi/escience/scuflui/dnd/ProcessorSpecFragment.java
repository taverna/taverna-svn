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
 * from the Processor objects for purposes of drag specialization
 * @author Tom Oinn
 */
public class ProcessorSpecFragment extends SpecFragment implements Serializable {
    
    private String name;

    public ProcessorSpecFragment(Element element, String processorName) {
	super(element);
	this.name = processorName;
    }
    
    /**
     * Will contain the name in the workflow for the processor from
     * which this spec fragment was derived
     */
    public String getProcessorName() {
	return this.name;
    }

}
