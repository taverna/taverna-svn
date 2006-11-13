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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import net.sf.taverna.perspectives.CustomPerspective;
import net.sf.taverna.perspectives.CustomPerspectiveFactory;
import net.sf.taverna.perspectives.PerspectiveRegistry;
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
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.shared.ModelMap.ModelChangeListener;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet.ScuflModelSetListener;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowInstanceSetViewSPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
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
				switchPerspective(perspective);
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
			if (oldModel instanceof PerspectiveSPI) {
				customPerspectives.remove(oldModel);
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
				switchPerspective(perspective);										
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
	}

	private static Logger logger = Logger.getLogger(Workbench.class);

	private ZBasePane basePane = null;	

	private ScuflModelSet workflowModels = ScuflModelSet.instance();

	private JMenu fileMenu = null;
	private ButtonGroup perspectiveButtons = new ButtonGroup();
	private JToolBar toolBar = null;
	private AbstractButton lastPerspectiveButton = null;
	private JMenu perspectivesMenu = null;
	private Action openPerspectiveAction=null;
	private Action deletePerspectiveAction=null;
	Set<CustomPerspective> customPerspectives = null;

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
	@SuppressWarnings({ "serial", "deprecation" })
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
		basePane = defaultBasePane();
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

	private ZBasePane defaultBasePane() {
		return new ZBasePane() {
			@Override
			public JMenuItem getMenuItem(Class theClass) {
				try {
					UIComponentFactorySPI factory = (UIComponentFactorySPI) theClass
							.newInstance();
					Icon icon = factory.getIcon();
					if (icon != null) {
						return new JMenuItem(factory.getName(), factory
								.getIcon());
					} else {
						return new JMenuItem(factory.getName());
					}
				} catch (InstantiationException e) {
					return new JMenuItem("Instantiation exception!");
				} catch (IllegalAccessException e) {
					return new JMenuItem("Illegal access exception!");
				}
			}

			@Override
			public JComponent getComponent(Class theClass) {
				UIComponentFactorySPI factory;
				try {
					factory = (UIComponentFactorySPI) theClass.newInstance();
					return (JComponent) factory.getComponent();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return new JPanel();
			}

			@Override
			protected void registerComponent(JComponent comp) {
				if (comp instanceof WorkflowModelViewSPI) {
					ScuflModel model = (ScuflModel) modelmap
							.getNamedModel(ModelMap.CURRENT_WORKFLOW);
					if (model != null) {
						((WorkflowModelViewSPI) comp).attachToModel(model);
					}
				}
			}

			@Override
			protected void deregisterComponent(JComponent comp) {
				if (comp instanceof WorkflowModelViewSPI) {
					((WorkflowModelViewSPI) comp).detachFromModel();
				}
			}

		};
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
		toolBar = new JToolBar();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(toolBar,BorderLayout.PAGE_START);
		getContentPane().add(basePane, BorderLayout.CENTER);
				
		JMenuBar menuBar = getWorkbenchMenuBar();
		initialisePerspectives();

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
		
		perspectivesMenu = new JMenu("Perspectives");
		zariaMenu.add(perspectivesMenu);
		
		dumpLayoutXMLAction.putValue(Action.NAME, "Dump layout XML to console");
		
		zariaMenu.add(new JMenuItem(dumpLayoutXMLAction));		

		return menuBar;
	}
	
	private void newPerspective(String name) {
		Element layout=new Element("layout");
		layout.setAttribute("name",name);
		layout.addContent(defaultBasePane().getElement());
		CustomPerspective p = new CustomPerspective(layout);
		customPerspectives.add(p);
		addPerspective(p,true);			
	}
	
	private Action getSavePerspectiveAction() {
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Save perspective");
				chooser.setFileFilter(new ExtensionFileFilter(
						new String[] { "xml" }));
				int retVal = chooser.showSaveDialog(Workbench.this);
				if (retVal != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File file = chooser.getSelectedFile();
				if (file == null) {
					return;
				}
				try {
					PrintWriter out = new PrintWriter(new FileWriter(file));
					Element element = basePane.getElement();
					XMLOutputter xo = new XMLOutputter(Format
							.getPrettyFormat());
					out.print(xo.outputString(element));
					out.flush();
					out.close();							
				} catch (IOException ex) {
					logger.error("IOException saving layout", ex);
					JOptionPane.showMessageDialog(Workbench.this,
							"Error saving layout file: "
							+ ex.getMessage());
				}
			}
		};
		action.putValue(Action.NAME, "Save current");
		action.putValue(Action.SMALL_ICON, TavernaIcons.saveIcon);
		return action;
	}

	private Action getOpenPerspectiveAction() {
		if (openPerspectiveAction==null) {
			openPerspectiveAction=new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("Open Layout");
					chooser.setFileFilter(new ExtensionFileFilter(
							new String[] { "xml" }));
					int retVal = chooser.showOpenDialog(Workbench.this);
					if (retVal == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						if (file != null) {
							try {
								openLayout(file.toURI().toURL().openStream());							
							}  catch(IOException ex) {
								logger.error("Error saving default layout",ex);
							}
						}
					}
				}
			};
			openPerspectiveAction.putValue(Action.NAME, "Load");
			openPerspectiveAction.putValue(Action.SMALL_ICON, TavernaIcons.openIcon);
		}
		return openPerspectiveAction;
	}
	
	private Action getDeleteCurrentPerspectiveAction() {
		if (deletePerspectiveAction==null) {
			deletePerspectiveAction = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					int ret=JOptionPane.showConfirmDialog(Workbench.this, "Are you sure you wish to delete the current perspective","Delete perspective?",JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						PerspectiveSPI p = (PerspectiveSPI)modelmap.getNamedModel(ModelMap.CURRENT_PERSPECTIVE);
						if (p!=null) {
							modelmap.setModel(ModelMap.CURRENT_PERSPECTIVE, null);
							customPerspectives.remove(p);						
							try {
								CustomPerspectiveFactory.getInstance().saveAll(customPerspectives);
								refreshPerspectives();
							} catch (FileNotFoundException e1) {
								logger.error("No file to save custom perspectives",e1);
							} catch (IOException e1) {
								logger.error("Error writing custom perspectives to file",e1);
							}
						}
					}
				}
				
			};
			deletePerspectiveAction.putValue(Action.NAME, "Delete current");
			deletePerspectiveAction.putValue(Action.SMALL_ICON,TavernaIcons.deleteIcon);		
		}
		return deletePerspectiveAction;
	}
	
	/**
	 * Recreates the menu and toolbar buttons. Useful if a perspective has been removed.
	 *
	 */
	private void refreshPerspectives() {
		toolBar.removeAll();
		toolBar.repaint();
		
		perspectivesMenu.removeAll();
		customPerspectives.clear();
		initialisePerspectives();
	}
	
	private void initialisePerspectives() {		
		Action newPerspectiveAction = new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				String name=JOptionPane.showInputDialog(Workbench.this,"New perspective name");
				if (name!=null) {
					newPerspective(name);
				}
			}			
		};
		
		newPerspectiveAction.putValue(Action.NAME, "New ..");
		newPerspectiveAction.putValue(Action.SMALL_ICON,TavernaIcons.newInputIcon);
		perspectivesMenu.add(newPerspectiveAction);
		Action toggleEditAction = basePane.getToggleEditAction();
		toggleEditAction.putValue(Action.SMALL_ICON, TavernaIcons.editIcon);
		perspectivesMenu.add(toggleEditAction);		
		
		perspectivesMenu.add(getOpenPerspectiveAction());
		perspectivesMenu.add(getSavePerspectiveAction());
		perspectivesMenu.add(getDeleteCurrentPerspectiveAction());
		perspectivesMenu.addSeparator();
		
		PerspectiveSPI firstPerspective = null;
		List<PerspectiveSPI> perspectives=PerspectiveRegistry.getInstance().getPerspectives();
		for (final PerspectiveSPI perspective : perspectives) {			
			addPerspective(perspective,false);
			if (firstPerspective==null) {
				firstPerspective=perspective;
			}
		}		
		
		toolBar.addSeparator();
		perspectivesMenu.addSeparator();
		
		try {
			customPerspectives = CustomPerspectiveFactory.getInstance().getAll();
		} catch (IOException e) {
			logger.error("Error reading user perspectives",e);				
		}
		if (customPerspectives!=null && customPerspectives.size()>0) {			
			for (CustomPerspective perspective : customPerspectives) {
				addPerspective(perspective,false);
				if (firstPerspective==null) {
					firstPerspective=perspective;
				}
			}
		}
		//if (firstPerspective!=null) modelmap.setModel(ModelMap.CURRENT_PERSPECTIVE, firstPerspective);
		for (Component c : toolBar.getComponents()) {
			if (c instanceof AbstractButton) { 
				((AbstractButton)c).doClick();
				break;
			}
		}
	}
	
	

	private void addPerspective(final PerspectiveSPI perspective, boolean makeActive) {
		final JToggleButton toolbarButton = new JToggleButton(perspective.getText(),perspective.getButtonIcon());
		toolbarButton.setToolTipText(perspective.getText()+" perspective");		
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (basePane.isEditable()) {					
					JOptionPane.showMessageDialog(Workbench.this, "Sorry, unable to change perspectives whilst in edit mode", "Cannot change perspective",JOptionPane.INFORMATION_MESSAGE);
					//make sure selected button is the previous one.
					if (lastPerspectiveButton!=null) lastPerspectiveButton.setSelected(true);
				}
				else {
					modelmap.setModel(ModelMap.CURRENT_PERSPECTIVE, perspective);
					toolbarButton.setSelected(true); //select the button incase action was invoked via the menu
					lastPerspectiveButton=toolbarButton;
				}
			}				
		};			
		
		action.putValue(Action.NAME, perspective.getText());
		action.putValue(Action.SMALL_ICON,perspective.getButtonIcon());
		
		perspectivesMenu.add(action);						
		toolbarButton.setAction(action);
		toolBar.add(toolbarButton);
		perspectiveButtons.add(toolbarButton);
		if (makeActive) toolbarButton.doClick();
	}
	
	private void openLayout(InputStream layoutStream) {
		try {
			InputStreamReader isr = new InputStreamReader(layoutStream);
			SAXBuilder builder = new SAXBuilder(false);
			Document document = builder.build(isr);
			basePane.configure(document.detachRootElement());			
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Error opening layout file", ex);
			JOptionPane.showMessageDialog(Workbench.this,
					"Error opening layout file: "
							+ ex.getMessage());
		}
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
					CustomPerspectiveFactory.getInstance().saveAll(customPerspectives);
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
		ModelMap.DEFAULT_MODEL_LISTENER = new DefaultModelListener();
	}
	
	private void switchPerspective(PerspectiveSPI perspective) {
		if (perspective instanceof CustomPerspective) { //only allow custom perspectives to be editable.
			basePane.getToggleEditAction().setEnabled(true);
			openPerspectiveAction.setEnabled(true);
			deletePerspectiveAction.setEnabled(true);
		} else {
			basePane.getToggleEditAction().setEnabled(false);
			openPerspectiveAction.setEnabled(false);
			deletePerspectiveAction.setEnabled(false);
		}
		openLayout(perspective.getLayoutInputStream());	
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
}