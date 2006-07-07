/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.semantics.RDFSParser;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.DataThingConstructionPanel;
import org.embl.ebi.escience.scuflui.EnactorInvocation;
import org.embl.ebi.escience.scuflui.ScavengerTreePanel;
import org.embl.ebi.escience.scuflui.ScuflDiagramPanel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.UIComponentRegistry;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;

/**
 * A sample workbench application to allow editing and visualization of Scufl
 * workflows
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class Workbench extends JFrame {

	static {
		// Initialize the proxy settings etc.
		ResourceBundle rb = ResourceBundle.getBundle("mygrid");
		Properties sysProps = System.getProperties();
		Enumeration keys = rb.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) rb.getString(key);
			sysProps.put(key, value);
		}		
		// Set the native look and feel for whatever platform we're on
		try {
			if (System.getProperty("taverna.workbench.themeclass") == null) {
				if (System.getProperty("os.name").equals("Linux") && System.getProperty("java.vm.version").startsWith("1.5"))
				{
					//stops the default theme looking horrible with jdk1.5 under Linux
					UIManager.setLookAndFeel("javax.swing.plaf.synth.SynchLookAndFeel");
				}
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(System
						.getProperty("taverna.workbench.themeclass"));
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Launch the model workbench, shows the default set of UI components in
	 * internal frames and waits for the user to load a model from file
	 */
	public static void main(String[] args) {

		new SplashScreen(8000);

		// Set up the plugin classloader system
		/**
		 * if (System.getProperty("taverna.home") == null) {
		 * System.out.println("Warning, taverna.home not set, will probably be"+ "
		 * unable to locate any of the plugins!."); } else {
		 * PluginManager.init(new File(System.getProperty("taverna.home"))); }
		 */
		// Initialize the UI component registry
		UIComponentRegistry.instance();
		RendererRegistry.instance();

		// Create the workbench and define the authenticator before anything
		// tries to
		// access the network
		Workbench workbench = new Workbench();
		java.net.Authenticator
				.setDefault(new WorkbenchAuthenticator(workbench));

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

		// Treat any command line arguments as files to import into the
		// workbench
		for (int i = 0; i < args.length; i++) {
			try {
				File inputFile = new File(args[i]);
				XScuflParser.populate(inputFile.toURL().openStream(),
						workbench.model, null);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		// Only show the workbench window if we're not on OSX and using external
		// frames...
		if (System.getProperty("taverna.osxpresent") == null
				|| System.getProperty("taverna.workbench.useinternalframes") != null) {
			workbench.setVisible(true);
			workbench.toFront();
		}

		// Show default starting set of windows
		UIUtils.createFrame(workbench.model, new ScuflDiagramPanel(), 20, 440,
				500, 400);
		UIUtils.createFrame(workbench.model, new AdvancedModelExplorer(), 20,
				120, 500, 300);
		UIUtils.createFrame(workbench.model, new ScavengerTreePanel(), 540,
				120, 300, 720);

	}

	/**
	 * Hold a reference to the ScuflModel that this workbench acts on
	 */
	private ScuflModel model;

	/**
	 * Create a new top level application. This contains a menu bar and desktop
	 * pane which in turn acts as the container for the views and controllers. A
	 * single ScuflModel is shared between all these contained components.
	 */
	public Workbench() {
		// Display the build date and version in the top level window
		super("Scufl Workbench v"
				+ org.embl.ebi.escience.scufl.TavernaReleaseInfo.getVersion()
				+ ", built "
				+ org.embl.ebi.escience.scufl.TavernaReleaseInfo.getBuildDate()
						.toString());

		// Create the desktop pane
		JDesktopPane desktop = null;
		if (System.getProperty("taverna.scrollDesktop") == null) {
			desktop = new JDesktopPane();
			setContentPane(desktop);
		} else {
			desktop = new ScrollableDesktopPane();
			setContentPane(new JScrollPane(desktop));
		}

		// Set up the bounds and frame creators based on whether we're running
		// under OS X and whether the internal frame flag is set to be true.
		if (System.getProperty("taverna.workbench.useinternalframes") != null) {
			UIUtils.DEFAULT_FRAME_CREATOR = new InternalFrameCreator(desktop);
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
			setJMenuBar(createMenuBar());
		} else if (System.getProperty("taverna.osxpresent") != null) {
			UIUtils.DEFAULT_FRAME_CREATOR = new OSXFrameCreator();
		} else {
			setBounds(0, 0, 450, 105);
			setJMenuBar(createMenuBar());
		}

		// Initialise the scufl model
		this.model = new ScuflModel();

		// Quit this app when the big window closes.
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		// Put the background image in
		ImageIcon background = new ImageIcon(getClass().getResource(
				"background.png"));
		if (background != null) {
			JLabel bgLabel = new JLabel(background);
			bgLabel.setBounds(0, 0, background.getIconWidth(), background
					.getIconHeight());
			desktop.add(bgLabel, new Integer(Integer.MIN_VALUE));
		}

		// Add a file drop listener
		createFileDrop(desktop);
	}

	/**
	 * Create a filedrop listener for this workbench
	 */
	private void createFileDrop(JDesktopPane desktop) {
		new FileDrop(desktop, new FileDrop.Listener() {
			public void filesDropped(File[] filesDropped) {
				final File[] files = filesDropped;
				for (int i = 0; i < files.length; i++) {
					final int j = i;
					new Thread() {
						public void run() {
							try {
								XScuflParser
										.populate(
												files[j].toURL().openStream(),
												model,
												(files.length == 1) ? null
														: "file" + j);
							} catch (Exception ex) {
								JOptionPane
										.showMessageDialog(null,
												"Problem opening XScufl from file : \n"
														+ ex.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
						}
					}.start();
				}
			}
		});
	}

	/**
	 * Create the menus required by the application
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu windowMenu = new JMenu("Tools and Workflow Invocation");

		// Use the component SPI to discover appropriate components for this
		// menu
		UIComponentRegistry registry = UIComponentRegistry.instance();
		for (Iterator i = registry.getComponents().keySet().iterator(); i
				.hasNext();) {
			final String itemName = (String) i.next();
			try {
				final Class itemClass = Class.forName((String) registry
						.getComponents().get(itemName));
				final ImageIcon itemIcon = (ImageIcon) registry.getIcons().get(
						itemName);
				JMenuItem menuItem = null;
				if (itemIcon == null) {
					menuItem = new JMenuItem(itemName);
				} else {
					menuItem = new JMenuItem(itemName, itemIcon);
				}
				menuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						try {
							ScuflUIComponent thing = (ScuflUIComponent) itemClass
									.newInstance();
							UIUtils.createFrame(Workbench.this.model, thing,
									100, 100, 400, 400);
						} catch (InstantiationException ie) {
							//
						} catch (IllegalAccessException iae) {
							//
						}
					}
				});
				windowMenu.add(menuItem);
			} catch (Exception ex) {
				//
			}
		}

		JMenuItem thingBuilder = new JMenuItem("Run workflow",
				TavernaIcons.runIcon);
		thingBuilder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Show a workflow input panel if there are workflow inputs,
				// otherwise
				// construct a new enactor invocation and run immediately
				final ScuflModel theModel = Workbench.this.model;
				// Check whether we're in offline mode, if so then just chuck up
				// an error
				if (theModel.isOffline()) {
					JOptionPane
							.showMessageDialog(
									null,
									"Workflow is currently offline, cannot be invoked.\n"
											+ "Deselect the 'offline' checkbox in the AME to set \n"
											+ "online mode in order to run this workflow.",
									"Offline, cannot invoke",
									JOptionPane.ERROR_MESSAGE);
				} else {
					if (theModel.getWorkflowSourcePorts().length != 0) {
						DataThingConstructionPanel thing = new DataThingConstructionPanel() {
							public void launchEnactorDisplay(Map inputObject) {
								try {
									UIUtils.createFrame(theModel,
											new EnactorInvocation(
													FreefluoEnactorProxy
															.getInstance(),
													theModel, inputObject),
											100, 100, 600, 400);
								} catch (WorkflowSubmissionException wse) {
									JOptionPane.showMessageDialog(null,
											"Problem invoking workflow engine : \n"
													+ wse.getMessage(),
											"Exception!",
											JOptionPane.ERROR_MESSAGE);
								}
							}
						};
						UIUtils
								.createFrame(theModel, thing, 100, 100, 600,
										400);
					} else {
						try {
							// No inputs so launch the enactor directly
							UIUtils.createFrame(theModel,
									new EnactorInvocation(FreefluoEnactorProxy
											.getInstance(), theModel,
											new HashMap()), 100, 100, 600, 400);
						} catch (Exception ex) {
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

	/**
	 * A frame creator which places components within a JInternalFrame
	 */
	private class InternalFrameCreator implements UIUtils.FrameCreator {
		private JDesktopPane desktop;

		public InternalFrameCreator(JDesktopPane desktop) {
			this.desktop = desktop;
		}

		public void createFrame(ScuflModel targetModel,
				ScuflUIComponent targetComponent, int posX, int posY,
				int sizeX, int sizeY) {
			GenericUIComponentFrame thing = new GenericUIComponentFrame(
					targetModel, targetComponent);
			thing.setSize(sizeX, sizeY);
			thing.setLocation(posX, posY);
			desktop.add(thing);
			thing.moveToFront();
			thing.setVisible(true);
		}

		class GenericUIComponentFrame extends JInternalFrame {
			ScuflUIComponent component;

			public GenericUIComponentFrame(ScuflModel model,
					ScuflUIComponent component) {
				super(component.getName(), true, true, true, true);
				
				getContentPane().setLayout(new BorderLayout());
				this.component = component;
				JScrollPane pane = new JScrollPane((JComponent) component);
				pane.setPreferredSize(new Dimension(0, 0));
				((JComponent) component).revalidate();
				if (component.getIcon() != null) {
					setFrameIcon(component.getIcon());
				}
				getContentPane().add(pane, BorderLayout.CENTER);
				// Bind to the specified model
				component.attachToModel(model);
				// Unbind on window close
				addInternalFrameListener(new InternalFrameAdapter() {
					public void internalFrameClosing(InternalFrameEvent e) {
						GenericUIComponentFrame.this.component
								.detachFromModel();
					}
				});
			}
		}
	}

	/**
	 * A frame creator which adds the top level menu bar to all windows, this
	 * causes the menu to always appear in the top level OSX menu bar as you
	 * would expect for a mac application
	 */
	private class OSXFrameCreator implements UIUtils.FrameCreator {
		public void createFrame(ScuflModel targetModel,
				ScuflUIComponent targetComponent, int posX, int posY,
				int sizeX, int sizeY) {
			final ScuflUIComponent component = targetComponent;
			final ScuflModel model = targetModel;
			JFrame newFrame = new JFrame(component.getName());
			newFrame.setJMenuBar(createMenuBar());
			newFrame.getContentPane().setLayout(new BorderLayout());
			newFrame.getContentPane().add(
					new JScrollPane((JComponent) targetComponent),
					BorderLayout.CENTER);
			newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			newFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					component.detachFromModel();
				}
			});
			if (component.getIcon() != null) {
				newFrame.setIconImage(component.getIcon().getImage());
			}
			component.attachToModel(model);
			newFrame.setSize(sizeX, sizeY);
			newFrame.setLocation(posX, posY);
			newFrame.setVisible(true);
		}
	}

}
