package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.repository.impl.LocalRepository.ArtifactClassLoader;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;
import net.sf.taverna.tools.Bootstrap;
import net.sf.taverna.update.ProfileHandler;
import net.sf.taverna.utils.MyGridConfiguration;
import net.sf.taverna.zaria.ZBasePane;
import net.sf.taverna.zaria.ZRavenComponent;
import net.sf.taverna.zaria.raven.ArtifactDownloadDialog;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.shared.ModelMap.ModelChangeListener;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet.ScuflModelSetListener;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Top level Zaria based UI for Taverna
 * @author Tom Oinn
 * @author Stuart Owen
 */
@SuppressWarnings("serial")
public class Workbench extends JFrame {
	
	private static Logger logger = Logger.getLogger(Workbench.class);
	
	private ZBasePane basePane = null;
	private ScuflModelSet workflowModels=ScuflModelSet.instance();
	private JMenu fileMenu = null;
	
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
	@SuppressWarnings("serial")
	private Workbench() {
		super();
		try {
			UIManager.setLookAndFeel(
			"de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		}
		catch (Exception ex) {
			// Look and feel not available
		}
		fileMenu = new JMenu("File");
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
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return new JPanel();
			}
			@Override
			protected void registerComponent(JComponent comp) {
				if (comp instanceof WorkflowModelViewSPI) {
					ScuflModel model = (ScuflModel)ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_WORKFLOW);
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
		
		// Running from outside of Raven - won't expect this to work properly!
		catch (ClassCastException cce) {
			basePane.setRepository(LocalRepository.getRepository(new File(Bootstrap.TAVERNA_CACHE)));
			for (URL repository : Bootstrap.remoteRepositories) {
				basePane.getRepository().addRemoteRepository(repository);
			}
		}			
		
		TavernaSPIRegistry.setRepository(basePane.getRepository());
		
		basePane.setKnownSPINames(new String[]{
		"org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI"});
		basePane.setEditable(true);
		setUI();
		setModelChangeListener();
		setModelSetListener();
		// Force a new workflow instance to start off with
		createWorkflowAction().actionPerformed(null);
	}
	
	private void setModelSetListener() {
		ScuflModelSetListener listener=new ScuflModelSetListener() {

			public void modelAdded(ScuflModel model) {
				ModelMap.getInstance().setModel(ModelMap.CURRENT_WORKFLOW, model);
				refreshFileMenu();
				
			}

			public void modelRemoved(ScuflModel model) {
				ModelMap.getInstance().setModel(ModelMap.CURRENT_WORKFLOW, null);
				if (workflowModels.size()>0) {
					ScuflModel firstmodel=(ScuflModel)workflowModels.getModels().toArray()[0];
					ModelMap.getInstance().setModel(ModelMap.CURRENT_WORKFLOW, firstmodel);
				}
				refreshFileMenu();				
			}
			
		};
		workflowModels.addListener(listener);
	}
	
	private void saveDefaultLayout() throws IOException {		
		File userDir = MyGridConfiguration.getUserDir("conf");
		File layout=new File(userDir,"layout.xml");
		Writer writer=new BufferedWriter(new FileWriter(layout));
		Element element = basePane.getElement();
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());		
		writer.write(xo.outputString(element));
		writer.flush();
		writer.close();
	}
	
	@SuppressWarnings("serial")
	public void setUI() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(basePane, BorderLayout.CENTER);
		
		
		JMenuBar menuBar = getWorkbenchMenuBar();
				
		setJMenuBar(menuBar);
		setSize(new Dimension(500,500));				
		addWindowListener(getWindowClosingAdaptor());
		updateRepository();
		
		setWorkbenchTitle();
				
		readLastPreferences();		
		readLastLayout();		
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
				"uk.org.mygrid.taverna.processors"
		};
		final String[] versions = new String[] {
				"1.5-SNAPSHOT"
		};
		getArtifact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//basePane.lockFrame();
				Artifact a = ArtifactDownloadDialog.showDialog(Workbench.this, null, "Download new artifact", "Raven downloader", groups, versions);
				//basePane.unlockFrame();
				if (a != null) {
					addArtifact(a);
				}
			}
		});
		
		if (System.getProperty("raven.remoteprofile")!=null) {
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
		zariaMenu.add(new JMenuItem(basePane.getToggleEditAction()));
		
		Action dumpLayoutXMLAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				Element element = basePane.getElement();
				XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
				System.out.println(xo.outputString(element));
			}
		};
		
		dumpLayoutXMLAction.putValue(Action.NAME,"Dump layout XML to console");
		
		Action saveLayoutXMLAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser=new JFileChooser();
				chooser.setDialogTitle("Save Layout");
				chooser.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
				int retVal=chooser.showSaveDialog(Workbench.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {					
					File file=chooser.getSelectedFile();					
					if (file!=null) {
						PrintWriter out;
						try {
							out = new PrintWriter(new FileWriter(file));
							Element element = basePane.getElement();
							XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
							out.print(xo.outputString(element));
							out.flush();
							out.close();
							
							saveDefaultLayout();
						} catch (IOException ex) {							
							logger.error("IOException saving layout",ex);
							JOptionPane.showMessageDialog(Workbench.this,"Error saving layout file: "+ex.getMessage());
						}
						
					}
				}
			}			
		};
		
		saveLayoutXMLAction.putValue(Action.NAME, "Save layout XML");			
		
		Action loadLayoutXMLAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {				
				JFileChooser chooser=new JFileChooser();
				chooser.setDialogTitle("Open Layout");
				chooser.setFileFilter(new ExtensionFileFilter(new String[] { "xml" }));
				int retVal=chooser.showOpenDialog(Workbench.this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
					File file=chooser.getSelectedFile();
					if (file!=null) {
						try {
							InputStreamReader isr = new InputStreamReader(file.toURL().openStream());
							SAXBuilder builder = new SAXBuilder(false);
							Document document = builder.build(isr);
							basePane.configure(document.detachRootElement());							
							saveDefaultLayout();
						}
						catch(Exception ex) {
							ex.printStackTrace();
							logger.error("Error opening layout file",ex);
							JOptionPane.showMessageDialog(Workbench.this,"Error opening layout file: "+ex.getMessage());
						}
					}
				}
			}
			
		};				
		
		loadLayoutXMLAction.putValue(Action.NAME, "Open layout XML");
				
		zariaMenu.add(new JMenuItem(dumpLayoutXMLAction));
		zariaMenu.add(new JMenuItem(loadLayoutXMLAction));
		zariaMenu.add(new JMenuItem(saveLayoutXMLAction));
		
		return menuBar;
	}
	
	/**
	 * set the title to the profile name and version, otherwise just Taverna Workbench
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
		String remoteProfileURL=System.getProperty("raven.remoteprofile");
		try {
			ProfileHandler handler=new ProfileHandler(remoteProfileURL);
			if (handler.isNewVersionAvailable()) {
				int retval=JOptionPane.showConfirmDialog(Workbench.this, "New updates are available, update now?");
				if (retval==JOptionPane.YES_OPTION) {
					try {
						handler.updateLocalProfile();
						JOptionPane.showMessageDialog(Workbench.this,"Your updates will be applied when you restart Taverna","Resart required",JOptionPane.INFORMATION_MESSAGE);
					}
					catch(Exception e) {
						logger.error("Error updating local profile",e);
						JOptionPane.showMessageDialog(Workbench.this, "Updating your profile failed, try again later.", "Error updating profile", JOptionPane.WARNING_MESSAGE);
					}					
				}
			}
			else {
				JOptionPane.showMessageDialog(Workbench.this,"You have all the latest components for this profile","No updates",JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (Exception e) {
			logger.error("Error checking for new profile",e);
			JOptionPane.showMessageDialog(Workbench.this, "Currently unable to check for updates, try again later.", "Error checking for update", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private WindowAdapter getWindowClosingAdaptor() {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					storeUserPrefs();
				}
				catch(Exception ex) {
					logger.error("Error writing user preferences when closing",ex);
				}
				System.exit(0);
			}
		};
	}	

	private void readLastPreferences() {
		File userDir=MyGridConfiguration.getUserDir("conf");
		File size=new File(userDir,"preferences.properties");
		if (size.exists()) {
			Properties props=new Properties();
			try {
				props.load(size.toURL().openStream());
				String swidth=props.getProperty("width");
				String sheight=props.getProperty("height");
				String sx=props.getProperty("x");
				String sy=props.getProperty("y");
				
				Dimension resolution=getToolkit().getScreenSize();
				
				
				int width=Integer.parseInt(swidth);
				int height=Integer.parseInt(sheight);
				int x=Integer.parseInt(sx);
				int y=Integer.parseInt(sy);
				
				if (resolution.getWidth() < width ) {
					width=(int)resolution.getWidth();
				}
				
				if (resolution.getHeight() < height) {
					height=(int)resolution.getHeight();
				}
				
				if (x>(resolution.getWidth()-50) || x<0) {
					x=0;
				}
				
				if (y>(resolution.getHeight()-50) || y<0) {
					y=0;
				}
				
				this.setBounds(x, y, width, height);
				this.repaint();
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error loading default window dimensions",e);
			}
		}
	}

	private File readLastLayout() {
		File userDir=MyGridConfiguration.getUserDir("conf");		
		File layout=new File(userDir,"layout.xml");
		if (layout.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(layout.toURL().openStream());
				SAXBuilder builder = new SAXBuilder(false);
				Document document = builder.build(isr);
				basePane.configure(document.detachRootElement());
			}
			catch(FileNotFoundException e) {
				logger.info("last used layout not found");
				//ignore and just use defaults
			}
			catch(IOException e) {
				logger.warn("IOException reading default layout",e);
			}
			catch(Exception e) {
				logger.error("Exception reading default layout",e);
			}
		}
		return userDir;
	}
	
	private void storeUserPrefs() throws IOException {
		File userDir=MyGridConfiguration.getUserDir("conf");				
		
		//store current window size
		File size=new File(userDir,"preferences.properties");		
		Writer writer=new BufferedWriter(new FileWriter(size));
		writer.write("width="+this.getWidth()+"\n");
		writer.write("height="+this.getHeight()+"\n");		
		writer.write("x="+this.getX()+"\n");
		writer.write("y="+this.getY()+"\n");
		writer.flush();
		writer.close();
	}
	
	
	private void setModelChangeListener() {
		ModelMap.DEFAULT_MODEL_LISTENER = new ModelChangeListener() {

			private ScuflModelEventListener listener = new ScuflModelEventListener() {
				public void receiveModelEvent(ScuflModelEvent event) {
					// Refresh file menu to reflect any changes to the workflow
					// titles. This isn't terribly efficient but hey.					
					refreshFileMenu();
				}
			};
			private ScuflModel currentWorkflowModel = null;
			
			public synchronized void modelChanged(String modelName, Object oldModel, Object newModel) {
				if (! modelName.equals(ModelMap.CURRENT_WORKFLOW)) {
					return;
				}
				if (! (newModel instanceof ScuflModel)) {
					logger.error(ModelMap.CURRENT_WORKFLOW +
							" is not a ScuflModel: " + newModel);
					return;
				}
				ScuflModel newWorkflow = (ScuflModel)newModel;
				for (WorkflowModelViewSPI view : getWorkflowViews()) {
					view.detachFromModel();
					view.attachToModel(newWorkflow);
				}
				if (currentWorkflowModel != null) {
					currentWorkflowModel.removeListener(listener);
				}
				currentWorkflowModel = newWorkflow;
				currentWorkflowModel.addListener(listener);		
				refreshFileMenu();
			}

			public synchronized void modelDestroyed(String modelName) {
				if (! modelName.equals(ModelMap.CURRENT_WORKFLOW)) {
					return;
				}
				if (currentWorkflowModel != null) {
					currentWorkflowModel.removeListener(listener);
				}
				currentWorkflowModel = null;				
			}

			public synchronized void modelCreated(String modelName, Object model) {
				if (! modelName.equals(ModelMap.CURRENT_WORKFLOW)) {
					return;
				}
				if (! (model instanceof ScuflModel)) {
					logger.error(ModelMap.CURRENT_WORKFLOW +
							" is not a ScuflModel: " + model);
					return;
				}
				if (currentWorkflowModel != null) {
					currentWorkflowModel.removeListener(listener);
				}
				ScuflModel newWorkflow = (ScuflModel)model;
				newWorkflow.addListener(listener);
				currentWorkflowModel = newWorkflow;
				for (WorkflowModelViewSPI view : getWorkflowViews()) {
					view.detachFromModel();
					view.attachToModel(newWorkflow);
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
		
		if (workflowModels.size()>1) {
			fileMenu.add(new JMenuItem(closeWorkflowAction()));
		}
		
		if (!workflowModels.isEmpty()) {
			fileMenu.addSeparator();
		}
		for (final ScuflModel model : workflowModels.getModels()) {
			Action selectModel = new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					ModelMap.getInstance().setModel(ModelMap.CURRENT_WORKFLOW, model);				
				}
			};
			selectModel.putValue(Action.SMALL_ICON,TavernaIcons.windowExplorer);
			selectModel.putValue(Action.NAME,model.getDescription().getTitle());
			selectModel.putValue(Action.SHORT_DESCRIPTION,model.getDescription().getTitle());
			
			if (model == ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_WORKFLOW)) {
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
		a.putValue(Action.NAME,"New workflow");
		return a;
	}
	
	/**
	 * The action to remove the current workflow from the workbench
	 * @return Action
	 */
	private Action closeWorkflowAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				ScuflModel currentModel=(ScuflModel)ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_WORKFLOW);
				
				if (currentModel!=null) {
					int ret=JOptionPane.showConfirmDialog(Workbench.this, "Are you sure you want to close the workflow titled '"+currentModel.getDescription().getTitle()+"'");
					if (ret==JOptionPane.YES_OPTION) {
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