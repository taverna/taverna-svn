/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

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
		
		menuBar.add(new ScavengerAdderPopupMenu(this));
		
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
		if (this.currentWorkflow !=null) {
			logger.warn("Did not detachFromModel() before attachToModel()"); //$NON-NLS-1$
			detachFromModel();
		}
		this.currentWorkflow = model;
		for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
			ActivityTabPanel tabPanel = (ActivityTabPanel) this.tabbedPane.getComponentAt(i);
			tabPanel.setCurrentWorkflow(model);
		}
//TODO		model.addListener(eventListener);
		}

	public void detachFromModel() {
		if (this.currentWorkflow != null) {
			for (int i = 0; i < this.tabbedPane.getTabCount(); i++) {
				ActivityTabPanel tabPanel = (ActivityTabPanel) this.tabbedPane.getComponentAt(i);
				tabPanel.setCurrentWorkflow(null);
			}
//TODO			scuflModel.removeListener(eventListener);
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
			final ActivityRegistrySubsetModel subsetModel) {
		Runnable addTabRunnable = new Runnable() {
			public void run() {
				ActivityTabPanel tabPanel = new ActivityTabPanel(subsetModel);
				tabPanel.setCurrentWorkflow(ActivityPalettePanel.this.currentWorkflow);
				int tabCount = ActivityPalettePanel.this.tabbedPane.getTabCount();
				String subsetName = subsetModel.getName();
				int position;
				for (position = 0;
					(position < tabCount) && (ActivityPalettePanel.this.tabbedPane.getTitleAt(position).compareTo(subsetName) < 0);
					position++) {
					// nowt to do
				}
				if (position == tabCount) {
					ActivityPalettePanel.this.tabbedPane.add (new ActivityTabPanel(subsetModel));				
				} else {
					ActivityPalettePanel.this.tabbedPane.add(new ActivityTabPanel(subsetModel), position);
				}				
			}
		};
		SwingUtilities.invokeLater(addTabRunnable);
/*		synchronized(tabbedPane) {
			ActivityTabPanel tabPanel = new ActivityTabPanel(subsetModel);
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
				this.tabbedPane.add (new ActivityTabPanel(subsetModel));				
			} else {
				this.tabbedPane.add(new ActivityTabPanel(subsetModel), position);
			}
		}*/
	}

	/**
	 * @return the paletteModel
	 */
	public synchronized final ActivityPaletteModel getPaletteModel() {
		return this.paletteModel;
	}

	private ActivityTabPanel getCurrentTab() {
		ActivityTabPanel result = null;
		Component selectedTab = this.tabbedPane.getSelectedComponent();
		if (selectedTab instanceof ActivityTabPanel) {
			result = (ActivityTabPanel) selectedTab;
		}
		return result;
	}
	
	private void expandTab() {
		ActivityTabPanel currentTab = getCurrentTab();
		if (currentTab != null) {
			currentTab.expandAll();
		}		
	}
	
	private void collapseTab() {
		ActivityTabPanel currentTab = getCurrentTab();
		if (currentTab != null) {
			currentTab.collapseAll();
		}		
	}
	
	private void showAddScavengerPopup(Component c, int px, int py) {
		JPopupMenu scavengerAdderPopupMenu = new ScavengerAdderPopupMenu(this);
		scavengerAdderPopupMenu.show(c,px,py);
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

}
