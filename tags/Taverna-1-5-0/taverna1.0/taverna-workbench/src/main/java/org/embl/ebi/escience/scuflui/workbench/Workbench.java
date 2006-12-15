package org.embl.ebi.escience.scuflui.workbench;

import static java.lang.Math.min;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.sf.taverna.perspectives.CustomPerspective;
import net.sf.taverna.perspectives.PerspectiveSPI;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.update.ProfileHandler;
import net.sf.taverna.update.plugin.PluginManager;
import net.sf.taverna.update.plugin.ui.PluginManagerFrame;
import net.sf.taverna.update.plugin.ui.UpdatesAvailableIcon;
import net.sf.taverna.utils.MyGridConfiguration;
import net.sf.taverna.zaria.ZBasePane;
import net.sf.taverna.zaria.ZRavenComponent;
import net.sf.taverna.zaria.ZTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.actions.OpenWorkflowFromFileAction;
import org.embl.ebi.escience.scuflui.actions.OpenWorkflowFromURLAction;
import org.embl.ebi.escience.scuflui.actions.RunWorkflowAction;
import org.embl.ebi.escience.scuflui.actions.SaveWorkflowAction;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.shared.WorkflowChanges;
import org.embl.ebi.escience.scuflui.shared.ModelMap.ModelChangeListener;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet.ScuflModelSetListener;
import org.embl.ebi.escience.scuflui.spi.WorkflowInstanceSetViewSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * Top level Zaria based UI for Taverna
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 * @author Stian Soiland
 */
@SuppressWarnings("serial")
public class Workbench extends JFrame {

	private static Logger logger = Logger.getLogger(Workbench.class);

	private static Workbench instance = null;

	private ZBasePane basePane = null;

	private ScuflModelSet workflowModels = ScuflModelSet.getInstance();

	private WorkbenchPerspectives perspectives = null;

	private Repository repository;

	private ModelMap modelmap = ModelMap.getInstance();

	private WorkbenchMenuBar menuBar;

	private WorkflowChanges workflowChanges = WorkflowChanges.getInstance();

	/**
	 * Singleton constructor. More than one Workbench is not recommended as it
	 * performs System.exit() when the window is closed.
	 * 
	 * @return Workbench instance
	 */
	public static Workbench getInstance() {
		if (instance == null) {
			instance = new Workbench();
		}
		return instance;
	}

	/**
	 * Do not attempt to run from here - this is a quick hack to make at least
	 * some parts of the workbench run from within Eclipse but it doesn't really
	 * work very well. You need to launch from the Bootstrap class to get the
	 * thing working properly.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		getInstance();
	}

	/**
	 * Construct a new Workbench instance with the underlying Raven repository
	 * pointing to the given directory on disc.
	 * 
	 * @param localRepositoryLocation
	 */
	@SuppressWarnings("deprecation")
	private Workbench() {
		super();
		MyGridConfiguration.loadMygridProperties();
		setLookAndFeel();
		setIconImage(TavernaIcons.tavernaIcon.getImage());

		// Create and configure the ZBasePane
		basePane = new WorkbenchZBasePane();
		try {
			LocalArtifactClassLoader acl = (LocalArtifactClassLoader) getClass()
					.getClassLoader();
			repository = acl.getRepository();
			basePane.setRepository(repository);
		} catch (ClassCastException cce) {
			// Running from outside of Raven - won't expect this to work
			// properly!
			repository = LocalRepository.getRepository(new File(
					Bootstrap.TAVERNA_CACHE));
			basePane.setRepository(repository);
			for (URL remoteRepository : Bootstrap.remoteRepositories) {
				repository.addRemoteRepository(remoteRepository);
			}
		}		
		
		PluginManager.setRepository(repository);
		PluginManager.getInstance();
		TavernaSPIRegistry.setRepository(repository);

		basePane
				.setKnownSPINames(new String[] { "org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI" });
		setUI();
		setModelChangeListeners();
		setModelSetListener();
		setVisible(true);
				
		// Force a new workflow instance to start off with
		createWorkflow();
				
	}

