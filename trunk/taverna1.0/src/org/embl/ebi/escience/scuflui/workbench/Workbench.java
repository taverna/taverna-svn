/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

// IO Imports
import java.io.File;

import org.embl.ebi.escience.scuflui.workbench.DiagramFrame;
import org.embl.ebi.escience.scuflui.workbench.ExplorerFrame;
import org.embl.ebi.escience.scuflui.workbench.XScuflFrame;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * A sample workbench application to allow editing and visualization 
 * of Scufl workflows
 * @author Tom Oinn
 */
public class Workbench extends JFrame {
    
    JDesktopPane desktop;

    ScuflModel model;

    final JFileChooser fc = new JFileChooser();

    public static void main(String[] args) {
	Workbench workbench = new Workbench();
	// Add instances of all the components just for fun
	XScuflFrame xscufl = new XScuflFrame(workbench.model);
	xscufl.setSize(600,300);
	xscufl.setLocation(50,50);
	workbench.desktop.add(xscufl);
	DiagramFrame diagram = new DiagramFrame(workbench.model);
	diagram.setSize(300,300);
	diagram.setLocation(50,400);
	workbench.desktop.add(diagram);
	ExplorerFrame explorer = new ExplorerFrame(workbench.model);
	explorer.setSize(300,300);
	explorer.setLocation(400,400);
	workbench.desktop.add(explorer);

	workbench.setVisible(true);
    }

    public Workbench() {
	super("Scufl Workbench");
	int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, 
                  screenSize.width - inset*2, 
                  screenSize.height-inset*2);

	// Initialise the scufl model
	this.model = new ScuflModel();
	
	//Quit this app when the big window closes.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

	// Create the desktop pane and menu
	desktop = new JDesktopPane();
	setContentPane(desktop);
	setJMenuBar(createMenuBar());
	
    }
    
    JMenuBar createMenuBar() {
	JMenuBar menuBar = new JMenuBar();
	// Menu to handle opening XScufl files
	JMenu fileMenu = new JMenu("File");
	JMenuItem openScufl = new JMenuItem("Import XScufl Definition");
	openScufl.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Load an XScufl definition here
		    int returnVal = fc.showOpenDialog(Workbench.this);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
			    XScuflParser.populate(file.toURL().openStream(), Workbench.this.model, null);
			}
			catch (Exception ex) {
			    System.out.println("Exception while opening file! "+ex.getMessage());
			}
		    }
		}
	    });
	fileMenu.add(openScufl);
	
	JMenu windowMenu = new JMenu("Views");
	JMenuItem explorerView = new JMenuItem("Scufl Explorer");
	explorerView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a scufl explorer panel
		    Workbench.this.desktop.add(new ExplorerFrame(Workbench.this.model));
		}
	    });
	windowMenu.add(explorerView);
	JMenuItem diagramView = new JMenuItem("Workflow Diagram");
	diagramView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a scufl diagram panel
		    Workbench.this.desktop.add(new DiagramFrame(Workbench.this.model));
		}
	    });
	windowMenu.add(diagramView);
	JMenuItem xscuflView = new JMenuItem("XScufl View");
	xscuflView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show an XScufl panel
		    Workbench.this.desktop.add(new XScuflFrame(Workbench.this.model));
		}
	    });
	windowMenu.add(xscuflView);
	
	menuBar.add(fileMenu);
	menuBar.add(windowMenu);
	return menuBar;
	
    }

}
