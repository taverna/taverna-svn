/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;

import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.Properties;




/**
 * A processor to generate rdf statements
 * @author Tom Oinn
 */
public class RDFGeneratorProcessor extends Processor implements java.io.Serializable {

    /**
     * Construct a new processor with the given model and
     * name, delegates to the superclass.
     */
    public RDFGeneratorProcessor(ScuflModel model, String name)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	// Create the subject, verb, object ports
	try {
	    Port newSubjectPort = new InputPort(this, "subject");
	    Port newVerbPort = new InputPort(this, "verb");
	    Port newObjectPort = new InputPort(this, "object");
	    newSubjectPort.setSyntacticType("'text/plain'");
	    newVerbPort.setSyntacticType("'text/plain'");
	    newObjectPort.setSyntacticType("'text/plain'");
	    this.addPort(newSubjectPort);
	    this.addPort(newVerbPort);
	    this.addPort(newObjectPort);
	    Port newPort = new OutputPort(this, "statement");
	    newPort.setSyntacticType("'text/xml'");
	    this.addPort(newPort);
	}
	catch (Exception ex) {
	    // should never happen
	}
    }

    /**
     * Override the toString method
     */
    public String toString() {
	return "RDF : "+getName();
    }

    /**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
	Properties props = new Properties();
	return props;
    }

}