	private void setLookAndFeel() {
		String landf = MyGridConfiguration
				.getProperty("taverna.workbench.themeclass");
		boolean set = false;

		if (landf != null) {
			try {
				UIManager.setLookAndFeel(landf);
				logger.info("Using " + landf + " Look and Feel");
				set = true;
			} catch (Exception ex) {
				logger.error(
						"Error using theme defined by taverna.workbench.themeclass as "
								+ landf, ex);
			}
		}

		if (!set) {
			try {
				UIManager
						.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
				logger.info("Using Synthetica Look and Feel");
			} catch (Exception ex) {
				try {
					if (!(System.getProperty("os.name").equals("Linux"))) {
						UIManager.setLookAndFeel(UIManager
								.getSystemLookAndFeelClassName());
						logger.info("Using "
								+ UIManager.getSystemLookAndFeelClassName()
								+ " Look and Feel");
					} else {
						logger.info("Using default Look and Feel");
					}
				} catch (Exception ex2) {
					ex2.printStackTrace();
				}

			}
		}
	}

	public ScuflModelSet getWorkflowModels() {
		return workflowModels;
	}

	private void setModelChangeListeners() {
		
		modelmap.addModelListener(new DebugListener());
		modelmap.addModelListener(new CurrentWorkflowListener());
		modelmap.addModelListener(new WorkflowInstanceListener());
	}

	private void setModelSetListener() {
		ScuflModelSetListener listener = new ScuflModelSetListener() {
			public void modelAdded(ScuflModel model) {
				modelmap.setModel(ModelMap.CURRENT_WORKFLOW, model);
				menuBar.refreshWorkflowsMenu();
			}

			public void modelRemoved(ScuflModel model) {
				if (model == modelmap.getNamedModel(ModelMap.CURRENT_WORKFLOW)) {
					// Need to find some other current workflow
					modelmap.setModel(ModelMap.CURRENT_WORKFLOW, null);
					try {
						ScuflModel firstmodel = workflowModels.getModels()
								.iterator().next();
						modelmap
								.setModel(ModelMap.CURRENT_WORKFLOW, firstmodel);
					} catch (NoSuchElementException ex) {
						// No more models, make a new empty one
						createWorkflow();
						return;
					}
				}
				menuBar.refreshWorkflowsMenu();
			}
		};
		workflowModels.addListener(listener);
	}

	public void setUI() {
		JToolBar toolBar = new JToolBar();
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new BorderLayout());	
		toolBar.setFloatable(false);
		
		toolBarPanel.add(toolBar,BorderLayout.WEST);
		toolBarPanel.add(new UpdatesAvailableIcon(),BorderLayout.EAST);

		perspectives = new WorkbenchPerspectives(basePane, toolBar);
		modelmap.addModelListener(perspectives.getModelChangeListener());
		perspectives.initialisePerspectives();

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(toolBarPanel, BorderLayout.PAGE_START);
		getContentPane().add(basePane, BorderLayout.CENTER);

		menuBar = new WorkbenchMenuBar();
		setJMenuBar(menuBar);

		// set default size to 3/4 of width and height
		Dimension screen = getToolkit().getScreenSize();
		setSize((int) (screen.getWidth() * 0.75),
				(int) (screen.getHeight() * 0.75));

		// Handle closing ourself
		addWindowListener(getWindowClosingAdaptor());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		updateRepository();

		setWorkbenchTitle();

		readLastPreferences();

