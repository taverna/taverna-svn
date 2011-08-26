/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.parser.test;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEventPrinter;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;

// Network Imports
import java.net.URL;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;

/**
 * Attempt to load a chunk of XScufl into a model
 * 
 * @author Tom Oinn
 */
public class XScuflParserTest {

	public static void main(String args[]) throws Exception {

		// Create a new ScuflModel and add the trivial listener
		// to print out all events on it
		ScuflModel model = new ScuflModel();
		model.addListener(new ScuflModelEventPrinter(null));
		XScuflView view = new XScuflView(model);

		// Load the XScufl chunk from the test package
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL location = loader
				.getResource("org/embl/ebi/escience/scufl/parser/test/example_sink_source.xml");
		System.out.println("Loading definition from : " + location.toString());
		// Use it to populate the model, names do not have
		// prefixes applied to them.
		XScuflParser.populate(location.openStream(), model, null);

		// And then do the same, but prefixing with 'foo' to test that
		// we can import more than one model.
		location = loader
				.getResource("org/embl/ebi/escience/scufl/parser/test/example_sink_source.xml");
		System.out.println("Loading definition from : " + location.toString());
		XScuflParser.populate(location.openStream(), model, "foo");

		System.out.println(view.getXMLText());
	}

}
