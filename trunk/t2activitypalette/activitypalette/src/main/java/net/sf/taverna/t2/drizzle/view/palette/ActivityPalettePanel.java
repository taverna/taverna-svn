/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.palette;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.drizzle.model.ActivityPaletteModelListener;
import net.sf.taverna.t2.drizzle.model.ActivityRegistrySubsetModel;
import net.sf.taverna.t2.drizzle.util.ObjectMembershipFilter;
import net.sf.taverna.t2.drizzle.util.ObjectNotFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.view.subset.ActivitySubsetPanel;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.util.beanable.jaxb.BeanSerializer;
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
	
	public ActivityPalettePanel() {
		subsetToPanelMap = new HashMap<ActivityRegistrySubsetModel, ActivitySubsetPanel>();
		this.setLayout(new BorderLayout());
		
		this.paletteModel = new ActivityPaletteModel(this);
		
		final JButton clearSubsetItem = new JButton("clear subset");
		clearSubsetItem.setActionCommand("clearSubset");
		clearSubsetItem.setToolTipText("clear the contents of the subset");
		clearSubsetItem.addActionListener(this);

		final JButton removeSelectionItem = new JButton("remove selection");
		removeSelectionItem.setActionCommand("removeSelection");
		removeSelectionItem.setToolTipText("remove the selected activities from the the subset");
		removeSelectionItem.addActionListener(this);

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
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
		
		JButton saveRegistryItem = new JButton("save registry");
		saveRegistryItem.setToolTipText("Save the known subsets and their activities");
		saveRegistryItem.setActionCommand("saveRegistry");
		saveRegistryItem.addActionListener(this);
		menuBar.add(saveRegistryItem);
		
		JButton expandAllItem = new JButton("expand"); //$NON-NLS-1$
		expandAllItem.setToolTipText("Expand all the nodes in the tree"); //$NON-NLS-1$
		expandAllItem.setActionCommand("expandAll"); //$NON-NLS-1$
		expandAllItem.addActionListener(this);
		menuBar.add(expandAllItem);
		
		JButton collapseAllItem = new JButton("collapse"); //$NON-NLS-1$
		collapseAllItem.setToolTipText("Collapse all the nodes in the tree"); //$NON-NLS-1$
		collapseAllItem.setActionCommand("collapseAll"); //$NON-NLS-1$
		collapseAllItem.addActionListener(this);
		menuBar.add(collapseAllItem);
		
		JButton addScavengerItem = new JButton("add scavenger"); //$NON-NLS-1$
		addScavengerItem.setToolTipText("Add a new subset from activities detected by a scavenger"); //$NON-NLS-1$
		addScavengerItem.setActionCommand("addScavenger"); //$NON-NLS-1$
		addScavengerItem.addActionListener(this);
		menuBar.add(addScavengerItem);
		
		JButton hideSubsetItem = new JButton("hide subset"); //$NON-NLS-1$
		hideSubsetItem.setToolTipText("Hide the subset of activities shown in the current tab"); //$NON-NLS-1$
		hideSubsetItem.setActionCommand("hideSubset"); //$NON-NLS-1$
		hideSubsetItem.addActionListener(this);
		menuBar.add(hideSubsetItem);
		
		JButton showSubsetItem = new JButton("show subset"); //$NON-NLS-1$
		showSubsetItem.setToolTipText("Select a previously hidden subset of activities to show"); //$NON-NLS-1$
		showSubsetItem.setActionCommand("showSubset"); //$NON-NLS-1$
		showSubsetItem.addActionListener(this);
		menuBar.add(showSubsetItem);
		
		JButton copySelectionItem = new JButton("copy selection");
		copySelectionItem.setToolTipText("Copy the selected activities to another subset");
		copySelectionItem.setActionCommand("copySelection");
		copySelectionItem.addActionListener(this);
		menuBar.add(copySelectionItem);
		
		JButton createSubsetItem = new JButton("create subset"); //$NON-NLS-1$
		createSubsetItem.setToolTipText("Create a new subset of activities"); //$NON-NLS-1$
		createSubsetItem.setActionCommand("createSubset"); //$NON-NLS-1$
		createSubsetItem.addActionListener(this);
		menuBar.add(createSubsetItem);
		
		menuBar.add(clearSubsetItem);
		menuBar.add(removeSelectionItem);
		
		JButton searchSubsetItem = new JButton("search subset"); //$NON-NLS-1$
		searchSubsetItem.setToolTipText("Search for activities and add them to search results"); //$NON-NLS-1$
		searchSubsetItem.setActionCommand("searchSubset"); //$NON-NLS-1$
		searchSubsetItem.addActionListener(this);
		menuBar.add(searchSubsetItem);
		
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
	
	private void showShowSubsetPopup(Component c, int px, int py) {
		if (c == null) {
			throw new NullPointerException("c cannot be null"); //$NON-NLS-1$
		}
		JPopupMenu showSubsetPopupMenu = new ShowSubsetPopupMenu(this);
		showSubsetPopupMenu.show(c,px,py);
	}
	
	private void showCopySelectionPopup(Component c, int px, int py) {
		if (c == null) {
			throw new NullPointerException("c cannot be null"); //$NON-NLS-1$
		}
		Component selectedComponent = this.tabbedPane.getSelectedComponent();
		if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
			ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
			JPopupMenu copySelectionPopupMenu = new CopySelectionPopupMenu(this, subsetPanel.getSelectedObjects());
			copySelectionPopupMenu.show(c,px,py);
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
				subsetPanel.destroy();
				this.tabbedPane.remove(subsetPanel);
			}
		} else if (command.equals("showSubset")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showShowSubsetPopup(c,0,py);
		} else if (command.equals("createSubset")) { //$NON-NLS-1$
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showCreateSubsetPopup(c,0,py);
		} else if (command.equals("copySelection")) {
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showCopySelectionPopup(c,0,py);			
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
				PropertiedObjectFilter<ProcessorFactory> positiveFilter =
					new ObjectMembershipFilter<ProcessorFactory>(subsetPanel.getSelectedObjects());
				PropertiedObjectFilter<ProcessorFactory> negativeFilter = 
					new ObjectNotFilter<ProcessorFactory>(positiveFilter);
				subsetPanel.getSubsetModel().addAndedFilter(negativeFilter);
				subsetPanel.setModels();
			}
		} else if (command.equals("saveRegistry")) {
			Component c = (Component) e.getSource();
			JFileChooser chooser = new JFileChooser("");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			int returnVal = chooser.showSaveDialog(c);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = chooser.getSelectedFile();
				BeanSerialiser.getInstance().beanableToXMLFile(paletteModel, saveFile);
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
