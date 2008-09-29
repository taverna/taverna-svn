/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

// IO Imports
import java.io.File;

import org.embl.ebi.escience.scuflui.ScuflDiagramDemo;
import org.embl.ebi.escience.scuflui.ScuflModelExplorerDemo;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Demos the interaction between the views, model and controllers
 * defined within this package.
 */
public class ComponentDemo {

    public static void main(String[] args) throws Exception {
	ScuflModel model = new ScuflModel();
	String filename = args[0];
	File inputFile = new File(filename);
	XScuflParser.populate(inputFile.toURL().openStream(), model, null);
	// Create a tree view
	ScuflModelExplorerDemo tree = new ScuflModelExplorerDemo();
	// Exit the application when the tree is closed
	tree.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
	tree.explorer.attachToModel(model);
	tree.pack();
	tree.setVisible(true);
	ScuflDiagramDemo diagram = new ScuflDiagramDemo();
	diagram.diagram.bindToModel(model);
	diagram.pack();
	diagram.setVisible(true);
    }

}
