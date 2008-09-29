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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.drizzle.bean.ActivityPaletteModelBean;
import net.sf.taverna.t2.drizzle.bean.ActivitySetModelBean;
import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.drizzle.model.ActivityPaletteModelListener;
import net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification;
import net.sf.taverna.t2.drizzle.model.ActivitySubsetModel;
import net.sf.taverna.t2.drizzle.model.ActivitySubsetSelectionIdentification;
import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.query.ActivitySavedConfigurationQuery;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.ObjectNotFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;
import net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelperRegistry;
import org.embl.ebi.escience.scuflworkers.web.WebScavengerHelper;
/**
 * @author alanrw
 *
 */
public class ActivityPalettePanel extends JPanel implements WorkflowModelViewSPI,
ActivityPaletteModelListener, ActionListener {

	static Logger logger = Logger.getLogger(ActivityPalettePanel.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -891768552987961118L;
	
	ActivityPaletteModel paletteModel = null;
	
	JTabbedPane tabbedPane;
	
	private JProgressBar progressBar;
	
	ScuflModel currentWorkflow = null;
	
	boolean watchingWorkflow = true;
	
	private HashMap<ActivitySubsetModel, ActivitySubsetPanel> subsetToPanelMap;
	
	/**
	 * 
	 */
	public ActivityPalettePanel() {
		this.subsetToPanelMap = new HashMap<ActivitySubsetModel, ActivitySubsetPanel>();
		this.setLayout(new BorderLayout());
		
		this.paletteModel = new ActivityPaletteModel(this);
		
		final JMenuItem clearSubsetItem = new JMenuItem("Clear subset"); //$NON-NLS-1$
		clearSubsetItem.setActionCommand("clearSubset"); //$NON-NLS-1$
		clearSubsetItem.setToolTipText("clear the contents of the subset"); //$NON-NLS-1$
		clearSubsetItem.addActionListener(this);

		final JMenuItem removeSelectionItem = new JMenuItem("Remove selection"); //$NON-NLS-1$
		removeSelectionItem.setActionCommand("removeSelection"); //$NON-NLS-1$
		removeSelectionItem.setToolTipText("remove the selected activities from the the subset"); //$NON-NLS-1$
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
		
		JMenuItem saveKindConfigItem = new JMenuItem("Save kind configuration"); //$NON-NLS-1$
		saveKindConfigItem.setToolTipText("Save the kind configuration"); //$NON-NLS-1$
		saveKindConfigItem.setActionCommand("saveKindConfiguration"); //$NON-NLS-1$
		saveKindConfigItem.addActionListener(this);

		JMenuItem loadKindConfigItem = new JMenuItem("Load kind configuration"); //$NON-NLS-1$
		loadKindConfigItem.setToolTipText("Load the kind configuration"); //$NON-NLS-1$
		loadKindConfigItem.setActionCommand("loadKindConfiguration"); //$NON-NLS-1$
		loadKindConfigItem.addActionListener(this);
		
		JMenuItem saveActivitySetItem = new JMenuItem("Save activity set"); //$NON-NLS-1$
		saveActivitySetItem.setToolTipText("Save the activity set"); //$NON-NLS-1$
		saveActivitySetItem.setActionCommand("saveActivitySet"); //$NON-NLS-1$
		saveActivitySetItem.addActionListener(this);

		JMenuItem loadActivitySetItem = new JMenuItem("Load activity set"); //$NON-NLS-1$
		loadActivitySetItem.setToolTipText("Load an activity set"); //$NON-NLS-1$
		loadActivitySetItem.setActionCommand("loadActivitySet"); //$NON-NLS-1$
		loadActivitySetItem.addActionListener(this);
		
		JMenuItem expandAllItem = new JMenuItem("Expand"); //$NON-NLS-1$
		expandAllItem.setToolTipText("Expand all the nodes in the tree"); //$NON-NLS-1$
		expandAllItem.setActionCommand("expandAll"); //$NON-NLS-1$
		expandAllItem.addActionListener(this);
		
		JMenuItem collapseAllItem = new JMenuItem("Collapse"); //$NON-NLS-1$
		collapseAllItem.setToolTipText("Collapse all the nodes in the tree"); //$NON-NLS-1$
		collapseAllItem.setActionCommand("collapseAll"); //$NON-NLS-1$
		collapseAllItem.addActionListener(this);
		
		final JMenu addScavengerItem = new JMenu("Add scavenger"); //$NON-NLS-1$
		addScavengerItem.setToolTipText("Add a new subset from activities detected by a scavenger"); //$NON-NLS-1$
		addScavengerItem.addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void menuDeselected(MenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void menuSelected(MenuEvent arg0) {
				addScavengerItem.removeAll();
				ScavengerHelper webScavengerHelper = null;
				// Iterate over the scavenger creator list from the
				// ProcessorHelper class

				List<ScavengerHelper> scavengerHelpers = ScavengerHelperRegistry
						.instance().getScavengerHelpers();

				// sort alphabetically
				Collections.sort(scavengerHelpers, new Comparator<ScavengerHelper>() {
					public int compare(ScavengerHelper o1, ScavengerHelper o2) {
						if (o1.getScavengerDescription() == null)
							return -1;
						if (o2.getScavengerDescription() == null)
							return 1;
						return o1.getScavengerDescription().compareTo(
								o2.getScavengerDescription());
					}
				});

				for (ScavengerHelper scavengerHelper : scavengerHelpers) {
					// Instantiate a ScavengerHelper...
					try {
						// webscavenger helper is added after the
						// seperator
						if (scavengerHelper instanceof WebScavengerHelper) {
							webScavengerHelper = scavengerHelper;
						} else {
							addScavengerHelperToMenu(addScavengerItem, scavengerHelper);
						}
					} catch (Exception ex) {
						logger.error("Exception adding scavenger helper to scavenger tree"); //$NON-NLS-1$
					}
				}
				// if (!parentPanel.getPaletteModel().isPopulating()) {
				// TODO
				addScavengerItem.addSeparator();

				if (webScavengerHelper != null) {
					addScavengerHelperToMenu(addScavengerItem, webScavengerHelper);
				}
				}
			private void addScavengerHelperToMenu(JMenu menu,
					ScavengerHelper scavengerHelper) {
				if (menu == null) {
					throw new NullPointerException("menu cannot be null"); //$NON-NLS-1$
				}
				if (scavengerHelper == null) {
					throw new NullPointerException("scavengerHelper cannot be null"); //$NON-NLS-1$
				}
				String scavengerDescription = scavengerHelper.getScavengerDescription();
				if (scavengerDescription != null) {
					JMenuItem scavengerMenuItem = new JMenuItem(scavengerDescription,
							scavengerHelper.getIcon());
					scavengerMenuItem.addActionListener(scavengerHelper
							.getListener(ActivityPalettePanel.this.paletteModel.getAdapter()));
					menu.add(scavengerMenuItem);
				}
			}			
		});
		
		JMenuItem hideSubsetItem = new JMenuItem("Hide subset"); //$NON-NLS-1$
		hideSubsetItem.setToolTipText("Hide the subset of activities shown in the current tab"); //$NON-NLS-1$
		hideSubsetItem.setActionCommand("hideSubset"); //$NON-NLS-1$
		hideSubsetItem.addActionListener(this);
	
		final JMenu showSubsetItem = new JMenu("Show subset"); //$NON-NLS-1$
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
				List<ActivitySubsetModel> subsets = new ArrayList<ActivitySubsetModel>(
						getPaletteModel().getSubsetModels());

				Collections.sort(subsets,
						new Comparator<ActivitySubsetModel>() {
							public int compare(ActivitySubsetModel arg0,
									ActivitySubsetModel arg1) {
								return (arg0.getName().compareTo(arg1.getName()));
							}

						});

				for (final ActivitySubsetModel subset : subsets) {
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
		
		final JMenu copySelectionItem = new JMenu("Copy selection"); //$NON-NLS-1$
		copySelectionItem.setToolTipText("Copy the selected activities to another subset"); //$NON-NLS-1$
		copySelectionItem.addMenuListener(new MenuListener() {

			public void menuCanceled(MenuEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void menuDeselected(MenuEvent arg0) {
				// TODO Auto-generated method stub
			}

			public void menuSelected(MenuEvent arg0) {
				copySelectionItem.removeAll();
				Component selectedComponent = ActivityPalettePanel.this.tabbedPane.getSelectedComponent();

				if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
						final ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
						
					List<ActivitySubsetModel> subsets = new ArrayList<ActivitySubsetModel>(
							getPaletteModel().getSubsetModels());

					Collections.sort(subsets,
							new Comparator<ActivitySubsetModel>() {
								public int compare(ActivitySubsetModel a,
										ActivitySubsetModel b) {
									return (a.getName().compareTo(b.getName()));
								}

							});

					for (final ActivitySubsetModel subset : subsets) {
						ActivitySubsetIdentification ident = subset.getIdent();
						String subsetName = subset.getName();
						int subsetIndex = getIndexOfTab(subsetName);
						if ((subsetIndex != -1) && (!getCurrentTab().getName().equals(subsetName)) && (ident instanceof ActivitySubsetSelectionIdentification)) {
							JMenuItem subsetItem = new JMenuItem(subset.getName());
							subsetItem.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent a) {
									PropertiedObjectFilter<ProcessorFactoryAdapter> newFilter =
										new ObjectMembershipFilter<ProcessorFactoryAdapter>(subsetPanel.getSelectedObjects());
									subset.addOredFilter(newFilter);
								}

							});
							copySelectionItem.add(subsetItem);
						}
					}
					}
			}
			
		});
		
		JMenuItem createSubsetItem = new JMenuItem("Create subset"); //$NON-NLS-1$
		createSubsetItem.setToolTipText("Create a new subset of activities"); //$NON-NLS-1$
		createSubsetItem.setActionCommand("createSubset"); //$NON-NLS-1$
		createSubsetItem.addActionListener(this);
		
		
		JMenuItem searchSubsetItem = new JMenuItem("Search subset"); //$NON-NLS-1$
		searchSubsetItem.setToolTipText("Search for activities and add them to search results"); //$NON-NLS-1$
		searchSubsetItem.setActionCommand("searchSubset"); //$NON-NLS-1$
		searchSubsetItem.addActionListener(this);
		
		JMenuItem configureSubsetKindItem = new JMenuItem("Configure subset"); //$NON-NLS-1$
		configureSubsetKindItem.setToolTipText("Configure the display of a subset"); //$NON-NLS-1$
		configureSubsetKindItem.setActionCommand("configureSubsetKind"); //$NON-NLS-1$
		configureSubsetKindItem.addActionListener(this);
		
		JCheckBox watchingWorkflowItem = new JCheckBox("Watch workflow", true); //$NON-NLS-1$
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
		JMenu saveLoad = new JMenu("Save/load"); //$NON-NLS-1$
		saveLoad.add(saveKindConfigItem);
		saveLoad.add(loadKindConfigItem);
		saveLoad.add(saveActivitySetItem);
		saveLoad.add(loadActivitySetItem);
		menuBar.add(saveLoad);
		
		JMenu expandCollapse = new JMenu("Expand/collapse"); //$NON-NLS-1$
		expandCollapse.add(expandAllItem);
		expandCollapse.add(collapseAllItem);
		menuBar.add(expandCollapse);
		
		menuBar.add(addScavengerItem);
		
		JMenu subsetControl = new JMenu("Subset control"); //$NON-NLS-1$
		subsetControl.add(hideSubsetItem);
		subsetControl.add(showSubsetItem);
		subsetControl.add(createSubsetItem);
		subsetControl.add(clearSubsetItem);
		subsetControl.add(configureSubsetKindItem);
		menuBar.add(subsetControl);
		
		JMenu selectionControl = new JMenu("Selection"); //$NON-NLS-1$
		selectionControl.add(copySelectionItem);
		selectionControl.add(removeSelectionItem);
