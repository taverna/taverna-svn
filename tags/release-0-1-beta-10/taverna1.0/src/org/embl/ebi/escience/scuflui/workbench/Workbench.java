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
import java.util.*;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.implementation.*;
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
import java.util.prefs.Preferences;

// IO Imports
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflui.workbench.FileDrop;
import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.ScrollableDesktopPane;
import org.embl.ebi.escience.scuflui.workbench.SplashScreen;
import org.embl.ebi.escience.scuflui.*;
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

    public static ImageIcon openIcon, deleteIcon, importIcon, saveIcon, openurlIcon, background;

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
	    try {
		background = new ImageIcon(c.getResource("background.png"));
	    }
	    catch (Exception e) {
		background = null;
	    }
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}

	// Initialize the component SPI
	UIComponentRegistry.instance();

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

    /**
     * Launch the model workbench, shows the default set of UI components
     * in internal frames and waits for the user to load a model from file
     */
    public static void main(String[] args) {
	
	new SplashScreen(8000);
	
	// Load the test ontology for the annotation of workflow
	// source and sink ports
	try {
	    URL ontologyURL =
		ClassLoader.getSystemResource("org/embl/ebi/escience/scufl/semantics/mygrid-reasoned-small.rdfs");
	    RDFSParser.loadRDFSDocument(ontologyURL.openStream(), "internal test ontology");
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

	GenericUIComponentFrame diagram = new GenericUIComponentFrame(workbench.model,
								      new ScuflDiagramPanel());
	diagram.setSize(500,500);
	diagram.setLocation(20,440);
	workbench.desktop.add(diagram);
	GenericUIComponentFrame explorer = new GenericUIComponentFrame(workbench.model,
								       new AdvancedModelExplorer());
	explorer.setSize(500,300);
	explorer.setLocation(20,120);
	workbench.desktop.add(explorer);

	GenericUIComponentFrame scavenger = new GenericUIComponentFrame(workbench.model,
									new ScavengerTreePanel());
	scavenger.setSize(300,820);
	scavenger.setLocation(540,120);
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
	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) { }

	Workbench.workbench = this;
	int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 880;
	int height = 1010;
	if (screenSize.width - inset * 2 < width) {
	    width = screenSize.width - inset * 2;
	}
	if (screenSize.height - inset * 2 < height) {
	    height = screenSize.height - inset * 2;
	}
	setBounds(inset, inset, width, height);

	// Initialise the scufl model
	this.model = new ScuflModel();

	//Quit this app when the big window closes.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

	// Create the desktop pane and menu
	if (System.getProperty("taverna.scrollDesktop") == null) {
	    desktop = new JDesktopPane();
	    setContentPane(desktop);
	}
	else {
	    desktop = new ScrollableDesktopPane();
	    setContentPane(new JScrollPane(desktop));
	}
	setJMenuBar(createMenuBar());

	// Put the background image in
	if (background != null) {
	    JLabel bgLabel = new JLabel(background);
	    bgLabel.setBounds(0,0,background.getIconWidth(), background.getIconHeight());
	    desktop.add(bgLabel, new Integer(Integer.MIN_VALUE));
	}

	// Add a filedrop listener to allow users to drag
	// workflow definition in (cheers to Robert Harder!)
	// http://iharder.sourceforge.net/
	new FileDrop(desktop, new FileDrop.Listener() {
		public void filesDropped(File[] filesDropped) {
		    final File[] files = filesDropped;
		    if (files.length == 1) {
			// Don't use a prefix, single drop event
			new Thread() {
			    public void run() {
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
			}.start();
		    }
		    else {
			for (int i = 0; i < files.length; i++) {
			    final int j = i;
			    new Thread() {
				public void run() {
				    try {
					XScuflParser.populate(files[j].toURL().openStream(), Workbench.this.model, "file"+j);
				    }
				    catch (Exception ex) {
					JOptionPane.showMessageDialog(null,
								      "Problem opening XScufl from file : \n"+ex.getMessage(),
								      "Exception!",
								      JOptionPane.ERROR_MESSAGE);
				    }
				}
			    }.start();
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

	JMenu windowMenu = new JMenu("Tools and Workflow Invocation");
	
	// Use the component SPI to discover appropriate components for this menu
	UIComponentRegistry registry = UIComponentRegistry.instance();
	for (Iterator i = registry.getComponents().keySet().iterator(); i.hasNext();) {
	    final String itemName = (String)i.next();
	    try {
		final Class itemClass = Class.forName((String)registry.getComponents().get(itemName));
		final ImageIcon itemIcon = (ImageIcon)registry.getIcons().get(itemName);
		JMenuItem menuItem = null;
		if (itemIcon == null) {
		    menuItem = new JMenuItem(itemName);
		}
		else {
		    menuItem = new JMenuItem(itemName, itemIcon);
		}
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    try {
				ScuflUIComponent thing = (ScuflUIComponent)itemClass.newInstance();
				GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
				frame.setSize(400,400);
				Workbench.this.desktop.add(frame);
				frame.moveToFront();
			    }
			    catch (InstantiationException ie) {
				//
			    }
			    catch (IllegalAccessException iae) {
				//
			    }
			}
		    });
		windowMenu.add(menuItem);
	    }
	    catch (Exception ex) {
		//
	    }
	}

	JMenuItem thingBuilder = new JMenuItem("Run workflow", ScuflIcons.runIcon);
	thingBuilder.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Show a workflow input panel if there are workflow inputs, otherwise
		    // construct a new enactor invocation and run immediately
		    ScuflModel theModel = Workbench.this.model;
		    if (theModel.getWorkflowSourcePorts().length != 0) {
			DataThingConstructionPanel thing = new DataThingConstructionPanel();
			GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
			Workbench.this.desktop.add(frame);
			frame.moveToFront();
		    }
		    else {
			try {
			    // No inputs so launch the enactor directly
			    GenericUIComponentFrame frame = new GenericUIComponentFrame(theModel,
											new EnactorInvocation(new FreefluoEnactorProxy(),
													      theModel,
													      new HashMap()));
			    frame.setSize(600,400);
			    frame.setLocation(100,100);
			    Workbench.this.desktop.add(frame);
			    frame.moveToFront();
			}
			catch (Exception ex) {
			    ex.printStackTrace();
			}
		    }
		}
	    });
	windowMenu.addSeparator();
	windowMenu.add(thingBuilder);

	menuBar.add(windowMenu);
	return menuBar;

    }
    


}
