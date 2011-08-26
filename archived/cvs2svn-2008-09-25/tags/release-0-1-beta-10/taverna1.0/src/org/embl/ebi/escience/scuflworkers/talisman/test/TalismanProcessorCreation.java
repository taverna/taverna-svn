/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman.test;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessor;

// Network Imports
import java.net.URL;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



/**
 * Tests the creation of the TalismanProcessor node
 * @author Tom Oinn
 */
public class TalismanProcessorCreation {

    public static void main(String[] args) throws Exception {
	System.out.println("Starting test : TalismanProcessorCreation");
	
	// Create a new ScuflModel
	ScuflModel model = new ScuflModel();
	// Register a listener to print to stdout
	model.addListener(new ScuflModelEventPrinter(null));

	// Fetch the example tscript file from the archive
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	URL location = loader.getResource("org/embl/ebi/escience/scufl/test/tscript.xml");
	System.out.println("Loading talisman script from : "+location.toString());
	String tscriptURL = location.toString();
	

	// Attempt to create a new TalismanProcessor
	model.addProcessor(new TalismanProcessor(model,
						 "my_talisman_processor",
						 tscriptURL));
    }

}
	
