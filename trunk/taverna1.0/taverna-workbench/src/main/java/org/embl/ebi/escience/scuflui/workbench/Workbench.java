package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import net.sf.taverna.perspectives.CustomPerspective;
import net.sf.taverna.perspectives.PerspectiveSPI;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.repository.impl.LocalRepository.ArtifactClassLoader;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.update.ProfileHandler;
import net.sf.taverna.utils.MyGridConfiguration;
import net.sf.taverna.zaria.ZBasePane;
import net.sf.taverna.zaria.ZRavenComponent;
import net.sf.taverna.zaria.ZTreeNode;
import net.sf.taverna.zaria.raven.ArtifactDownloadDialog;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.shared.ModelMap.ModelChangeListener;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet.ScuflModelSetListener;
import org.embl.ebi.escience.scuflui.spi.WorkflowInstanceSetViewSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Top level Zaria based UI for Taverna
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
@SuppressWarnings("serial")
public class Workbench extends JFrame {

	private static Logger logger = Logger.getLogger(Workbench.class);

	private ZBasePane basePane = null;	

	private ScuflModelSet workflowModels = ScuflModelSet.instance();

	private JMenu fileMenu = null;
	
	private WorkbenchPerspectives perspectives = null;

	private Repository repository;
	private ModelMap modelmap = ModelMap.getInstance();

	

	public static Workbench getWorkbench() {
		return new Workbench();
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
		getWorkbench();
	}

	/**
	 * Construct a new Workbench instance with the underlying Raven repository
	 * pointing to the given directory on disc.
	 * 
	 * @param localRepositoryLocation
	 */	
	private Workbench() {
		super();
		MyGridConfiguration.loadMygridProperties();
		try {
			UIManager
					.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		} catch (Exception ex) {
			// Look and feel not available
		}
		setIconImage(TavernaIcons.tavernaIcon.getImage());
		fileMenu = new JMenu("File");

		// Create and configure the ZBasePane
		basePane=new WorkbenchZBasePane();
		try {
			ArtifactClassLoader acl = (ArtifactClassLoader) getClass()
					.getClassLoader();
			repository = acl.getRepository();
			basePane.setRepository(repository);
		} catch (ClassCastException cce) {
		// Running from outside of Raven - won't expect this to work properly!
			repository = LocalRepository.getRepository(new File(
					Bootstrap.TAVERNA_CACHE));
			basePane.setRepository(repository);
			for (URL remoteRepository : Bootstrap.remoteRepositories) {
				repository.addRemoteRepository(remoteRepository);
			}
		}
		TavernaSPIRegistry.setRepository(repository);

		basePane.setKnownSPINames(new String[] {
				"org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI" });				

		setModelChangeListener();
		setModelSetListener();
		setUI();
		// Force a new workflow instance to start off with
		createWorkflowAction().actionPerformed(null);
	}

	private void setModelSetListener() {
		ScuflModelSetListener listener = new ScuflModelSetListener() {

			public void modelAdded(ScuflModel model) {
				modelmap.setModel(ModelMap.CURRENT_WORKFLOW,
						model);
				refreshFileMenu();

			}

			public void modelRemoved(ScuflModel model) {
				modelmap
						.setModel(ModelMap.CURRENT_WORKFLOW, null);
				if (workflowModels.size() > 0) {
					ScuflModel firstmodel = (ScuflModel) workflowModels
							.getModels().toArray()[0];
					modelmap.setModel(ModelMap.CURRENT_WORKFLOW,
							firstmodel);
				}
				refreshFileMenu();
			}

		};
		workflowModels.addListener(listener);
	}	

	public void setUI() {
		JToolBar toolBar = new JToolBar();
		
		perspectives=new WorkbenchPerspectives(basePane,toolBar);
		perspectives.initialisePerspectives();
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(toolBar,BorderLayout.PAGE_START);
		getContentPane().add(basePane, BorderLayout.CENTER);				
		
		JMenuBar menuBar = getWorkbenchMenuBar();

		setJMenuBar(menuBar);		
		setSize(new Dimension(500, 500));
		addWindowListener(getWindowClosingAdaptor());
		updateRepository();

		setWorkbenchTitle();

		readLastPreferences();		
		setVisible(true);

		basePane.setEditable(false);

	}

