/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.tools;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.DotView;

// IO Imports
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Command line tool to read in an XScufl definition and produce the dot
 * file corresponding to it.
 * @author Tom Oinn
 */
public class XScufl2Dot {

    public static void main(String[] args) throws Exception {
	// First command line argument is the location of the
	// xscufl file to load into the data model.
	try {
	    int portPolicy = DotView.ALL;
	    String filename = args[0];
	    if (args.length == 3) {
		// defaults to showing all ports.
		String portPolicyString = args[2];
		if (portPolicyString.equalsIgnoreCase("none")) {
		    portPolicy = DotView.NONE;
		}
		else if (portPolicyString.equalsIgnoreCase("bound")) {
		    portPolicy = DotView.BOUND;
		}
		
	    }
	    String outfilename = args[1];
	    PrintWriter out = new PrintWriter(new FileWriter(outfilename));

	    // Create a new scuflmodel
	    ScuflModel model = new ScuflModel();
	    // Register a dot view with it
	    DotView view = new DotView(model);
	    //model.addListener(new ScuflModelEventPrinter(null));
	    // Decide how much information to show
	    view.setPortDisplay(portPolicy);
	    File inputFile = new File(filename);
	    XScuflParser.populate(inputFile.toURL().openStream(), model, null);
	    out.println(view.getDot());
	    out.flush();
	    out.close();
	}
	catch (ArrayIndexOutOfBoundsException aioobe) {
	    System.out.println("Usage : ... XScufl2Dot <xscuflfilename> <outputfilename> [none|bound|all]");
	}
	catch (IOException ioe) {
	    System.out.println("IOException : "+ioe.getMessage());
	}
    }

}
