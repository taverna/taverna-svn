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
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

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
//import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.ScrollableDesktopPane;
import org.embl.ebi.escience.scuflui.workbench.SplashScreen;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scufl.enactor.*;
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

    public static ImageIcon background;

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
	    URL ontologyURL;
	    if (System.getProperty("taverna.ontology.location")!=null) {
		ontologyURL = new URL(System.getProperty("taverna.ontology.location"));
	    }
	    else {
		ontologyURL = ClassLoader.getSystemResource("org/embl/ebi/escience/scufl/semantics/mygrid-reasoned-small.rdfs");
	    }
	    RDFSParser.loadRDFSDocument(ontologyURL.openStream(), "Types");
	}
	catch (Exception ex) {
	    System.out.println("Failed to load ontology data! "+ex.getMessage());
	    ex.printStackTrace();
	}
	
	final Workbench workbench = new Workbench();
	
	// Create a new implementation of the FrameCreator interface to create windows in the desktop
	// Only do this if the property 'taverna.workbench.useinternalframes' is defined
	if (System.getProperty("taverna.workbench.useinternalframes") != null) {
	    UIUtils.DEFAULT_FRAME_CREATOR = new UIUtils.FrameCreator() {
		    public void createFrame(ScuflModel targetModel, ScuflUIComponent targetComponent, int posX, int posY, int sizeX, int sizeY) {
			GenericUIComponentFrame thing = new GenericUIComponentFrame(targetModel, targetComponent);
			thing.setSize(sizeX, sizeY);
			thing.setLocation(posX, posY);
			workbench.desktop.add(thing);
			thing.moveToFront();
			thing.setVisible(true);
		    }
		    class GenericUIComponentFrame extends JInternalFrame { 
			ScuflUIComponent component;
			public GenericUIComponentFrame(ScuflModel model, ScuflUIComponent component) {
			    super(component.getName(), true, true, true, true);
			    this.component = component;
			    JScrollPane pane = new JScrollPane((JComponent)component);
			    if (component.getIcon() != null) {
				setFrameIcon(component.getIcon());
			    }
			    getContentPane().add(pane);
			    // Bind to the specified model
			    component.attachToModel(model);
			    // Unbind on window close
			    addInternalFrameListener(new InternalFrameAdapter() {
				    public void internalFrameClosing(InternalFrameEvent e) {
					GenericUIComponentFrame.this.component.detachFromModel();
				    }
				});
			}
		    };
		};
	}
	else {
	    // If not defined then reset the bounds of the Workbench object so it doesn't
	    // take up so much space.
	    workbench.setBounds(0,0,450,105);
	}
	
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
	
	workbench.setVisible(true);
	workbench.toFront();
	
	UIUtils.createFrame(workbench.model, new ScuflDiagramPanel(), 20, 440, 500, 400);
	UIUtils.createFrame(workbench.model, new AdvancedModelExplorer(), 20, 120, 500, 300);
	UIUtils.createFrame(workbench.model, new ScavengerTreePanel(), 540, 120, 300, 720);
	
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
								  "Problem opening XScufl from file : \n\n"+ex.getMessage()+
								  "\n\nTo load this workflow try setting offline mode, this will allow you to load and remove any defunct operations.",
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
				UIUtils.createFrame(Workbench.this.model, thing, 100, 100, 400, 400);
				//GenericUIComponentFrame frame = new GenericUIComponentFrame(Workbench.this.model, thing);
				//frame.setSize(400,400);
				//Workbench.this.desktop.add(frame);
				//frame.moveToFront();
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
		    final ScuflModel theModel = Workbench.this.model;
		    // Check whether we're in offline mode, if so then just chuck up an error
		    if (theModel.isOffline()) {
			JOptionPane.showMessageDialog(null,
						      "Workflow is currently offline, cannot be invoked.\n"+
						      "Deselect the 'offline' checkbox in the AME to set \n"+
						      "online mode in order to run this workflow.",
						      "Offline, cannot invoke",
						      JOptionPane.ERROR_MESSAGE);
		    }
		    else {
			if (theModel.getWorkflowSourcePorts().length != 0) {
			    DataThingConstructionPanel thing = new DataThingConstructionPanel() {
				    public void launchEnactorDisplay(Map inputObject) {
					try {
					    UIUtils.createFrame(theModel, new EnactorInvocation(FreefluoEnactorProxy.getInstance(), 
												theModel,
												inputObject),
								100, 100, 600, 400);
					}
					catch (WorkflowSubmissionException wse) {
					    JOptionPane.showMessageDialog(null,
									  "Problem invoking workflow engine : \n"+wse.getMessage(),
									  "Exception!",
									  JOptionPane.ERROR_MESSAGE);
					}
				    }
				};
			    UIUtils.createFrame(theModel, thing, 100, 100, 600, 400);
			}
			else {
			    try {
				// No inputs so launch the enactor directly
				UIUtils.createFrame(theModel, new EnactorInvocation(FreefluoEnactorProxy.getInstance(), 
										    theModel,
										    new HashMap()),
						    100, 100, 600, 400);
			    }
			    catch (Exception ex) {
				ex.printStackTrace();
			    }
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
