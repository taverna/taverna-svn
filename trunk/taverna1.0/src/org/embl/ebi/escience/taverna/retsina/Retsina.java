package org.embl.ebi.escience.taverna.retsina;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.*;

import org.embl.ebi.escience.taverna.retsina.ScuflGraphPanel;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * This is the top level container for the Retsina graph 
 * editor. It can be instantiated as an applet or as an
 * application through the main() method.
 * @author Tom Oinn
 */
public class Retsina extends JApplet {
    
    private static String[] services = new String[0];

    /**
     * Takes the URL to the AnalysisFactory web service as first
     * parameter in order to be able to get a list of the services
     */
    public static void main(String[] args) {
	// Fetch the service list from Soaplab
	try {
	    services = getServicesFromSoaplab(args[0]);
	}
	catch (Exception e) {
	    System.out.println("You must supply a URL to an AnalysisFactory SOAP service endpoint as first parameter");
	    System.exit(-1);
	}
	JFrame f = new JFrame("Retsina graph editor, application mode");
	f.addWindowListener(new java.awt.event.WindowAdapter() {
		public void windowClosing(java.awt.event.WindowEvent e) {
		    System.exit(0);
		};
	    });
	
	Retsina r = new Retsina();
	f.getContentPane().add(r);
	f.pack();
	r.init();
	f.setSize(800,400);
	f.show();
    }

    public Retsina() {
	super();
    }

    public void init() {
	// Create the list of services
	JList servicelist = new JList(services);
	// Put it in a scrollpane
	JScrollPane servicelistpane = new JScrollPane(servicelist, 
						      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	servicelistpane.setViewportBorder(BorderFactory.createTitledBorder("Services"));
	// Create the graph edit view
	ScuflGraphPanel graph = new ScuflGraphPanel(null);
	
	// Put the components in the content pane
	Container contentpane = getContentPane();
	JPanel pane = new JPanel();
	pane.setLayout(new BorderLayout());
	pane.add(servicelistpane,BorderLayout.WEST);
	pane.add(graph,BorderLayout.CENTER);
	contentpane.add(pane, BorderLayout.CENTER);
    }

    // Just to play around with, will delegate to 
    // a method to retrieve configured services in
    // the final version.
    private static String[] getServices() {
	return new String[]{"seqret","emma","prophet","prophecy","plotorf"};
    }

    /**
     * Connect to Soaplab and fetch all the services available.
     * Of course, this won't work for applets as they're not 
     * allowed to make that particular network connection.
     */
    private static String[] getServicesFromSoaplab(String endpoint) {
	return org.embl.ebi.escience.taverna.QueryServices.getServices(endpoint);
    }
    
}
