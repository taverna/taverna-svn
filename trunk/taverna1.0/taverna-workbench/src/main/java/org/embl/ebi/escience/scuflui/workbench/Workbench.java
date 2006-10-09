package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.repository.impl.LocalRepository.ArtifactClassLoader;
import net.sf.taverna.zaria.ZBasePane;
import net.sf.taverna.zaria.ZRavenComponent;
import net.sf.taverna.zaria.raven.ArtifactDownloadDialog;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.shared.UIUtils.ModelChangeListener;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

/**
 * Top level Zaria based UI for Taverna
 * @author Tom Oinn
 */
public class Workbench extends JFrame {
	
	private ZBasePane basePane = null;
	private Set<ScuflModel> workflowModels = 
		new HashSet<ScuflModel>();
	private JMenu fileMenu = new JMenu("File");
	
	public static Workbench getWorkbench() {
		return new Workbench();
	}
	
	/**
	 * Do not attempt to run from here - this is a quick hack to make at least
	 * some parts of the workbench run from within Eclipse but it doesn't really
	 * work very well. You need to launch from the Bootstrap class to get the
	 * thing working properly.
	 * @param args
	 */
	public static void main(String[] args) {
		new Workbench();
	}
	
	/**
	 * Construct a new Workbench instance with the underlying Raven
	 * repository pointing to the given directory on disc.
	 * @param localRepositoryLocation
	 */
	private Workbench() {
		super();
		try {
			UIManager.setLookAndFeel(
			"de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		}
		catch (Exception ex) {
			// Look and feel not available
		}
		/**
		 * Create and configure the ZBasePane
		 */
		basePane = new ZBasePane() {
			@Override
			public JMenuItem getMenuItem(Class theClass) {
				try {
					//System.out.println(UIComponentFactorySPI.class.getClassLoader());
					//System.out.println(theClass.getClassLoader());
					UIComponentFactorySPI factory = 
						(UIComponentFactorySPI) theClass.newInstance();
					Icon icon = factory.getIcon();
					if (icon != null) {
						return new JMenuItem(factory.getName(), factory.getIcon());
					}
					else {
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
					return (JComponent)factory.getComponent();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new JPanel();
			}
			@Override
			protected void registerComponent(JComponent comp) {
				if (comp instanceof WorkflowModelViewSPI) {
					ScuflModel model = (ScuflModel)UIUtils.getNamedModel("currentWorkflow");
					if (model != null) {
						((WorkflowModelViewSPI)comp).attachToModel(model);
					}
				}
			}
			@Override
			protected void deregisterComponent(JComponent comp) {
				if (comp instanceof WorkflowModelViewSPI) {
					((WorkflowModelViewSPI)comp).detachFromModel();
				}
			}
			
		};
		try {
			ArtifactClassLoader acl = 
				(ArtifactClassLoader)getClass().getClassLoader();
			basePane.setRepository(acl.getRepository());
		}
		catch (ClassCastException cce) {
			basePane.setRepository(LocalRepository.getRepository(new File("e:/home/tom/taverna")));
			try {
				basePane.getRepository().addRemoteRepository(new URL("http://www.ebi.ac.uk/~tmo/repository/"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				basePane.getRepository().addRemoteRepository(new URL("http://www.ibiblio.org/maven2/"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		basePane.setKnownSPINames(new String[]{
		"org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI"});
		basePane.setEditable(true);
		setUI();
		setModelChangeListener();
		// Force a new workflow instance to start off with
		createWorkflowAction().actionPerformed(null);
	}
	
	public void setUI() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(basePane, BorderLayout.CENTER);
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.add(fileMenu);
		refreshFileMenu();
		
		JMenu ravenMenu = new JMenu("Raven");
		menuBar.add(ravenMenu);
		JMenuItem getArtifact = new JMenuItem("Download artifact...");
		ravenMenu.add(getArtifact);
		final String[] groups = new String[] {
				"uk.org.mygrid.taverna.scufl.scufl-ui-components",
				"uk.org.mygrid.taverna.scuflui"
		};
		final String[] versions = new String[] {
				"1.5-SNAPSHOT"
		};
		getArtifact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				basePane.lockFrame();
				Artifact a = ArtifactDownloadDialog.showDialog(Workbench.this, null, "Download new artifact", "Raven downloader", groups, versions);
				basePane.unlockFrame();
				if (a != null) {
					addArtifact(a);
				}
			}
		});
		JMenu zariaMenu = new JMenu("Layout");
		menuBar.add(zariaMenu);
		zariaMenu.add(new JMenuItem(basePane.getToggleEditAction()));
		
		setJMenuBar(menuBar);
		setSize(new Dimension(500,500));
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		updateRepository();
	}
	
	private void setModelChangeListener() {
		UIUtils.DEFAULT_MODEL_LISTENER = new ModelChangeListener() {

			public void modelChanged(String modelName, Object oldModel, Object newModel) {
				if (newModel instanceof ScuflModel) {
					ScuflModel newWorkflow = (ScuflModel)newModel;
					for (WorkflowModelViewSPI view : getWorkflowViews()) {
						view.detachFromModel();
						view.attachToModel(newWorkflow);
					}
				}
			}

			public void modelDestroyed(String modelName) {
				// No actions at the moment
			}

			public void modelCreated(String modelName, Object model) {
				if (model instanceof ScuflModel && modelName.equals("currentWorkflow")) {
					ScuflModel newWorkflow = (ScuflModel)model;
					for (WorkflowModelViewSPI view : getWorkflowViews()) {
						view.detachFromModel();
						view.attachToModel(newWorkflow);
					}
				}			
			}
			
			private List<WorkflowModelViewSPI> getWorkflowViews() {
				List<WorkflowModelViewSPI> workflowViews = 
					new ArrayList<WorkflowModelViewSPI>();
				for (ZRavenComponent zc : basePane.getRavenComponents()) {
					JComponent contents = zc.getComponent();
					if (contents instanceof WorkflowModelViewSPI) {
						workflowViews.add((WorkflowModelViewSPI)contents);
					}
				}	
				return workflowViews;
			}
			
		};
	}
	
	/**
	 * Wipe the current contents of the 'file' menu and replace, 
	 * regenerates the various model specific actions to ensure that
	 * they're acting on the current model
	 */
	private void refreshFileMenu() {
		fileMenu.removeAll();
		JMenuItem newWorkflow = new JMenuItem(createWorkflowAction());
		fileMenu.add(newWorkflow);
		if (!workflowModels.isEmpty()) {
			fileMenu.addSeparator();
		}
		for (final ScuflModel model : workflowModels) {
			Action selectModel = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					UIUtils.setModel("currentWorkflow",model);					
					refreshFileMenu();
				}
			};
			selectModel.putValue("Action.SMALL_ICON",TavernaIcons.windowExplorer);
			selectModel.putValue("Action.NAME",model.getDescription().getTitle());
			selectModel.putValue("Action.DESCRIPTION",model.getDescription().getTitle());
			if (model == UIUtils.getNamedModel("currentWorkflow")) {
				selectModel.setEnabled(false);
			}
			fileMenu.add(new JMenuItem(selectModel));
		}
		
	}
	
	private Action createWorkflowAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ScuflModel model = new ScuflModel();
				workflowModels.add(model);
				UIUtils.setModel("currentWorkflow",model);
				refreshFileMenu();
			}
		};
		a.putValue(Action.NAME,"New workflow");
		return a;
	}
		
	public synchronized void addArtifact(Artifact a) {
		basePane.getRepository().addArtifact(a);
		updateRepository();
	}
	
	public synchronized void updateRepository() {
		basePane.lockFrame();
		basePane.getRepository().update();
		basePane.unlockFrame();
	}
}