/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scuflui.ScavengerTreePanel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

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
	
	private ActivityPaletteModel paletteModel = null;
	
	JTabbedPane tabbedPane;
	
	private JProgressBar progressBar;
	
	ScuflModel currentWorkflow = null;
	
	private boolean watchingWorkflow = true;
	
	public ActivityPalettePanel() {
		this.setLayout(new BorderLayout());
		
		this.paletteModel = new ActivityPaletteModel(this);

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
//		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
		this.add(this.tabbedPane, BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		JButton expandAllItem = new JButton("expand"); //$NON-NLS-1$
		expandAllItem.setActionCommand("expandAll"); //$NON-NLS-1$
		expandAllItem.addActionListener(this);
		menuBar.add(expandAllItem);
		
		JButton collapseAllItem = new JButton("collapse"); //$NON-NLS-1$
		collapseAllItem.setActionCommand("collapseAll"); //$NON-NLS-1$
		collapseAllItem.addActionListener(this);
		menuBar.add(collapseAllItem);
		
		JButton addScavengerItem = new JButton("add scavenger"); //$NON-NLS-1$
		addScavengerItem.setActionCommand("addScavenger"); //$NON-NLS-1$
		addScavengerItem.addActionListener(this);
		menuBar.add(addScavengerItem);
		
		JButton hideSubsetItem = new JButton("hide subset");
		hideSubsetItem.setActionCommand("hideSubset");
		hideSubsetItem.addActionListener(this);
		menuBar.add(hideSubsetItem);
		
		JButton showSubsetItem = new JButton("show subset");
		showSubsetItem.setActionCommand("showSubset");
		showSubsetItem.addActionListener(this);
		menuBar.add(showSubsetItem);
		
		JButton createSubsetItem = new JButton("create subset");
		createSubsetItem.setActionCommand("createSubset");
		createSubsetItem.addActionListener(this);
		menuBar.add(createSubsetItem);
		
		JCheckBox watchingWorkflowItem = new JCheckBox("watch workflow", true);
		watchingWorkflowItem.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.DESELECTED) {
					if (currentWorkflow != null) {
						paletteModel.detachFromModel(currentWorkflow);
					}
					watchingWorkflow = false;
				} else {
					if (currentWorkflow != null) {
						try {
							paletteModel.attachToModel(currentWorkflow);
						} catch (ScavengerCreationException e) {
							// TODO Auto-generated catch block
						}
					}
					watchingWorkflow = true;
				}
			}
			
		});
		menuBar.add(watchingWorkflowItem);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		progressBar.setOrientation(JProgressBar.VERTICAL);
		this.add(progressBar, BorderLayout.EAST);
		
		this.add(menuBar, BorderLayout.SOUTH);
		this.paletteModel.addListener(this);
		this.paletteModel.initialize();
	}

	public void attachToModel(ScuflModel model) {
		if (this.currentWorkflow !=null) {
			logger.warn("Did not detachFromModel() before attachToModel()"); //$NON-NLS-1$
			detachFromModel();
		}
		this.currentWorkflow = model;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			ActivitySubsetPanel tabPanel = (ActivitySubsetPanel) tabbedPane.getComponentAt(i);
			tabPanel.setCurrentWorkflow(model);
		}
		try {
			if (watchingWorkflow) {
				paletteModel.attachToModel(model);
			}
		} catch (ScavengerCreationException e) {
			//TODO figure out what to do
		}
		}

	public void detachFromModel() {
		if (this.currentWorkflow != null) {
			for (int i = 0; i < tabbedPane.getComponentCount(); i++) {
				ActivitySubsetPanel tabPanel = (ActivitySubsetPanel) tabbedPane.getComponentAt(i);
				tabPanel.setCurrentWorkflow(null);
			}
			if (watchingWorkflow) {
				paletteModel.detachFromModel(this.currentWorkflow);
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

	public void subsetModelAdded(ActivityPaletteModel activityPaletteModel,
			ActivityRegistrySubsetModel subsetModel) {
		synchronized(tabbedPane) {
			ActivitySubsetPanel tabPanel = new ActivitySubsetPanel(subsetModel);
			tabPanel.setCurrentWorkflow(this.currentWorkflow);
			int tabCount = tabbedPane.getTabCount();
			String subsetName = subsetModel.getName();
			int position;
			for (position = 0;
				(position < tabCount) && (tabbedPane.getTitleAt(position).compareTo(subsetName) < 0);
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
	 * @return the paletteModel
	 */
	public synchronized final ActivityPaletteModel getPaletteModel() {
		return this.paletteModel;
	}

	private ActivitySubsetPanel getCurrentTab() {
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
		JPopupMenu scavengerAdderPopupMenu = new ScavengerAdderPopupMenu(this);
		scavengerAdderPopupMenu.show(c,px,py);
	}
	
	private void showShowSubsetPopup(Component c, int px, int py) {
		JPopupMenu showSubsetPopupMenu = new ShowSubsetPopupMenu(this);
		showSubsetPopupMenu.show(c,px,py);
	}
	
	private void showCreateSubsetPopup(Component c, int px, int py) {
		String subsetName = (String) JOptionPane
		.showInputDialog(c,
				"Name of subset");
		if ((subsetName == null) || (subsetName.length() == 0)) {
			JOptionPane.showMessageDialog(c, "subset name must be specified");
		}
		boolean found = false;
		for (ActivityRegistrySubsetModel subsetModel : paletteModel.getSubsetModels()) {
			if (subsetModel.getName().equals(subsetName)) {
				JOptionPane.showMessageDialog(c, "The name " + subsetName + " is already in use");
				found = true;
				break;
			}
		}
		if (!found) {
			Component selectedComponent = tabbedPane.getSelectedComponent();
			if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
				Set<PropertyKey> profile = new HashSet<PropertyKey>();
				for (PropertyKeySetting setting : subsetPanel.getPropertyProfile()) {
					profile.add(setting.getPropertyKey());
				}
				paletteModel.addSubsetModelFromSelection(subsetName,
						subsetPanel.getSelectedObjects(),
						profile);
			}
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals("expandAll")) { //$NON-NLS-1$
			expandTab();
		} else if (command.equals("collapseAll")) { //$NON-NLS-1$
			collapseTab();
		} else if (command.equals("addScavenger")) { //$NON-NLS-1$
	           Component c = (Component) e.getSource();
	           int py = c.getY() + c.getHeight() + 2;
	 			showAddScavengerPopup(c, 0, py);
		} else if (command.equals("hideSubset")) {
			Component selectedComponent = tabbedPane.getSelectedComponent();
			if ((selectedComponent != null) && (selectedComponent instanceof ActivitySubsetPanel)){
				ActivitySubsetPanel subsetPanel = (ActivitySubsetPanel) selectedComponent;
				subsetPanel.destroy();
				tabbedPane.remove(subsetPanel);
			}
		} else if (command.equals("showSubset")) {
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showShowSubsetPopup(c,0,py);
		} else if (command.equals("createSubset")) {
			Component c = (Component) e.getSource();
			int py = c.getY() + c.getHeight() + 2;
			showCreateSubsetPopup(c,0,py);
		}
				
	}

	public void scavengingDone(ActivityPaletteModel model) {
		this.progressBar.setVisible(false);
	}

	public void scavengingStarted(ActivityPaletteModel model, String message) {
		this.progressBar.setString(message);
		this.progressBar.setStringPainted(true);
		this.progressBar.setVisible(true);
	}
	
	public int getIndexOfTab(String tabName) {
		return this.tabbedPane.indexOfTab(tabName);
	}

	/**
	 * @return the currentWorkflow
	 */
	public synchronized final ScuflModel getCurrentWorkflow() {
		return currentWorkflow;
	}

}
