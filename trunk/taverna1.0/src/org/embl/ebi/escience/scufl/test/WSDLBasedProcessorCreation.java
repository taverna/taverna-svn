/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.test;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.view.*;

/**
 * Tests the creation of a WSDLBasedProcessor node
 * @author Tom Oinn
 */
public class WSDLBasedProcessorCreation {

    public static void main(String[] args) throws Exception {
	System.out.println("Starting test : WSDLBasedProcessorCreation");

	// Create a new ScuflModel
	ScuflModel model = new ScuflModel();
	// Register a listener to print to stdout
	model.addListener(new ScuflModelEventPrinter(null));

	// Attempt to create a new WSDLBasedProcessor
	model.addProcessor(new WSDLBasedProcessor(model,
						  "my_processor",
						  "http://xml.nig.ac.jp/wsdl/Blast.wsdl",
						  "Blast",
						  "search"));
	XScuflView view = new XScuflView(model);
	System.out.println(view.getXMLText());
	System.out.println("Finished test : WSDLBasedProcessorCreation");
    }

}
