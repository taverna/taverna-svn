/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.tools;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.*;
import org.embl.ebi.escience.scufl.view.*;
import java.io.*;

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
	    if (args.length == 2) {
		// defaults to showing all ports.
		String portPolicyString = args[1];
		if (portPolicyString.equalsIgnoreCase("none")) {
		    portPolicy = DotView.NONE;
		}
		else if (portPolicyString.equalsIgnoreCase("bound")) {
		    portPolicy = DotView.BOUND;
		}
		
	    }
	    


	    // Create a new scuflmodel
	    ScuflModel model = new ScuflModel();
	    // Register a dot view with it
	    DotView view = new DotView(model);
	    // Decide how much information to show
	    view.setPortDisplay(portPolicy);
	    File inputFile = new File(filename);
	    XScuflParser.populate(inputFile.toURL().openStream(), model, null);
	    System.out.println(view.getDot());
	}
	catch (ArrayIndexOutOfBoundsException aioobe) {
	    System.out.println("Usage : ... XScufl2Dot <xscuflfilename> [none|bound|all]");
	}
    }

}
