/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.  It is based on the Taverna
 * Workbench class by Tom Oinn.
 * 
 */
package net.sourceforge.taverna.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.semantics.RDFSParser;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ScavengerTreePanel;
import org.embl.ebi.escience.scuflui.ScuflDiagramPanel;
import org.embl.ebi.escience.scuflui.ScuflModelTreeTableContrib;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.UIComponentRegistry;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflui.graph.WorkflowEditor;
import org.embl.ebi.escience.scuflui.workbench.FileDrop;
import org.embl.ebi.escience.scuflui.workbench.ScrollableDesktopPane;
//import org.embl.ebi.escience.scuflui.workbench.SplashScreen;

/**
 * A workbench application to allow editing and visualization of Scufl
 * workflows
 * 
 * @author Mark
 */
public class Workbench extends JFrame {

    public static ImageIcon background;

    public static AdvancedModelExplorer explorer = null;
    public static ScuflModelTreeTableContrib treeExplorer = null;
    
    //public static ScuflDiagramPanel diagram ;

    public static ScavengerTreePanel treePanel = null;
    static WorkflowEditor editor = null;

    /**
     * If the workbench is created, it will set this to the instance value. This
     * allows ui components that otherwise don't have a handle to the desktop
     * pane to create themselves in internal windows. This especially applies to
     * the workflow run panel
     */
    public static Workbench workbench = null;

    static {
       

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
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

    public static ScuflModel model = new ScuflModel();


    /**
     * This method gets the currently open ScuflModel
     * 
     * @return
     */
    public static ScuflModel getModel() {
        return model;
    }

    /**
     * This method sets the current ScuflModel
     * 
     * @param _model
     * @author Mark
     */
    public static void setModel(ScuflModel _model) {
        model = _model;
        
        //explorer.attachToModel(_model);
        //treeExplorer.attachToModel( _model);
        //diagram.attachToModel(_model);
        
        //editor.attachToModel(_model);
        
    }
    
    /**
     * This method clears the current ScuflModel,
     * and detaches all listening objects.
     * @author Mark
     */
    public static void clearModel(){
        model.clear();
       
    }

    /**
     * Launch the model workbench, shows the default set of UI components in
     * internal frames and waits for the user to load a model from file
     */
    public static void main(String[] args) {

        JXSplash splash = new JXSplash(null, "org/embl/ebi/escience/scuflui/workbench/splashscreen.png",8000);
        

        // Load the test ontology for the annotation of workflow
        // source and sink ports
        try {
            URL ontologyURL;
            if (System.getProperty("taverna.ontology.location") != null) {
                ontologyURL = new URL(System
                        .getProperty("taverna.ontology.location"));
            } else {
                ontologyURL = ClassLoader
                        .getSystemResource("org/embl/ebi/escience/scufl/semantics/mygrid-reasoned-small.rdfs");
            }
            RDFSParser.loadRDFSDocument(ontologyURL.openStream(), "Types");
        } catch (Exception ex) {
            System.out.println("Failed to load ontology data! "
                    + ex.getMessage());
            ex.printStackTrace();
        }

        final Workbench workbench = new Workbench();
        Toolbar toolbar = new Toolbar();
        toolbar.setSize(workbench.getWidth(), 30);
        toolbar.setLocation(0,0);
        workbench.desktop.add(toolbar);

        
        // Create a new implementation of the FrameCreator interface to create
        // windows in the desktop
        // Only do this if the property 'taverna.workbench.useinternalframes' is
        // defined
        if (System.getProperty("taverna.workbench.useinternalframes") != null) {
            UIUtils.DEFAULT_FRAME_CREATOR = new UIUtils.FrameCreator() {
                public void createFrame(ScuflModel targetModel,
                        ScuflUIComponent targetComponent, int posX, int posY,
                        int sizeX, int sizeY) {
                    GenericUIComponentFrame thing = new GenericUIComponentFrame(
                            targetModel, targetComponent);
                    thing.setSize(sizeX, sizeY);
                    thing.setLocation(posX, posY);
                    workbench.desktop.add(thing);
                    thing.moveToFront();
                    thing.setVisible(true);
                }

                class GenericUIComponentFrame extends JInternalFrame {
                    ScuflUIComponent component;

                    public GenericUIComponentFrame(ScuflModel model,
                            ScuflUIComponent component) {
                        super(component.getName(), true, true, true, true);
                        this.component = component;
                        JScrollPane pane = new JScrollPane(
                                (JComponent) component);
                        if (component.getIcon() != null) {
                            setFrameIcon(component.getIcon());
                        }
                        getContentPane().add(pane);
                        
                        // Bind to the specified model
                        component.attachToModel(model);
                        // Unbind on window close
                        addInternalFrameListener(new InternalFrameAdapter() {
                            public void internalFrameClosing(
                                    InternalFrameEvent e) {
                                GenericUIComponentFrame.this.component
                                        .detachFromModel();
                            }
                        });
                    }
                };
            };
        } else {
            // If not defined then reset the bounds of the Workbench object so
            // it doesn't
            // take up so much space.
            workbench.setBounds(0, 0, 450, 105);
        }

        // Treat any command line arguments as files to import into the
        // workbench
        for (int i = 0; i < args.length; i++) {
            try {
                File inputFile = new File(args[i]);
                XScuflParser.populate(inputFile.toURL().openStream(),
                        model, null);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        
        UIUtils.createFrame(model, new WorkflowEditor(),20, 360, 500, 400);
        UIUtils.createFrame(model, new AdvancedModelExplorer(), 20, 40, 500, 300);
        UIUtils.createFrame(model, new ScavengerTreePanel(), 540, 40, 300, 720);
        workbench.setVisible(true);
        workbench.toFront();
        splash.setVisible(false);

    }

    /**
     * Create a new top level application. This contains a menu bar and desktop
     * pane which in turn acts as the container for the views and controllers. A
     * single ScuflModel is shared between all these contained components.
     */
    public Workbench() {
        super("Taverna Scufl Workbench v"
                + org.embl.ebi.escience.scufl.TavernaReleaseInfo.getVersion()
                + ", built "
                + org.embl.ebi.escience.scufl.TavernaReleaseInfo.getBuildDate()
                        .toString());

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
        } else {
            desktop = new ScrollableDesktopPane();
            setContentPane(new JScrollPane(desktop));
        }
        setJMenuBar(new MenuBar());

        // Put the background image in
        if (background != null) {
            JLabel bgLabel = new JLabel(background);
            bgLabel.setBounds(0, 0, background.getIconWidth(), background
                    .getIconHeight());
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
                                XScuflParser.populate(files[0].toURL()
                                        .openStream(), Workbench.this.model,
                                        null);
                            } catch (Exception ex) {
                                JOptionPane
                                        .showMessageDialog(
                                                null,
                                                "Problem opening XScufl from file : \n\n"
                                                        + ex.getMessage()
                                                        + "\n\nTo load this workflow try setting offline mode, this will allow you to load and remove any defunct operations.",
                                                "Exception!",
                                                JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }.start();
                } else {
                    for (int i = 0; i < files.length; i++) {
                        final int j = i;
                        new Thread() {
                            public void run() {
                                try {
                                    XScuflParser.populate(files[j].toURL()
                                            .openStream(),
                                            Workbench.this.model, "file" + j);
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null,
                                            "Problem opening XScufl from file : \n"
                                                    + ex.getMessage(),
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


}