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
import org.embl.ebi.escience.scufl.view.DotView;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.DotTextArea;
import org.embl.ebi.escience.scuflui.ScuflDiagram;
import org.embl.ebi.escience.scuflui.ScuflModelExplorer;
import org.embl.ebi.escience.scuflui.XScuflTextArea;

// IO Imports
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.SoaplabScavenger;
import org.embl.ebi.escience.scuflui.workbench.WSDLBasedScavenger;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Exception;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.System;



/**
 * A sample workbench application to allow editing and visualization 
 * of Scufl workflows
 * @author Tom Oinn
 */
public class Workbench extends JFrame {
    
    protected static ImageIcon openIcon, deleteIcon, importIcon, saveIcon, openurlIcon;

    static {
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.workbench.Workbench");
	    openIcon = new ImageIcon(c.getResource("open.gif"));
	    deleteIcon = new ImageIcon(c.getResource("delete.gif"));
	    saveIcon = new ImageIcon(c.getResource("save.gif"));
	    importIcon = new ImageIcon(c.getResource("import.gif"));
	    openurlIcon = new ImageIcon(c.getResource("openurl.gif"));
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}
    }

    JDesktopPane desktop;

    ScuflModel model;

    final JFileChooser fc = new JFileChooser();

    /**
     * Launch the model workbench, shows the default set of UI components
     * in internal frames and waits for the user to load a model from file
     */
    public static void main(String[] args) {
	Workbench workbench = new Workbench();
	// Treat any command line arguments as files to import into the workbench
	for (int i = 0; i < args.length; i++) {
	    try {
		File inputFile = new File(args[i]);
		XScuflParser.populate(inputFile.toURL().openStream(), workbench.model, null);
	    }
	    catch (Exception e) {
		System.out.println(e.getMessage());
	    }
	}

	// Add instances of all the components just for fun
	GenericUIComponentFrame xscufl = new GenericUIComponentFrame(workbench.model, 
								     new XScuflTextArea());
	xscufl.setSize(600,300);
	xscufl.setLocation(50,50);
	workbench.desktop.add(xscufl);
	GenericUIComponentFrame diagram = new GenericUIComponentFrame(workbench.model, 
								      new ScuflDiagram());
	diagram.setSize(600,600);
	diagram.setLocation(50,400);
	workbench.desktop.add(diagram);
	GenericUIComponentFrame explorer = new GenericUIComponentFrame(workbench.model, 
								       new ScuflModelExplorer());
	explorer.setSize(300,300);
	explorer.setLocation(700,50);
	workbench.desktop.add(explorer);

	try {
	    ScavengerTree s = new ScavengerTree();
	    s.addScavenger(new SoaplabScavenger("http://industry.ebi.ac.uk/soap/soaplab/"));
	    s.addScavenger(new WSDLBasedScavenger("http://www.ebi.ac.uk/xembl/XEMBL.wsdl"));
	    GenericUIComponentFrame scavenger = new GenericUIComponentFrame(workbench.model,
									    s);
	    scavenger.setSize(300,600);
	    scavenger.setLocation(700,400);
	    workbench.desktop.add(scavenger);
	}
	catch (ScavengerCreationException sce) {
	    throw new RuntimeException(sce.getMessage());
	}
	workbench.setVisible(true);
    }

    /**
     * Create a new top level application. This contains a menu bar and
     * desktop pane which in turn acts as the container for the views
     * and controllers. A single ScuflModel is shared between all these
     * contained components.
     */
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
    
    /**
     * Create the menus required by the application
     */
    JMenuBar createMenuBar() {
	JMenuBar menuBar = new JMenuBar();

	// Menu to handle opening XScufl files, clearing the model and saving
	JMenu fileMenu = new JMenu("File");
	JMenuItem openScufl = new JMenuItem("Import XScufl Definition", importIcon);
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
			    JOptionPane.showMessageDialog(null,
							  "Problem opening XScufl from file : \n"+ex.getMessage(),
							  "Exception!",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });
	fileMenu.add(openScufl);
	// Load from web
	JMenuItem openScuflURL = new JMenuItem("Import XScufl Definition from web",openurlIcon);
	openScuflURL.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			String name = (String)JOptionPane.showInputDialog(null,
									  "URL of an XScufl definition to open?",
									  "URL Required",
									  JOptionPane.QUESTION_MESSAGE,
									  null,
									  null,
									  "http://");
			if (name != null) {
			    XScuflParser.populate((new URL(name)).openStream(), Workbench.this.model, null);
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(null,
						      "Problem opening XScufl from web : \n"+ex.getMessage(),
						      "Exception!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	
	fileMenu.add(openScuflURL);
	JMenuItem saveScufl = new JMenuItem("Save as XScufl", saveIcon);
	saveScufl.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Save to XScufl
		    try {
			int returnVal = fc.showSaveDialog(Workbench.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    XScuflView xsv = new XScuflView(Workbench.this.model);
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    out.println(xsv.getXMLText());
			    Workbench.this.model.removeListener(xsv);
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		    }
		}
	    });
	fileMenu.add(saveScufl);
	
	// Sub menu for the various dot save options
	JMenu dotSubMenu = new JMenu("Save as Dot");
	dotSubMenu.setIcon(saveIcon);
	fileMenu.add(dotSubMenu);
	
	JMenuItem noPorts = new JMenuItem("No ports shown", saveIcon);
	JMenuItem boundPorts = new JMenuItem("Bound ports only", saveIcon);
	JMenuItem allPorts = new JMenuItem("All ports shown", saveIcon);
	dotSubMenu.add(noPorts);
	dotSubMenu.add(boundPorts);
	dotSubMenu.add(allPorts);
	noPorts.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			int returnVal = fc.showSaveDialog(Workbench.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    DotView dv = new DotView(Workbench.this.model);
			    dv.setPortDisplay(DotView.NONE);
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    out.println(dv.getDot());
			    Workbench.this.model.removeListener(dv);
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		    }  
		}
	    });
	boundPorts.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			int returnVal = fc.showSaveDialog(Workbench.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    DotView dv = new DotView(Workbench.this.model);
			    dv.setPortDisplay(DotView.BOUND);
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    out.println(dv.getDot());
			    Workbench.this.model.removeListener(dv);
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		    }  
		}
	    });
	allPorts.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			int returnVal = fc.showSaveDialog(Workbench.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    File file = fc.getSelectedFile();
			    DotView dv = new DotView(Workbench.this.model);
			    dv.setPortDisplay(DotView.ALL);
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    out.println(dv.getDot());
			    Workbench.this.model.removeListener(dv);
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		    }  
		}
	    });

	JMenuItem clearModel = new JMenuItem("Reset model data", deleteIcon);
	clearModel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Workbench.this.model.clear();
		}
	    });
	fileMenu.add(clearModel);

	// Menu to show different UI widgets
	JMenu windowMenu = new JMenu("Views");
	JMenuItem explorerView = new JMenuItem("Scufl Explorer");
	explorerView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a scufl explorer panel
		    Workbench.this.desktop.add(new GenericUIComponentFrame(Workbench.this.model,
									   new ScuflModelExplorer()));
		}
	    });
	windowMenu.add(explorerView);
	JMenuItem diagramView = new JMenuItem("Workflow Diagram");
	diagramView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a scufl diagram panel
		    Workbench.this.desktop.add(new GenericUIComponentFrame(Workbench.this.model,
									   new ScuflDiagram()));
		}
	    });
	windowMenu.add(diagramView);
	JMenuItem xscuflView = new JMenuItem("XScufl View");
	xscuflView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show an XScufl panel
		    Workbench.this.desktop.add(new GenericUIComponentFrame(Workbench.this.model,
									   new XScuflTextArea()));
		}
	    });
	windowMenu.add(xscuflView);
	JMenuItem dotView = new JMenuItem("Dot View");
	dotView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a Dot panel
		    Workbench.this.desktop.add(new GenericUIComponentFrame(Workbench.this.model,
									   new DotTextArea()));
		}
	    });
	windowMenu.add(dotView);
	JMenuItem servicePanel = new JMenuItem("Service Panel");
	servicePanel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a service selection panel
		    Workbench.this.desktop.add(new GenericUIComponentFrame(Workbench.this.model,
									   new ScavengerTree()));
		}
	    });
	windowMenu.add(servicePanel);

	

	menuBar.add(fileMenu);
	menuBar.add(windowMenu);
	return menuBar;
	
    }

}
