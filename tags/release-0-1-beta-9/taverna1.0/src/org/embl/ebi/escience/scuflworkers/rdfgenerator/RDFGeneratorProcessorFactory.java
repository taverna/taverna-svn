/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.rdfgenerator.RDFGeneratorProcessor;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * RDFGeneratingProcessor nodes
 * @author Tom Oinn
 */
public class RDFGeneratorProcessorFactory implements ProcessorFactory {

    /**
     * Create a new factory configured with the specified
     * constant value
     */
    public RDFGeneratorProcessorFactory() {
	super();
    }
    
    /**
     * Return a constant value as the name
     */
    public String toString() {
	return "RDF Generator";
    }
    
    /**
     * Create a new RDFGeneratingProcessor and add it to the model
     */
    public Processor createProcessor(String name, ScuflModel model)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	Processor theProcessor = new RDFGeneratorProcessor(model, name);
	if (model != null) {
	    model.addProcessor(theProcessor);
	}
	return theProcessor;
    }
    
    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor that generates rdf tuples";
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.rdfgenerator.RDFGeneratorProcessor.class;
    }
    
}
