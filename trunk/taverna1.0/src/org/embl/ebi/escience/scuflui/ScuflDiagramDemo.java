/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

// IO Imports
import java.io.File;




/**
 * Demonstrates the functionality of the ScuflDiagram class
 * @author Tom Oinn
 */
public class ScuflDiagramDemo extends JFrame {

    ScuflModel model = new ScuflModel();

    ScuflDiagram diagram = new ScuflDiagram();

    public ScuflDiagramDemo() {
	super("Scufl Diagram Demo Application");
	JScrollPane view = new JScrollPane(diagram);
	view.setBackground(Color.white);
	setBackground(Color.white);
	getContentPane().add(view);
    }
    
    /**
     * Load the model definition from the XScufl file
     * specified as the first argument
     */
    public static void main(String[] args) throws Exception {
	// Create a new Scufl demo frame
	ScuflDiagramDemo frame = new ScuflDiagramDemo();
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    });
	String filename = args[0];
	File inputFile = new File(filename);
	XScuflParser.populate(inputFile.toURL().openStream(), frame.model, null);
	frame.diagram.attachToModel(frame.model);
	frame.pack();
	frame.setVisible(true);
    }
    

    

}
