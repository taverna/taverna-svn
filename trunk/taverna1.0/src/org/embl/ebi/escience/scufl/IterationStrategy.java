/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import org.jdom.*;
import java.util.*;
import org.embl.ebi.escience.baclava.*;

/**
 * Defines the manner in which iterators should be
 * constructed for a given processor instance.
 * @author Tom Oinn
 */
public class IterationStrategy {

    /**
     * Create a new IterationStrategy without any
     * iterator nodes
     */
    public IterationStrategy() {
	//
    }
	
    /**
     * Create a new IterationStrategy from the supplied
     * JDOM Element object
     */
    public IterationStrategy(Element strategyElement) {
	//
    }
    
    /**
     * Write out the current iteration strategy as an
     * Element
     */
    public Element getElement() {
	return new Element("iterationstrategy",XScufl.XScuflNS);
    }
    
    /**
     * Construct a concrete instance of the iteration tree from
     * this strategy object and the Map of port name -> BaclavaIteratorNode
     * objects.
     */
    public ResumableIterator buildIterator(Map iteratorNodes) {
	return new JoinIteratorNode();
    }

}
