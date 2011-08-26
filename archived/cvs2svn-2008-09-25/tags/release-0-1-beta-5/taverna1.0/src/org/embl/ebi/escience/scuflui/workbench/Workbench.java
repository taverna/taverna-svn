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
import org.embl.ebi.escience.scufl.semantics.RDFSParser;
import org.embl.ebi.escience.scufl.view.DotView;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.DotTextArea;
import org.embl.ebi.escience.scuflui.EnactorLaunchPanel;
import org.embl.ebi.escience.scuflui.ScuflDiagram;
import org.embl.ebi.escience.scuflui.ScuflModelExplorer;
import org.embl.ebi.escience.scuflui.XScuflTextArea;
import uk.ac.mrc.hgmp.taverna.retsina.Retsina;

// Utility Imports
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

// IO Imports
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflui.workbench.FileDrop;
import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.SplashScreen;
import java.lang.Class;
import java.lang.ClassLoader;
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
    
    public static ImageIcon openIcon, deleteIcon, importIcon, saveIcon, openurlIcon;

    /**
     * If the workbench is created, it will set this
     * to the instance value. This allows ui components
     * that otherwise don't have a handle to the desktop
     * pane to create themselves in internal windows. This
     * especially applies to the workflow run panel
     */
    public static Workbench workbench = null;

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
	
	// Initialize the proxy settings etc.
	ResourceBundle rb = ResourceBundle.getBundle("mygrid");
        Properties sysProps = System.getProperties();
        Enumeration keys = rb.getKeys();
	while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) rb.getString(key);
	    sysProps.put(key, value);
        }
    }

    public JDesktopPane desktop;

    public ScuflModel model;

    final JFileChooser fc = new JFileChooser();

    /**
     * Launch the model workbench, shows the default set of UI components
     * in internal frames and waits for the user to load a model from file
     */
    public static void main(String[] args) {
	new SplashScreen(6000);	
	// Load the test ontology for the annotation of workflow
	// source and sink ports
	try {
	    System.out.println("Loading ontologies...");
	    URL ontologyURL = 
		ClassLoader.getSystemResource("org/embl/ebi/escience/scufl/semantics/mygrid-reasoned-small.rdfs");
	    RDFSParser.loadRDFSDocument(ontologyURL.openStream(), "internal test ontology");
	    System.out.println("Done loading ontologies.");
	}
	catch (Exception ex) {
	    System.out.println("Failed to load ontology data! "+ex.getMessage());
	    ex.printStackTrace();
	}
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

	GenericUIComponentFrame scavenger = new GenericUIComponentFrame(workbench.model,
									new ScavengerTree());
	scavenger.setSize(300,600);
	scavenger.setLocation(700,400);
	workbench.desktop.add(scavenger);
	
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
	Workbench.workbench = this;
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
	
	// Add a filedrop listener to allow users to drag
	// workflow definition in (cheers to Robert Harder!)
	// http://iharder.sourceforge.net/
	new FileDrop(desktop, new FileDrop.Listener() {
		public void filesDropped(File[] files) {
		    if (files.length == 1) {
			// Don't use a prefix, single drop event
			try {
			    XScuflParser.populate(files[0].toURL().openStream(), Workbench.this.model, null);
			}
			catch (Exception ex) {
			    JOptionPane.showMessageDialog(null,
							  "Problem opening XScufl from file : \n"+ex.getMessage(),
							  "Exception!",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
		    else {
			for (int i = 0; i < files.length; i++) {
			    try {
				XScuflParser.populate(files[0].toURL().openStream(), Workbench.this.model, ""+i);
			    }
			    catch (Exception ex) {
				JOptionPane.showMessageDialog(null,
							      "Problem opening XScufl from file : \n"+ex.getMessage(),
							      "Exception!",
							      JOptionPane.ERROR_MESSAGE);
			    }
			}
		    }
		}
	    });
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
        JMenuItem retsinaView = new JMenuItem("EMBOSS Flow Builder");
        retsinaView.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  Retsina retsinaPane = new Retsina();
                  GenericUIComponentFrame retsina = new GenericUIComponentFrame(Workbench.this.model,retsinaPane);
                  Workbench.this.desktop.add(retsina);
                  retsina.setSize(650,600);
                  retsina.moveToFront();
               }
            });
        windowMenu.add(retsinaView);

	JMenuItem explorerView = new JMenuItem("Scufl Explorer");
	explorerView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a scufl explorer panel
		    ScuflModelExplorer thing = new ScuflModelExplorer();
		    GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
		    Workbench.this.desktop.add(frame);
		    frame.moveToFront();
		}
	    });
	windowMenu.add(explorerView);
	JMenuItem diagramView = new JMenuItem("Workflow Diagram");
	diagramView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a scufl diagram panel
		    ScuflDiagram thing = new ScuflDiagram();
		    GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
		    Workbench.this.desktop.add(frame);
		    frame.moveToFront();
		}
	    });
	windowMenu.add(diagramView);
	JMenuItem xscuflView = new JMenuItem("XScufl View");
	xscuflView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show an XScufl panel
		    XScuflTextArea thing = new XScuflTextArea();
		    GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
		    Workbench.this.desktop.add(frame);
		    frame.moveToFront();
		}
	    });
	windowMenu.add(xscuflView);
	JMenuItem dotView = new JMenuItem("Dot View");
	dotView.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a Dot panel
		    DotTextArea thing = new DotTextArea();
		    GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
		    Workbench.this.desktop.add(frame);
		    frame.moveToFront();
		}
	    });
	windowMenu.add(dotView);
	JMenuItem servicePanel = new JMenuItem("Service Panel");
	servicePanel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a service selection panel
		    ScavengerTree thing = new ScavengerTree();
		    GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
		    Workbench.this.desktop.add(frame);
		    frame.moveToFront();
		}
	    });
	windowMenu.add(servicePanel);
	JMenuItem inputPanel = new JMenuItem("Workflow Input Panel");
	inputPanel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a workflow input panel
		    EnactorLaunchPanel thing = new EnactorLaunchPanel();
		    GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
		    Workbench.this.desktop.add(frame);
		    frame.moveToFront();
		}
	    });
	windowMenu.add(inputPanel);
	

	menuBar.add(fileMenu);
	menuBar.add(windowMenu);
	return menuBar;
	
    }

}
