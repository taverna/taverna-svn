/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab.test;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;




/**
 * Tests the creation of SoaplabProcessor nodes
 * @author Tom Oinn
 */
public class SoaplabProcessorCreation {

    public static void main(String[] args) throws Exception {
	System.out.println("Starting test : SoaplabProcessorCreation");
	
	// Create a new ScuflModel
	ScuflModel model = new ScuflModel();
	// Register a listener to print to stdout
	model.addListener(new ScuflModelEventPrinter(null));
	
	// Attempt to create a new SoaplabProcessor
	model.addProcessor(new SoaplabProcessor(model, 
						"my_processor", 
						"http://industry.ebi.ac.uk/soap/soaplab/nucleic_gene_finding::getorf"));
	
	System.out.println("Finished test : SoaplabProcessorCreation");
    }


}