		basePane.setEditable(false);
	}

	/**
	 * set the title to the profile name and version, otherwise just Taverna
	 * Workbench
	 */
	private void setWorkbenchTitle() {
		String title = "Taverna Workbench";
		Profile prof = ProfileFactory.getInstance().getProfile();
		if (prof != null) {
			if (prof.getName() != null) {
				title = prof.getName();
			}
			title += " v" + prof.getVersion();
		}
		setTitle(title);
	}

	private void checkForProfileUpdate() {
		String remoteProfileURL = System.getProperty("raven.remoteprofile");
		ProfileHandler handler;
		try {
			handler = new ProfileHandler(remoteProfileURL);
		} catch (Exception e) {
			logger.error("Error checking for new profile", e);
			JOptionPane.showMessageDialog(this,
					"Currently unable to check for updates, try again later.",
					"Error checking for update", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!handler.isNewVersionAvailable()) {
			JOptionPane.showMessageDialog(this,
					"You have all the latest components for this profile",
					"No updates", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		int confirmation = JOptionPane.showConfirmDialog(this,
				"New updates are available, update now?",
				"New updates available", JOptionPane.YES_NO_OPTION);
		if (confirmation != JOptionPane.YES_OPTION) {
			return;
		}
		try {
			handler.updateLocalProfile();
			JOptionPane.showMessageDialog(this,
					"Your updates will be applied when you restart Taverna",
					"Restart required", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			logger.error("Error updating local profile", e);
			JOptionPane.showMessageDialog(this,
					"Updating your profile failed, try again later.",
					"Error updating profile", JOptionPane.WARNING_MESSAGE);
		}		
	}

	/**
	 * Exit Taverna workbench.
	 * <p>
	 * Save all changed, open workflows (will pop-up "Do you want to save?" if
	 * changed), save preferences and perspectives, and exit application.
	 * 
	 */
	public void exit() {
		// Save changed models if desired.
		// Note that we don't bother with closing the models here, we'll leave
		// that to System.exit. (And that also avoids the destructive
		// behavoure of closing all non-changed models if the user does
		// "Cancel")
		for (ScuflModel model : workflowModels.getModels()) {
			// Pop-up a warning if the model has not been saved
			if (!safeToClose(model)) {
				logger.info("Aborted exit() due to non-safe-to-close " + model);
				return;
			}
		}

		try {
			storeUserPrefs();
			PerspectiveSPI currentPerspective = (PerspectiveSPI) modelmap
					.getNamedModel(ModelMap.CURRENT_PERSPECTIVE);
			if (currentPerspective != null
					&& currentPerspective instanceof CustomPerspective) {
				((CustomPerspective) currentPerspective).update(basePane
						.getElement());
			}
			perspectives.saveAll();
		} catch (Exception ex) {
			logger.error("Error writing user preferences when closing", ex);
		}
		System.exit(0);
	}

	private WindowAdapter getWindowClosingAdaptor() {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		};
	}

	private void readLastPreferences() {
		File userDir = MyGridConfiguration.getUserDir("conf");
		File size = new File(userDir, "preferences.properties");
		if (!size.exists()) {
			return;
		}
		Properties props = new Properties();
		try {
			props.load(size.toURI().toURL().openStream());
			String swidth = props.getProperty("width");
			String sheight = props.getProperty("height");
			String sx = props.getProperty("x");
			String sy = props.getProperty("y");

			Dimension resolution = getToolkit().getScreenSize();

			int width = Integer.parseInt(swidth);
			int height = Integer.parseInt(sheight);
			int x = Integer.parseInt(sx);
			int y = Integer.parseInt(sy);

			// Make sure our window is not too big
			width = min((int) resolution.getWidth(), width);
			height = min((int) resolution.getHeight(), height);

			// Move to upper left corner if we are too far off
			if (x > (resolution.getWidth() - 50) || x < 0) {
				x = 0;
			}
			if (y > (resolution.getHeight() - 50) || y < 0) {
				y = 0;
			}

			this.setBounds(x, y, width, height);
			this.repaint();

		} catch (Exception e) {
			logger.error("Error loading default window dimensions", e);
		}
	}

	private void storeUserPrefs() throws IOException {
		File userDir = MyGridConfiguration.getUserDir("conf");

		// store current window size
		File size = new File(userDir, "preferences.properties");
		Writer writer = new BufferedWriter(new FileWriter(size));
		writer.write("width=" + this.getWidth() + "\n");
		writer.write("height=" + this.getHeight() + "\n");
		writer.write("x=" + this.getX() + "\n");
		writer.write("y=" + this.getY() + "\n");
		writer.flush();
		writer.close();
	}

	public class ExitAction extends AbstractAction {
		public ExitAction() {
			super();
			putValue(NAME, "Exit");
			putValue(SHORT_DESCRIPTION, "Quit the Taverna workbench");
		}

		public void actionPerformed(ActionEvent e) {
			exit();
		}
	}

	private void createWorkflow() {
		ScuflModel model = new ScuflModel();
		workflowModels.addModel(model);
	}

	private Action createWorkflowAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				createWorkflow();
			}
		};
		a.putValue(Action.NAME, "New workflow");
		a.putValue(Action.SMALL_ICON, TavernaIcons.newIcon);
		a.putValue(Action.SHORT_DESCRIPTION, "Create a new workflow");
		return a;
	}

	/**
	 * Close the current workflow.
	 * 
	 * @return true if the workflow was closed, false if anything went wrong or
	 *         the operation was cancelled.
	 */
	public boolean closeWorkflow() {
		ScuflModel currentModel = (ScuflModel) modelmap
				.getNamedModel(ModelMap.CURRENT_WORKFLOW);
		if (currentModel == null) {
			return false;
		}
		return closeWorkflow(currentModel);
	}

	/**
	 * Close the given workflow.
	 * 
	 * @param model
	 *            Workflow to close
	 * @return true if the workflow was closed, false if anything went wrong or
	 *         the operation was cancelled.
	 */
	public boolean closeWorkflow(ScuflModel model) {
		if (safeToClose(model)) {
			workflowModels.removeModel(model);
			return true;
		}
		return false;
	}

	/**
	 * Check if it's safe to close the given model. Pop's up a "Do you want to
	 * save?" box if the model has been changed.
	 * <p>
	 * Does not actually close the model, either use closeWorkflow(model.) or
	 * workflowModels.removeModel(model)
	 * 
	 * @param model
	 *            Model that is to be checked
	 * @return if the model has not been changed, the user saved the model, or
	 *         the user said "No" to saving it. Returns false if the user
	 *         cancelled.
	 */
	public boolean safeToClose(ScuflModel model) {
		if (!workflowChanges.hasChanged(model)) {
			return true;
		}
		// Make sure it is visible first so the user knows what he could save
		modelmap.setModel(ModelMap.CURRENT_WORKFLOW, model);

		String msg = "Do you want to save changes before closing workflow "
				+ model.getDescription().getTitle() + "?";
		int ret = JOptionPane.showConfirmDialog(Workbench.this, msg,
				"Save workflow?", JOptionPane.YES_NO_CANCEL_OPTION);
		if (ret == JOptionPane.CANCEL_OPTION) {
			return false;
		}
		if (ret == JOptionPane.NO_OPTION) {
			return true;
		}
		// ret == JOptionPane.YES_OPTION
		try {
			return new SaveWorkflowAction(Workbench.this).saveToFile(model);
		} catch (Exception ex) {
			logger.warn("Could not save file for " + model, ex);
			return false;
		}
	}

	/**
	 * The action to remove the current workflow from the workbench
	 * 
	 * @return Action
	 */
	private Action closeWorkflowAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				closeWorkflow();
			}
		};
		a.putValue(Action.NAME, "Close workflow");
		a.putValue(Action.SHORT_DESCRIPTION, "Close the current workflow");
		a.putValue(Action.SMALL_ICON, TavernaIcons.deleteIcon);
		return a;
	}

	public synchronized void addArtifact(Artifact a) {
		repository.addArtifact(a);
		updateRepository();
	}

	public synchronized void updateRepository() {
		repository.update();
	}

	// FIXME: Allow any SPI, not just WorkflowModelViewSPI
	public List<WorkflowModelViewSPI> getWorkflowViews() {
		List<WorkflowModelViewSPI> workflowViews = new ArrayList<WorkflowModelViewSPI>();
		for (ZRavenComponent zc : basePane.getRavenComponents()) {
			JComponent contents = zc.getComponent();
			if (contents instanceof WorkflowModelViewSPI) {
				workflowViews.add((WorkflowModelViewSPI) contents);
			}
		}
		return workflowViews;
	}

	/**
	 * Menu bar for the workbench.
	 * <p>
	 * Call refreshWorkflowsMenu() to update the "Workflows" menu with the
	 * current workflow models.
	 * 
	 * @author Stian Soiland
	 * @author Stuart Owen
	 * 
	 */
	public class WorkbenchMenuBar extends JMenuBar {
		
		private final AboutBox aboutBox=new AboutBox();

		private JMenu file = makeFile();
		
		private JMenu tools = makeTools();

		private JMenu advanced = makeAdvanced();

		private JMenu workflows = makeWorkflows();
		
		private JMenu help = makeHelp();

		private boolean refreshingWorkflowsmenu = false;

		// Only refresh every 0.3s at maximum
		public final int MAX_REFRESH = 300;

		public WorkbenchMenuBar() {
			add(file);
			add(tools);			
			add(workflows);
			add(advanced);
			add(Box.createHorizontalGlue());
			add(help);
		}

		/**
		 * Update the list of open workflows. A delay of MAX_REFRESH will be
		 * enforced to avoid excessive menu updates.
		 * 
		 */
		public synchronized void refreshWorkflowsMenu() {
			if (refreshingWorkflowsmenu) {
				return;
			}
			refreshingWorkflowsmenu = true;
			new Thread() {
				public void run() {
					try {
						try {
							Thread.sleep(MAX_REFRESH);
						} catch (InterruptedException e) {
						}
						int index = getComponentIndex(workflows);
						workflows = makeWorkflows();
						remove(index);
						add(workflows,index);
						revalidate();
					} finally {
						refreshingWorkflowsmenu = false;
					}
				}
			}.start();
		}

		private JMenu makeFile() {
			JMenu menu = new JMenu("File");
			JMenuItem newWorkflow = new JMenuItem(createWorkflowAction());
			menu.add(newWorkflow);

			menu.add(new JMenuItem(new OpenWorkflowFromFileAction(this)));
			menu.add(new JMenuItem(new OpenWorkflowFromURLAction(this)));
			menu.addSeparator();
			menu.add(new JMenuItem(closeWorkflowAction()));

			menu.addSeparator();
			menu.add(new JMenuItem(new SaveWorkflowAction(this)));

			menu.addSeparator();
			menu.add(new JMenuItem(new RunWorkflowAction(this)));

			menu.addSeparator();
			menu.add(new JMenuItem(new ExitAction()));
			return menu;
		}

		private JMenu makeTools() {
			JMenu menu = new JMenu("Tools");

			menu.add(new JMenuItem(new AbstractAction("Plugin Manager") {
				
				public void actionPerformed(ActionEvent e) {					
					PluginManagerFrame pluginManagerUI = new PluginManagerFrame(PluginManager.getInstance());
					pluginManagerUI.setLocationRelativeTo(Workbench.this);
					pluginManagerUI.setVisible(true);
				}
				
			}));
			
			if (System.getProperty("raven.remoteprofile") != null) {
				JMenuItem checkUpdates = new JMenuItem(
						"Check for core Taverna updates");
				menu.add(checkUpdates);

				checkUpdates.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						checkForProfileUpdate();
					}
				});
			}
			
			return menu;
		}

		private JMenu makeAdvanced() {
			JMenu advancedMenu = new JMenu("Advanced");
			advancedMenu.add(perspectives.getEditPerspectivesMenu());			
			return advancedMenu;
		}

		private JMenu makeWorkflows() {
			JMenu menu = new JMenu("Workflows");
			for (final ScuflModel model : workflowModels.getModels()) {
				Action selectModel = new AbstractAction() {
					public void actionPerformed(ActionEvent e) {
						modelmap.setModel(ModelMap.CURRENT_WORKFLOW, model);
					}
				};
				selectModel.putValue(Action.SMALL_ICON,
						TavernaIcons.windowExplorer);
				String title = model.getDescription().getTitle();
				if (workflowChanges.hasChanged(model)) {
					// FIXME: * is not removed on Save (because the save action
					// can't
					// call refreshWorkflowsMenu() )
					title = "*" + title;
				}
				selectModel.putValue(Action.NAME, title);
				selectModel.putValue(Action.SHORT_DESCRIPTION, title);
				// TODO: Tag the currently selected workflow instead of disabling it
				if (model == modelmap.getNamedModel(ModelMap.CURRENT_WORKFLOW)) {
					selectModel.setEnabled(false);
				}
				menu.add(new JMenuItem(selectModel));
			}

			return menu;
		}
		
		private JMenu makeHelp() {
			JMenu result = new JMenu("Help");
			JMenuItem about = new JMenuItem(new AbstractAction("About") {
				public void actionPerformed(ActionEvent e) {
					aboutBox.setLocationRelativeTo(Workbench.this);
					aboutBox.setVisible(true);
				}				
			});
			result.add(about);
			return result;
		}
	}

	/**
	 * Print debug messages of model changes.
	 * 
	 * @author Stian Soiland
	 * 
	 */
	public class DebugListener implements ModelChangeListener {
		public boolean canHandle(String modelName, Object model) {
			return logger.isDebugEnabled();
		}

		public void modelCreated(String modelName, Object model) {
			logger.debug("Model created:" + modelName + ", " + model);
		}

		public void modelChanged(String modelName, Object oldModel,
				Object newModel) {
			logger.debug("Model changed:" + modelName + ", " + oldModel + "-->"
					+ newModel);
		}

		public void modelDestroyed(String modelName, Object oldModel) {
			logger.debug("Model destroyed:" + modelName + ", " + oldModel);
		}
	}

	/**
	 * Notify WorkflowModelViewSPI instances that ModelMap.CURRENT_WORKFLOW have
	 * changed, by calling detachFromModel() and attachToModel(). In addition, a
	 * small ScuflModelEventListener is added to to the workflow that refreshes
	 * the file menu.
	 * 
	 * @author Stian Soiland
	 * 
	 */
	public class CurrentWorkflowListener implements ModelChangeListener {

		private ScuflModelEventListener listener = new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
				// Refresh file menu to reflect any changes to the workflow
				// titles. This isn't terribly efficient but hey.
				menuBar.refreshWorkflowsMenu();
			}
		};

		public boolean canHandle(String modelName, Object model) {
			if (!modelName.equals(ModelMap.CURRENT_WORKFLOW)) {
				return false;
			}
			if (!(model instanceof ScuflModel)) {
				logger.error(ModelMap.CURRENT_WORKFLOW
						+ " is not an ScuflModel instance");
				return false;
			}
			return true;
		}

		void setWorkflow(ScuflModel workflow) {
			for (WorkflowModelViewSPI view : getWorkflowViews()) {
				view.detachFromModel();
				if (workflow != null) {
					view.attachToModel(workflow);
				}
			}
			menuBar.refreshWorkflowsMenu();
		}

		public void modelCreated(String modelName, Object model) {
			ScuflModel workflow = (ScuflModel) model;
			workflow.addListener(listener);
			setWorkflow(workflow);
		}

		public void modelChanged(String modelName, Object oldModel,
				Object newModel) {
			ScuflModel oldWorkflow = (ScuflModel) oldModel;
			ScuflModel newWorkflow = (ScuflModel) newModel;
			oldWorkflow.removeListener(listener);
			newWorkflow.addListener(listener);
			setWorkflow(newWorkflow);
		}

		public void modelDestroyed(String modelName, Object oldModel) {
			setWorkflow(null);
			ScuflModel oldWorkflow = (ScuflModel) oldModel;
			oldWorkflow.removeListener(listener);
		}
	}

	/**
	 * Keep WorkflowInstanceSetViewSPI notified about creation and destruction
	 * about workflow instances. The WorkflowInstanceSetViewSPIs will create
	 * tabs or similar showing the workflow run progression.
	 * 
	 * @author Stian Soiland
	 * 
	 */
	public class WorkflowInstanceListener implements ModelChangeListener {

		public boolean canHandle(String modelName, Object model) {
			return model instanceof WorkflowInstance;
		}

		/**
		 * New WorkflowInstance created, create workflow instance in each
		 * WorkflowInstanceSetViewSPI. Highlights the first view by selecting
		 * its tabs.
		 */
		public void modelCreated(String modelName, Object model) {
			Set<WorkflowInstanceSetViewSPI> views = findWorkflowInstanceSetViewSPIPanes(basePane
					.getZChildren());
			boolean found = false;
			for (WorkflowInstanceSetViewSPI view : views) {
				logger.info("Notified " + view);
				view.newWorkflowInstance(modelName, (WorkflowInstance) model);
				if (!found && view instanceof Component) {
					// only jump to the first view found
					showEnactorTab((Component) view);
					found = true;
				}
			}
		}

		/**
		 * Should normally not happen with WF instances
		 */
		public void modelChanged(String modelName, Object oldModel,
				Object newModel) {
			logger.warn("modelChanged() unexpected for WorkflowInstance "
					+ modelName);
		}

		/**
		 * Remove workflow instance from each WorkflowInstanceSetViewSPI.
		 */
		public synchronized void modelDestroyed(String modelName,
				Object oldModel) {
			Set<WorkflowInstanceSetViewSPI> views = findWorkflowInstanceSetViewSPIPanes(basePane
					.getZChildren());
			for (WorkflowInstanceSetViewSPI view : views) {
				view.removeWorkflowInstance(modelName);
			}
		}

		/**
		 * Climb the hierarchy of components and select all tabs leading to the
		 * given component.
		 * 
		 * @param component
		 *            Component that should be showed.
		 */
		private void showEnactorTab(Component component) {
			Container parent = component.getParent();
			if (parent == null) {
				return;
			}
			if (parent instanceof JTabbedPane) {
				JTabbedPane pane = (JTabbedPane) parent;
				pane.setSelectedComponent(component);
			}
			showEnactorTab(parent); // recurse
		}

		// TODO: Generalise to work for any SPI/class, not just
		// WorkflowInstanceSetViewSPI
		private Set<WorkflowInstanceSetViewSPI> findWorkflowInstanceSetViewSPIPanes(
				List<ZTreeNode> children) {
			Set<WorkflowInstanceSetViewSPI> set = new HashSet<WorkflowInstanceSetViewSPI>();
			findWorkflowInstanceSetViewSPIPanes(children, set);
			return set;
		}

		private void findWorkflowInstanceSetViewSPIPanes(
				List<ZTreeNode> children,
				Set<WorkflowInstanceSetViewSPI> results) {
			for (ZTreeNode child : children) {
				if (child instanceof ZRavenComponent) {
					ZRavenComponent raven = (ZRavenComponent) child;
					if (raven.getComponent() instanceof WorkflowInstanceSetViewSPI) {
						results.add((WorkflowInstanceSetViewSPI) raven
								.getComponent());
					}
				}
				findWorkflowInstanceSetViewSPIPanes(child.getZChildren(),
						results);
			}
		}
	}
}