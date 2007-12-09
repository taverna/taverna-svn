/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.drizzle.bean.ActivityPaletteModelBean;
import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.drizzle.model.ActivityPaletteModelListener;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetModel;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetSelectionIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.model.SubsetKindConfiguration;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.ObjectNotFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
/**
 * @author alanrw
 *
 */
public class ActivityPalettePanel extends JPanel implements WorkflowModelViewSPI,
ActivityPaletteModelListener, ActionListener {

	private static Logger logger = Logger.getLogger(ActivityPalettePanel.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -891768552987961118L;
	
	ActivityPaletteModel paletteModel = null;
	
	JTabbedPane tabbedPane;
	
	private JProgressBar progressBar;
	
	ScuflModel currentWorkflow = null;
	
	boolean watchingWorkflow = true;
	
	private HashMap<ActivityRegistrySubsetModel, ActivitySubsetPanel> subsetToPanelMap;
	
	private JMenu showSubsetItem;
	
	public ActivityPalettePanel() {
		subsetToPanelMap = new HashMap<ActivityRegistrySubsetModel, ActivitySubsetPanel>();
		this.setLayout(new BorderLayout());
		
		this.paletteModel = new ActivityPaletteModel(this);
		
		final JMenuItem clearSubsetItem = new JMenuItem("clear subset");
		clearSubsetItem.setActionCommand("clearSubset");
		clearSubsetItem.setToolTipText("clear the contents of the subset");
		clearSubsetItem.addActionListener(this);

		final JMenuItem removeSelectionItem = new JMenuItem("remove selection");
		removeSelectionItem.setActionCommand("removeSelection");
		removeSelectionItem.setToolTipText("remove the selected activities from the the subset");
		removeSelectionItem.addActionListener(this);

		this.tabbedPane = new JTabbedPane();
//		this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
//		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		this.add(this.tabbedPane, BorderLayout.CENTER);
		this.tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				JTabbedPane pane = (JTabbedPane)arg0.getSource();
			    
	            Component c = pane.getSelectedComponent();
	            if (c instanceof ActivitySubsetPanel) {
	            	ActivitySubsetPanel subPanel = (ActivitySubsetPanel) c;
	           	//TODO check if this setModels is really needed
	            	subPanel.setModels();
	            	clearSubsetItem.setEnabled(subPanel.getSubsetModel().isEditable());
	            	removeSelectionItem.setEnabled(subPanel.getSubsetModel().isEditable());
	            }
			}
			
		});
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setToolTipText("Controls for activity subsets"); //$NON-NLS-1$
		
		JMenuItem saveConfigItem = new JMenuItem("save configuration");
		saveConfigItem.setToolTipText("Save the kind configuration");
		saveConfigItem.setActionCommand("saveConfiguration");
		saveConfigItem.addActionListener(this);

		JMenuItem loadConfigItem = new JMenuItem("load configuration");
		loadConfigItem.setToolTipText("Load the kind configuration");
		loadConfigItem.setActionCommand("loadConfiguration");
		loadConfigItem.addActionListener(this);
		
		JMenuItem expandAllItem = new JMenuItem("expand"); //$NON-NLS-1$
		expandAllItem.setToolTipText("Expand all the nodes in the tree"); //$NON-NLS-1$
		expandAllItem.setActionCommand("expandAll"); //$NON-NLS-1$
		expandAllItem.addActionListener(this);
		
		JMenuItem collapseAllItem = new JMenuItem("collapse"); //$NON-NLS-1$
		collapseAllItem.setToolTipText("Collapse all the nodes in the tree"); //$NON-NLS-1$
		collapseAllItem.setActionCommand("collapseAll"); //$NON-NLS-1$
		collapseAllItem.addActionListener(this);
		
		JMenuItem addScavengerItem = new JMenuItem("add scavenger"); //$NON-NLS-1$
		addScavengerItem.setToolTipText("Add a new subset from activities detected by a scavenger"); //$NON-NLS-1$
		addScavengerItem.setActionCommand("addScavenger"); //$NON-NLS-1$
		addScavengerItem.addActionListener(this);
		
		JMenuItem hideSubsetItem = new JMenuItem("hide subset"); //$NON-NLS-1$
		hideSubsetItem.setToolTipText("Hide the subset of activities shown in the current tab"); //$NON-NLS-1$
		hideSubsetItem.setActionCommand("hideSubset"); //$NON-NLS-1$
		hideSubsetItem.addActionListener(this);
		
		showSubsetItem = new JMenu("show subset"); //$NON-NLS-1$
		showSubsetItem.setToolTipText("Select a previously hidden subset of activities to show"); //$NON-NLS-1$
		showSubsetItem.addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void menuSelected(MenuEvent e) {
				showSubsetItem.removeAll();
				List<ActivityRegistrySubsetModel> subsets = new ArrayList<ActivityRegistrySubsetModel>(
						getPaletteModel().getSubsetModels());

				Collections.sort(subsets,
						new Comparator<ActivityRegistrySubsetModel>() {
							public int compare(ActivityRegistrySubsetModel arg0,
									ActivityRegistrySubsetModel arg1) {
								return (arg0.getName().compareTo(arg1.getName()));
							}

						});

				for (final ActivityRegistrySubsetModel subset : subsets) {
					if (getIndexOfTab(subset.getName()) == -1) {
						JMenuItem subsetItem = new JMenuItem(subset.getName());
						subsetItem.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent arg0) {
								reshowSubsetModel(subset);
							}

						});
						showSubsetItem.add(subsetItem);
					}
				}
			}
			}
			
		);
