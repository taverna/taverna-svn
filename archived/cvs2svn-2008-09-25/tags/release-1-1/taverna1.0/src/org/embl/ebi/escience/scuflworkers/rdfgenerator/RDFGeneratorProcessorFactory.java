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
public class RDFGeneratorProcessorFactory extends ProcessorFactory {

    /**
     * Create a new factory configured with the specified
     * constant value
     */
    public RDFGeneratorProcessorFactory() {
	super();
	setName("RDF Generator");
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
