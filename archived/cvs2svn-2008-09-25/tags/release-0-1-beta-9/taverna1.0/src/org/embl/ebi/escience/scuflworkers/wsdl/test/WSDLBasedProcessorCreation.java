/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl.test;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;

import java.lang.Exception;
import java.lang.String;
import java.lang.System;



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
						  "search"
						  ));
	XScuflView view = new XScuflView(model);
	System.out.println(view.getXMLText());
	System.out.println("Finished test : WSDLBasedProcessorCreation");
    }

}
