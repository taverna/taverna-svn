/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view.test;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.DotView;

// Network Imports
import java.net.URL;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;
import java.lang.Thread;



/**
 * Attempts to load data into a ScuflModel from the
 * same source that the XScuflParserTest uses, then
 * to print out the dot representation of the model
 * to stdout.
 * @author Tom Oinn
 */
public class DotViewTest {

    public static void main(String args[]) throws Exception {
	
	ScuflModel model = new ScuflModel();
	DotView view = new DotView(model);
	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	URL location = loader.getResource("org/embl/ebi/escience/scufl/view/test/complete_example_workflow.xml");
	System.out.println("Loading definition from : "+location.toString());
	XScuflParser.populate(location.openStream(), model, null);
	System.out.println(view.getDot());
	System.out.println("The dot file above can be rendered, assuming you have the dot binary, by putting it into a file and calling something like 'dot -Tgif < input.dot > output.gif'");
	
    }

}