	private JMenuBar getWorkbenchMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		refreshFileMenu();

		JMenu ravenMenu = new JMenu("Raven");
		menuBar.add(ravenMenu);
		JMenuItem getArtifact = new JMenuItem("Download artifact...");
		ravenMenu.add(getArtifact);
		final String[] groups = new String[] {
				"uk.org.mygrid.taverna.scufl.scufl-ui-components",
				"uk.org.mygrid.taverna.scuflui",
				"uk.org.mygrid.taverna.processors" };
		final String[] versions = new String[] { "1.5-SNAPSHOT" };
		getArtifact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// basePane.lockFrame();
				Artifact a = ArtifactDownloadDialog.showDialog(Workbench.this,
						null, "Download new artifact", "Raven downloader",
						groups, versions);
				// basePane.unlockFrame();
				if (a != null) {
					addArtifact(a);
				}
			}
		});

		if (System.getProperty("raven.remoteprofile") != null) {
			JMenuItem checkUpdates = new JMenuItem("Check for profile updates");
			ravenMenu.add(checkUpdates);

			checkUpdates.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkForProfileUpdate();
				}
			});
		}

		JMenu zariaMenu = new JMenu("Layout");
		menuBar.add(zariaMenu);		

		Action dumpLayoutXMLAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Element element = basePane.getElement();
				XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
				System.out.println(xo.outputString(element));
			}
		};
				
		zariaMenu.add(perspectives.getPerspectivesMenu());
		
		dumpLayoutXMLAction.putValue(Action.NAME, "Dump layout XML to console");
		
		zariaMenu.add(new JMenuItem(dumpLayoutXMLAction));		

		return menuBar;
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
		if (! handler.isNewVersionAvailable()) {
			JOptionPane.showMessageDialog(this,
						"You have all the latest components for this profile",
						"No updates", JOptionPane.INFORMATION_MESSAGE);
			return;
			}
		int retval = JOptionPane.showConfirmDialog(this,
				"New updates are available, update now?",
				"New updates available",
				JOptionPane.YES_NO_OPTION);
		if (retval != JOptionPane.YES_OPTION) {
			return;
		}
		try {
			handler.updateLocalProfile();
		} catch (Exception e) {
			logger.error("Error updating local profile", e);
			JOptionPane.showMessageDialog(this,
					"Updating your profile failed, try again later.",
					"Error updating profile", JOptionPane.WARNING_MESSAGE);
		}
		JOptionPane.showMessageDialog(this,
				"Your updates will be applied when you restart Taverna",
				"Resart required", JOptionPane.INFORMATION_MESSAGE);
	}

	private WindowAdapter getWindowClosingAdaptor() {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					storeUserPrefs();
					PerspectiveSPI currentPerspective = (PerspectiveSPI)modelmap.getNamedModel(ModelMap.CURRENT_PERSPECTIVE);
					if (currentPerspective != null && 
							currentPerspective instanceof CustomPerspective) {
						((CustomPerspective)currentPerspective).update(basePane.getElement());
					}
					perspectives.saveAll();
				} catch (Exception ex) {
					logger.error("Error writing user preferences when closing",
							ex);
				}
				System.exit(0);
			}
		};
	}

	private void readLastPreferences() {
		File userDir = MyGridConfiguration.getUserDir("conf");
		File size = new File(userDir, "preferences.properties");
		if (! size.exists()) {
			return;
		}
			Properties props = new Properties();
			try {
				props.load(size.toURL().openStream());
				String swidth = props.getProperty("width");
				String sheight = props.getProperty("height");
				String sx = props.getProperty("x");
				String sy = props.getProperty("y");

				Dimension resolution = getToolkit().getScreenSize();

				int width = Integer.parseInt(swidth);
				int height = Integer.parseInt(sheight);
				int x = Integer.parseInt(sx);
				int y = Integer.parseInt(sy);

				if (resolution.getWidth() < width) {
					width = (int) resolution.getWidth();
				}

				if (resolution.getHeight() < height) {
					height = (int) resolution.getHeight();
				}

				if (x > (resolution.getWidth() - 50) || x < 0) {
					x = 0;
				}

				if (y > (resolution.getHeight() - 50) || y < 0) {
					y = 0;
				}

				this.setBounds(x, y, width, height);
				this.repaint();

			} catch (Exception e) {
				e.printStackTrace();
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

	private void setModelChangeListener() {
		// FIXME: Also do modelmap.removeModelListener()
		modelmap.addModelListener(new DefaultModelListener());
	}


	/**
	 * Wipe the current contents of the 'file' menu and replace, regenerates the
	 * various model specific actions to ensure that they're acting on the
	 * current model
	 */
	private void refreshFileMenu() {
		fileMenu.removeAll();
		JMenuItem newWorkflow = new JMenuItem(createWorkflowAction());
		fileMenu.add(newWorkflow);

		if (workflowModels.size() > 1) {
			fileMenu.add(new JMenuItem(closeWorkflowAction()));
		}

		if (!workflowModels.isEmpty()) {
			fileMenu.addSeparator();
		}
		for (final ScuflModel model : workflowModels.getModels()) {
			Action selectModel = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					modelmap.setModel(ModelMap.CURRENT_WORKFLOW,
							model);
				}
			};
			selectModel.putValue(Action.SMALL_ICON, 
					TavernaIcons.windowExplorer);
			selectModel.putValue(Action.NAME, 
					model.getDescription().getTitle());
			selectModel.putValue(Action.SHORT_DESCRIPTION, 
					model.getDescription().getTitle());

			if (model == modelmap.getNamedModel(
					ModelMap.CURRENT_WORKFLOW)) {
				selectModel.setEnabled(false);
			}
			fileMenu.add(new JMenuItem(selectModel));
		}

	}

	private Action createWorkflowAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ScuflModel model = new ScuflModel();
				workflowModels.addModel(model);
			}
		};
		a.putValue(Action.NAME, "New workflow");
		return a;
	}

	/**
	 * The action to remove the current workflow from the workbench
	 * 
	 * @return Action
	 */
	private Action closeWorkflowAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ScuflModel currentModel = (ScuflModel) modelmap
						.getNamedModel(ModelMap.CURRENT_WORKFLOW);

				if (currentModel != null) {
					int ret = JOptionPane.showConfirmDialog(Workbench.this,
							"Are you sure you want to close the workflow titled '"
									+ currentModel.getDescription().getTitle()
									+ "'", "Close workflow?",
							JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						workflowModels.removeModel(currentModel);
					}
				}
			}
		};
		a.putValue(Action.NAME, "Close workflow");
		return a;
	}

	public synchronized void addArtifact(Artifact a) {
		basePane.getRepository().addArtifact(a);
		updateRepository();
	}

	public synchronized void updateRepository() {
		basePane.getRepository().update();
	}	
	
	private class DefaultModelListener implements ModelChangeListener {
		private ScuflModelEventListener listener = new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
				// Refresh file menu to reflect any changes to the workflow
				// titles. This isn't terribly efficient but hey.
				refreshFileMenu();
			}
		};

		private ScuflModel currentWorkflowModel = null;

		public synchronized void modelChanged(String modelName,
				Object oldModel, Object newModel) {
			if (logger.isDebugEnabled())
				logger.debug("Model changed:" + modelName + ", "
								+ newModel);
			if (newModel instanceof ScuflModel) {
				ScuflModel newWorkflow = (ScuflModel) newModel;
				for (WorkflowModelViewSPI view : getWorkflowViews()) {
					view.detachFromModel();
					view.attachToModel(newWorkflow);
					if (currentWorkflowModel != null) {
						currentWorkflowModel.removeListener(listener);
					}
					currentWorkflowModel = newWorkflow;
					currentWorkflowModel.addListener(listener);
				}
			}
			if (modelName.equalsIgnoreCase(ModelMap.CURRENT_PERSPECTIVE) && newModel instanceof PerspectiveSPI) {
				if (oldModel instanceof CustomPerspective) {
					((CustomPerspective)oldModel).update(basePane.getElement());
				}
				PerspectiveSPI perspective = (PerspectiveSPI)newModel;
				perspectives.switchPerspective(perspective);
			}
		}

		public synchronized void modelDestroyed(String modelName, Object oldModel) {
			if (logger.isDebugEnabled())
				logger.debug("Model destroyed:" + modelName);
			if (modelName.equals(ModelMap.CURRENT_WORKFLOW)) {
				if (currentWorkflowModel != null) {
					currentWorkflowModel.removeListener(listener);
				}
				currentWorkflowModel = null;
			} else {
				Set<WorkflowInstanceSetViewSPI> views = new HashSet<WorkflowInstanceSetViewSPI>();
				findWorkflowInstanceSetViewSPIPanes(
						basePane.getZChildren(), views);
				for (WorkflowInstanceSetViewSPI view : views) {
					view.removeWorkflowInstance(modelName);
				}
			}		
			if (oldModel instanceof CustomPerspective) {
				perspectives.removeCustomPerspective((CustomPerspective)oldModel);
			}
		}

		public synchronized void modelCreated(String modelName, Object model) {
			if (logger.isDebugEnabled())
				logger.debug("Model created:" + modelName + ", " + model);
			if (model instanceof ScuflModel
					&& modelName.equals(ModelMap.CURRENT_WORKFLOW)) {
				if (currentWorkflowModel != null) {
					currentWorkflowModel.removeListener(listener);
				}
				ScuflModel newWorkflow = (ScuflModel) model;
				newWorkflow.addListener(listener);
				currentWorkflowModel = newWorkflow;
				for (WorkflowModelViewSPI view : getWorkflowViews()) {
					view.detachFromModel();
					view.attachToModel(newWorkflow);
				}
			}
			if (model instanceof WorkflowInstance) {
				Set<WorkflowInstanceSetViewSPI> views = new HashSet<WorkflowInstanceSetViewSPI>();
				findWorkflowInstanceSetViewSPIPanes(
						basePane.getZChildren(), views);
				boolean found = false;
				for (WorkflowInstanceSetViewSPI view : views) {
					view.newWorkflowInstance(modelName,
							(WorkflowInstance) model);
					if (!found) { // only jump to the first view found
						showEnactorTab((Component) view);
						found = true;
					}
				}
			}
			if (modelName.equalsIgnoreCase(ModelMap.CURRENT_PERSPECTIVE) && model instanceof PerspectiveSPI) {
				PerspectiveSPI perspective = (PerspectiveSPI)model;
				perspectives.switchPerspective(perspective);										
			}
		}

		private void showEnactorTab(Component component) {
			if (component.getParent() instanceof JTabbedPane) {
				JTabbedPane pane = (JTabbedPane) component.getParent();
				pane.setSelectedComponent(component);
			}
			if (component.getParent() != null)
				showEnactorTab(component.getParent());
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

		private List<WorkflowModelViewSPI> getWorkflowViews() {
			List<WorkflowModelViewSPI> workflowViews = new ArrayList<WorkflowModelViewSPI>();
			for (ZRavenComponent zc : basePane.getRavenComponents()) {
				JComponent contents = zc.getComponent();
				if (contents instanceof WorkflowModelViewSPI) {
					workflowViews.add((WorkflowModelViewSPI) contents);
				}
			}
			return workflowViews;
		}

		public boolean canHandle(String modelName, Object model) {
			return true;
		}
	}	
}