//		selectionControl.add(searchSubsetItem);
		menuBar.add(selectionControl);
		
		menuBar.add(searchSubsetItem);
		
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

	/**
	 * @see org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
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

	/**
	 * @see org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI#detachFromModel()
	 */
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

	/**
	 * @see org.embl.ebi.escience.scuflui.spi.UIComponentSPI#onDisplay()
	 */
	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see org.embl.ebi.escience.scuflui.spi.UIComponentSPI#onDispose()
	 */
	public void onDispose() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param tabPanel
	 */
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
	
	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivityPaletteModelListener#subsetModelAdded(net.sf.taverna.t2.drizzle.model.ActivityPaletteModel, net.sf.taverna.t2.drizzle.model.ActivitySubsetModel)
	 */
	public void subsetModelAdded(ActivityPaletteModel activityPaletteModel,
			ActivitySubsetModel subsetModel) {
		if (activityPaletteModel == null) {
			throw new NullPointerException("activityPaletteModel cannot be null"); //$NON-NLS-1$
		}
		if (subsetModel == null) {
			throw new NullPointerException("subsetModel cannot be null"); //$NON-NLS-1$
		}
			ActivitySubsetPanel tabPanel = new ActivitySubsetPanel(subsetModel);
			this.subsetToPanelMap.put(subsetModel, tabPanel);
			tabPanel.setName(subsetModel.getName());
			insertTab(tabPanel);
	}
	
	/**
	 * @param subsetModel
	 */
	public void reshowSubsetModel (ActivitySubsetModel subsetModel) {
		insertTab(this.subsetToPanelMap.get(subsetModel));
		showTabWithModel(subsetModel);
	}

	/**
	 * @return the paletteModel
	 */
	public synchronized final ActivityPaletteModel getPaletteModel() {
		return this.paletteModel;
	}

	/**
	 * @return
	 */
	public ActivitySubsetPanel getCurrentTab() {
		ActivitySubsetPanel result = null;
		Component selectedTab = this.tabbedPane.getSelectedComponent();
		if (selectedTab instanceof ActivitySubsetPanel) {
			result = (ActivitySubsetPanel) selectedTab;
		}
		return result;
	}
	
	/**
	 * 
	 */
	public void expandTab() {
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
					JDialog dialog = new SearchSubsetDialog(this,
							subsetPanel,
							this.paletteModel.getActivitySetModel().getPropertiedProcessorFactoryAdapterSet());
					dialog.setLocationRelativeTo(this);
					dialog.setVisible(true);
				}				
			}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		String command = e.getActionCommand();
		if (command.equals("expandAll")) { //$NON-NLS-1$
			expandTab();
		} else if (command.equals("collapseAll")) { //$NON-NLS-1$
			collapseTab();
		} else if (command.equals("hideSubset")) { //$NON-NLS-1$
			Component selectedComponent = this.tabbedPane.getSelectedComponent();
			if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
	
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
				subsetPanel.destroy();
				this.tabbedPane.remove(subsetPanel);
				selectedComponent = this.tabbedPane.getSelectedComponent();
				if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)) {
					subsetPanel = (ActivitySubsetPanel) selectedComponent;
					subsetPanel.setModels();
				}
			}
		} else if (command.equals("createSubset")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showCreateSubsetPopup(c,0,py);
		} else if (command.equals("clearSubset")) { //$NON-NLS-1$
			if (JOptionPane.showConfirmDialog((Component)e.getSource(), "Really clear the subset?", "confirm", JOptionPane.YES_NO_OPTION)  //$NON-NLS-1$//$NON-NLS-2$
					== JOptionPane.YES_OPTION) {
				Component selectedComponent = this.tabbedPane.getSelectedComponent();
				if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
					ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
					subsetPanel.getSubsetModel().clearSubset();
					subsetPanel.setModels();
				}				
			}
		} else if (command.equals("searchSubset")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showSearchSubsetPopup(c,0,py);		
		} else if (command.equals("removeSelection")) { //$NON-NLS-1$
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
		} else if (command.equals("loadKindConfiguration")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser(""); //$NON-NLS-1$
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			int returnVal = chooser.showOpenDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File loadFile = chooser.getSelectedFile();
				try {
					Object o = BeanSerialiser.getInstance().beanFromXMLFile(loadFile);
					if (o instanceof ActivityPaletteModelBean) {
						this.paletteModel.mergeWithBean((ActivityPaletteModelBean)o);
					}
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command.equals("saveKindConfiguration")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser(""); //$NON-NLS-1$
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			int returnVal = chooser.showSaveDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = chooser.getSelectedFile();
				try {
					BeanSerialiser.getInstance().beanableToXMLFile(this.paletteModel, saveFile);
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command.equals("loadActivitySet")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser(""); //$NON-NLS-1$
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			int returnVal = chooser.showOpenDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File loadFile = chooser.getSelectedFile();
				try {
					Object o = BeanSerialiser.getInstance().beanFromXMLFile(loadFile);
					if (o instanceof ActivitySetModelBean) {
						this.paletteModel.addImmediateQuery(new ActivitySavedConfigurationQuery((ActivitySetModelBean)o));

						this.paletteModel.getActivitySetModel().mergeWithBean((ActivitySetModelBean)o);
					}
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command.equals("saveActivitySet")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser(""); //$NON-NLS-1$
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			int returnVal = chooser.showSaveDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = chooser.getSelectedFile();
				try {
					BeanSerialiser.getInstance().beanableToXMLFile(this.paletteModel.getActivitySetModel(), saveFile);
				} catch (StorageException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command.equals("configureSubsetKind")) { //$NON-NLS-1$
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

	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivityPaletteModelListener#scavengingDone(net.sf.taverna.t2.drizzle.model.ActivityPaletteModel)
	 */
	public void scavengingDone(ActivityPaletteModel model) {
		if (model == null) {
			throw new NullPointerException("model cannot be null"); //$NON-NLS-1$
		}
		this.progressBar.setVisible(false);
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivityPaletteModelListener#scavengingStarted(net.sf.taverna.t2.drizzle.model.ActivityPaletteModel, java.lang.String)
	 */
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
	
	/**
	 * @param tabName
	 * @return
	 */
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

	/**
	 * @return
	 */
	public Frame getContainingFrame() {
		Container result = this;
		while ((result != null) && !(result instanceof Frame)) {
			result = result.getParent();
		}
		return (Frame) result;
	}

	/**
	 * @param subsetModel
	 */
	public void showTabWithModel(ActivitySubsetModel subsetModel) {
		int tabCount = this.tabbedPane.getTabCount();
		ActivitySubsetPanel selectedSubsetPanel = null;
		for (int i = 0; (i < tabCount) && (selectedSubsetPanel == null); i++) {
			Component c = this.tabbedPane.getComponent(i);
			if (c instanceof ActivitySubsetPanel) {
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) c;
				if (subsetPanel.getSubsetModel() == subsetModel) {
					selectedSubsetPanel = subsetPanel;
				}
			}
		}
		if (selectedSubsetPanel != null) {
			this.tabbedPane.setSelectedComponent(selectedSubsetPanel);
			selectedSubsetPanel.setModels();
		}
	}

}
