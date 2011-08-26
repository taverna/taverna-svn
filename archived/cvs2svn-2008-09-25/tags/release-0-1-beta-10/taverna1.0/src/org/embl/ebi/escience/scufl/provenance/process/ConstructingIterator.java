/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

import org.embl.ebi.escience.scufl.*;
import org.jdom.*;

/**
 * Event corresponding to the generation of an implicit
 * iterator.
 * @author Tom Oinn
 */
public class ConstructingIterator extends ProcessEvent {
    
    private IterationStrategy theStrategy = null;

    public ConstructingIterator(IterationStrategy strategy) {
	super();
	theStrategy = strategy;
    }

    public ConstructingIterator() {
	super();
    }
    
    public Element eventElement() {
	Element e = super.eventTopLevelElement();
	if (theStrategy != null) {
	    e.addContent(theStrategy.getElement());
	}
	return e;
    }

}