/*		showSubsetItem.setActionCommand("showSubset"); //$NON-NLS-1$
		showSubsetItem.addActionListener(this);*/
		
		final JMenu copySelectionItem = new JMenu("copy selection");
		copySelectionItem.setToolTipText("Copy the selected activities to another subset");
		copySelectionItem.addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void menuDeselected(MenuEvent arg0) {
			}

			public void menuSelected(MenuEvent arg0) {
				copySelectionItem.removeAll();
				Component selectedComponent = tabbedPane.getSelectedComponent();

				if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
						final ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
						
					List<ActivityRegistrySubsetModel> subsets = new ArrayList<ActivityRegistrySubsetModel>(
							getPaletteModel().getSubsetModels());

					Collections.sort(subsets,
							new Comparator<ActivityRegistrySubsetModel>() {
								public int compare(ActivityRegistrySubsetModel arg0,
										ActivityRegistrySubsetModel arg1) {
									return (arg0.getName().compareTo(arg1.getName()));
								}

							});

					for (final ActivityRegistrySubsetModel subset : subsets) {
						ActivityRegistrySubsetIdentification ident = subset.getIdent();
						String subsetName = subset.getName();
						int subsetIndex = getIndexOfTab(subsetName);
						if ((subsetIndex != -1) && (!getCurrentTab().getName().equals(subsetName)) && (ident instanceof ActivityRegistrySubsetSelectionIdentification)) {
							JMenuItem subsetItem = new JMenuItem(subset.getName());
							subsetItem.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {
									PropertiedObjectFilter<ProcessorFactoryAdapter> newFilter = new ObjectMembershipFilter<ProcessorFactoryAdapter>(subsetPanel.getSelectedObjects());
									subset.addOredFilter(newFilter);
								}

							});
							copySelectionItem.add(subsetItem);
						}
					}
					}
			}
			
		});
		
		JMenuItem createSubsetItem = new JMenuItem("create subset"); //$NON-NLS-1$
		createSubsetItem.setToolTipText("Create a new subset of activities"); //$NON-NLS-1$
		createSubsetItem.setActionCommand("createSubset"); //$NON-NLS-1$
		createSubsetItem.addActionListener(this);
		
		
		JMenuItem searchSubsetItem = new JMenuItem("search subset"); //$NON-NLS-1$
		searchSubsetItem.setToolTipText("Search for activities and add them to search results"); //$NON-NLS-1$
		searchSubsetItem.setActionCommand("searchSubset"); //$NON-NLS-1$
		searchSubsetItem.addActionListener(this);
		
		JMenuItem configureSubsetKindItem = new JMenuItem("configure subset");
		configureSubsetKindItem.setToolTipText("Configure the display of a subset");
		configureSubsetKindItem.setActionCommand("configureSubsetKind");
		configureSubsetKindItem.addActionListener(this);
		
		JCheckBox watchingWorkflowItem = new JCheckBox("watch workflow", true); //$NON-NLS-1$
		watchingWorkflowItem.setToolTipText("Load the activities included in the current workflow"); //$NON-NLS-1$
		watchingWorkflowItem.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.DESELECTED) {
					if (ActivityPalettePanel.this.currentWorkflow != null) {
						ActivityPalettePanel.this.paletteModel.detachFromModel(ActivityPalettePanel.this.currentWorkflow);
					}
					ActivityPalettePanel.this.watchingWorkflow = false;
				} else {
					if (ActivityPalettePanel.this.currentWorkflow != null) {
						try {
							ActivityPalettePanel.this.paletteModel.attachToModel(ActivityPalettePanel.this.currentWorkflow);
						} catch (ScavengerCreationException e) {
							// TODO Auto-generated catch block
						}
					}
					ActivityPalettePanel.this.watchingWorkflow = true;
				}
			}
			
		});
		JMenu saveLoad = new JMenu("save/load");
		saveLoad.add(saveConfigItem);
		saveLoad.add(loadConfigItem);
		menuBar.add(saveLoad);
		
		JMenu expandCollapse = new JMenu("expand/collapse");
		expandCollapse.add(expandAllItem);
		expandCollapse.add(collapseAllItem);
		menuBar.add(expandCollapse);
		
		menuBar.add(addScavengerItem);
		
		JMenu subsetControl = new JMenu("subset control");
		subsetControl.add(hideSubsetItem);
		subsetControl.add(showSubsetItem);
		subsetControl.add(createSubsetItem);
		subsetControl.add(clearSubsetItem);
		subsetControl.add(configureSubsetKindItem);
		menuBar.add(subsetControl);
		
		JMenu selectionControl = new JMenu("selection");
		selectionControl.add(copySelectionItem);
		selectionControl.add(removeSelectionItem);
		selectionControl.add(searchSubsetItem);
		menuBar.add(selectionControl);
		
		menuBar.add(watchingWorkflowItem);
		
		this.progressBar = new JProgressBar();
		this.progressBar.setIndeterminate(true);
		this.progressBar.setVisible(false);
		this.progressBar.setOrientation(SwingConstants.VERTICAL);
		this.add(this.progressBar, BorderLayout.EAST);
		
		this.add(menuBar, BorderLayout.SOUTH);
		this.paletteModel.addListener(this);
		this.paletteModel.initialize();
	}

	public void attachToModel(ScuflModel model) {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		if (this.currentWorkflow !=null) {
			logger.warn("Did not detachFromModel() before attachToModel()"); //$NON-NLS-1$
			detachFromModel();
		}
		this.currentWorkflow = model;
		for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
			ActivitySubsetPanel tabPanel = (ActivitySubsetPanel) this.tabbedPane.getComponentAt(i);
			tabPanel.setCurrentWorkflow(model);
		}
		try {
			if (this.watchingWorkflow) {
				this.paletteModel.attachToModel(model);
			}
		} catch (ScavengerCreationException e) {
			//TODO figure out what to do
		}
		}

	public void detachFromModel() {
		if (this.currentWorkflow != null) {
			for (int i = 0; i < this.tabbedPane.getComponentCount(); i++) {
				ActivitySubsetPanel tabPanel = (ActivitySubsetPanel) this.tabbedPane.getComponentAt(i);
				tabPanel.setCurrentWorkflow(null);
			}
			if (this.watchingWorkflow) {
				this.paletteModel.detachFromModel(this.currentWorkflow);
			}
			this.currentWorkflow = null;
		}
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

	public void insertTab(ActivitySubsetPanel tabPanel) {
		synchronized(this.tabbedPane) {
			tabPanel.setCurrentWorkflow(this.currentWorkflow);
			int tabCount = this.tabbedPane.getTabCount();
			String subsetName = tabPanel.getName();
			int position;
			for (position = 0;
				(position < tabCount) && (this.tabbedPane.getTitleAt(position).compareTo(subsetName) < 0);
				position++) {
				// nowt to do
			}
			if (position == tabCount) {
				this.tabbedPane.add (tabPanel);				
			} else {
				this.tabbedPane.add(tabPanel, position);
			}			
		}
	}
	public void subsetModelAdded(ActivityPaletteModel activityPaletteModel,
			ActivityRegistrySubsetModel subsetModel) {
		if (activityPaletteModel == null) {
			throw new NullPointerException("activityPaletteModel cannot be null"); //$NON-NLS-1$
		}
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
			ActivitySubsetPanel tabPanel = new ActivitySubsetPanel(subsetModel);
			subsetToPanelMap.put(subsetModel, tabPanel);
			tabPanel.setName(subsetModel.getName());
			insertTab(tabPanel);
	}
	
	public void reshowSubsetModel (ActivityRegistrySubsetModel subsetModel) {
		insertTab(subsetToPanelMap.get(subsetModel));
	}

	/**
	 * @return the paletteModel
	 */
	public synchronized final ActivityPaletteModel getPaletteModel() {
		return this.paletteModel;
	}

	public ActivitySubsetPanel getCurrentTab() {
		ActivitySubsetPanel result = null;
		Component selectedTab = this.tabbedPane.getSelectedComponent();
		if (selectedTab instanceof ActivitySubsetPanel) {
			result = (ActivitySubsetPanel) selectedTab;
		}
		return result;
	}
	
	private void expandTab() {
		ActivitySubsetPanel currentTab = getCurrentTab();
		if (currentTab != null) {
			currentTab.expandAll();
		}		
	}
	
	private void collapseTab() {
		ActivitySubsetPanel currentTab = getCurrentTab();
		if (currentTab != null) {
			currentTab.collapseAll();
		}		
	}
	
	private void showAddScavengerPopup(Component c, int px, int py) {
		if (c == null) {
			throw new NullPointerException("c cannot be null"); //$NON-NLS-1$
		}
		JPopupMenu scavengerAdderPopupMenu = new ScavengerAdderPopupMenu(this);
		scavengerAdderPopupMenu.show(c,px,py);
	}
		
	private void showCreateSubsetPopup(Component c, @SuppressWarnings("unused")
	final int px, @SuppressWarnings("unused")
	final int py) {
		if (c == null) {
			throw new NullPointerException("c cannot be null"); //$NON-NLS-1$			
		}
		JDialog dialog = new CreateSubsetDialog(this.paletteModel);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}
	
	private void showSearchSubsetPopup(Component c, @SuppressWarnings("unused")
			final int px, @SuppressWarnings("unused")
			final int py) {
				if (c == null) {
					throw new NullPointerException("c cannot be null"); //$NON-NLS-1$			
				}
				Component selectedComponent = this.tabbedPane.getSelectedComponent();
				if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
					ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
					JDialog dialog = new SearchSubsetDialog(subsetPanel,
							this.paletteModel.getActivityRegistry().getRegistry());
					dialog.setLocationRelativeTo(this);
					dialog.setVisible(true);
				}				
			}

	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		String command = e.getActionCommand();
		if (command.equals("expandAll")) { //$NON-NLS-1$
			expandTab();
		} else if (command.equals("collapseAll")) { //$NON-NLS-1$
			collapseTab();
		} else if (command.equals("addScavenger")) { //$NON-NLS-1$
	           Component c = (Component) e.getSource();
	           int py = c.getY() + c.getHeight() + 2;
	 			showAddScavengerPopup(c, 0, py);
		} else if (command.equals("hideSubset")) { //$NON-NLS-1$
			Component selectedComponent = this.tabbedPane.getSelectedComponent();
			if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
	
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
				final JMenuItem showItem = new JMenuItem (subsetPanel.getName());
				showSubsetItem.add(showItem);
				final ActivityRegistrySubsetModel subsetModel = subsetPanel.getSubsetModel();
				showItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						reshowSubsetModel(subsetModel);
						showSubsetItem.remove(showItem);
					}
					
				});
				subsetPanel.destroy();
				this.tabbedPane.remove(subsetPanel);
			}
		} else if (command.equals("createSubset")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showCreateSubsetPopup(c,0,py);
		} else if (command.equals("clearSubset")) {
			if (JOptionPane.showConfirmDialog((Component)e.getSource(), "Really clear the subset?", "confirm", JOptionPane.YES_NO_OPTION)
					== JOptionPane.YES_OPTION) {
				Component selectedComponent = this.tabbedPane.getSelectedComponent();
				if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
					ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
					subsetPanel.getSubsetModel().clearSubset();
					subsetPanel.setModels();
				}				
			}
		} else if (command.equals("searchSubset")) {
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showSearchSubsetPopup(c,0,py);		
		} else if (command.equals("removeSelection")) {
			Component selectedComponent = this.tabbedPane.getSelectedComponent();
			if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)) {
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
				PropertiedObjectFilter<ProcessorFactoryAdapter> positiveFilter =
					new ObjectMembershipFilter<ProcessorFactoryAdapter>(subsetPanel.getSelectedObjects());
				PropertiedObjectFilter<ProcessorFactoryAdapter> negativeFilter = 
					new ObjectNotFilter<ProcessorFactoryAdapter>(positiveFilter);
				subsetPanel.getSubsetModel().addAndedFilter(negativeFilter);
				subsetPanel.setModels();
			}
		} else if (command.equals("loadConfiguration")) {
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser("");
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			int returnVal = chooser.showOpenDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File loadFile = chooser.getSelectedFile();
				try {
					Object o = BeanSerialiser.getInstance().beanFromXMLFile(loadFile);
					if (o instanceof ActivityPaletteModelBean) {
						paletteModel.mergeWithBean((ActivityPaletteModelBean)o);
					}
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command.equals("saveConfiguration")) {
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser("");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			int returnVal = chooser.showSaveDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = chooser.getSelectedFile();
				try {
					BeanSerialiser.getInstance().beanableToXMLFile(paletteModel, saveFile);
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command.equals("configureSubsetKind")) {
			Component selectedComponent = this.tabbedPane.getSelectedComponent();
			if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)) {
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
				String kind = subsetPanel.getSubsetModel().getIdent().getKind();
				JDialog dialog = new SubsetKindConfigurationDialog(ActivitySubsetPanel.kindConfigurationMap.get(kind), subsetPanel);
				dialog.setLocationRelativeTo(this);
				dialog.setVisible(true);		
			}
		}
				
	}

	public void scavengingDone(ActivityPaletteModel model) {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		this.progressBar.setVisible(false);
	}

	public void scavengingStarted(ActivityPaletteModel model, String message) {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		if (message == null) {
			throw new NullPointerException("message cannot be null"); //$NON-NLS-1$
		}
		this.progressBar.setString(message);
		this.progressBar.setStringPainted(true);
		this.progressBar.setVisible(true);
	}
	
	public int getIndexOfTab(String tabName) {
		if (tabName == null) {
			throw new NullPointerException("tabName cannot be null"); //$NON-NLS-1$
		}
		return this.tabbedPane.indexOfTab(tabName);
	}

	/**
	 * @return the currentWorkflow
	 */
	public synchronized final ScuflModel getCurrentWorkflow() {
		return this.currentWorkflow;
	}

	public Frame getContainingFrame() {
		Container result = this;
		while ((result != null) && !(result instanceof Frame)) {
			result = result.getParent();
		}
		return (Frame) result;
	}

}
