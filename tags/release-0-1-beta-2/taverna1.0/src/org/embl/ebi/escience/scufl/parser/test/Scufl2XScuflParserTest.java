/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.parser.test;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scufl.parser.Scufl2XScuflParser;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

// Network Imports
import java.net.URL;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.Thread;



/**
 * Attempt to load a chunk of XScufl into a model
 * @author Tom Oinn
 */
public class Scufl2XScuflParserTest {

    public static void main(String args[]) throws Exception {
	
	// Create a new ScuflModel and add the trivial listener
	// to print out all events on it
	ScuflModel model = new ScuflModel();
	model.addListener(new ScuflModelEventPrinter(null));
	
	// Load the Scufl chunk from the test package
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	URL location = loader.getResource("org/embl/ebi/escience/scufl/parser/test/Scufl_example.text");
	XScuflParser.populate(Scufl2XScuflParser.parse(location.openStream()),model,null);
	
    }

}
