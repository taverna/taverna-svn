/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.semantics;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scufl.semantics.RDFSParser;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Test the parser by attempting to load and display
 * the ontology stored at 
 * http://www.cs.man.ac.uk/~wroec/mygrid/ontology/mygrid-reasoned-small.rdfs
 * @author Tom Oinn
 */
public class ParserTest extends JFrame {
    
    public static void main(String[] args) {
	try {
	    URL url = new URL("http://www.cs.man.ac.uk/~wroec/mygrid/ontology/mygrid-reasoned-small.rdfs");
	    System.out.println("Loading ontology data from : "+url.toString());
	    RDFSParser.loadRDFSDocument(url.openStream(), url.toString());
	    System.out.println("Done loading ontology");
	}
	catch (Exception ex) {
	    System.out.println(ex.toString());
	    ex.printStackTrace();
	}
	ParserTest frame = new ParserTest();
	frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
	frame.pack();
	frame.setVisible(true);
    }

    public ParserTest() {
	super("RDFS Parser test");
	JScrollPane view = new JScrollPane(new JTree(RDFSParser.rootNode));
	getContentPane().add(view);
    }

}
