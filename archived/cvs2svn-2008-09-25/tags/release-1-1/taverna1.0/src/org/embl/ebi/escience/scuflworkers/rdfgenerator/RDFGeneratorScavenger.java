/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import org.embl.ebi.escience.scuflworkers.rdfgenerator.RDFGeneratorProcessorFactory;
/**
 * A Scavenger that knows how to create rdf generator nodes
 * @author Tom Oinn
 */
public class RDFGeneratorScavenger extends Scavenger {

    /**
     * Create a new String Constant scavenger, the single parameter
     * should is the constant value that any thus created will carry.
     * tscript could be fetched.
     */
    public RDFGeneratorScavenger()
	throws ScavengerCreationException {
	super(new RDFGeneratorProcessorFactory());
    }
}
	
