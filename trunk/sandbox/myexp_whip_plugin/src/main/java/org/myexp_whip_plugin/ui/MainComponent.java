package org.myexp_whip_plugin.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.myexp_whip_plugin.MyExperimentClient;

public class MainComponent extends JSplitPane implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 1L;

	private final Logger logger = Logger.getLogger(MainComponent.class);

	private ScuflModel model;
	
	private MyExperimentClient client;
	
	private JTabbedPane tabsPane;
	
	private LatestWorkflowsPanel latestWorkflowsPanel;
	
	private SearchWorkflowsPanel searchWorkflowsPanel;
	
	private TagsBrowserPanel tagsBrowserPanel;
	
	private CurrentWorkflowPanel currentWorkflowPanel;
	
	public MainComponent() {
		super();
		
		try {
			this.client = new MyExperimentClient(new URL("http://sandbox.myexperiment.org/"));
		} catch (MalformedURLException e) {
			this.logger.debug("Failed to set baseUrl for myExperimentClient");
		}
		
		initialiseUI();
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	setOneTouchExpandable(true);
				setDividerLocation(0.6);
				setDoubleBuffered(true);
		    }
		});
		
		// Do the rest in a separate thread to avoid hanging the GUI.
		// Remember to use SwingUtilities.invokeLater to update the GUI directly.
		new Thread("Data initialisation for myExp/WHIP plugin") {
			public void run() {
				initialiseData();						
			}
		}.start();
	}
	
	public void attachToModel(ScuflModel model) {
		this.model = model;
	}

	public void detachFromModel() {

	}

	public ImageIcon getIcon() {
		return null;
	}

	public void onDisplay() {

	}

	public void onDispose() {

	}
	
	//public void componentResized(ComponentEvent e) {
	//	this.latestWorkflowsPanel.revalidateScrollPane();
	//}

	private void initialiseUI() {
		this.logger.debug("Initialising myExperiment Perspective UI components");
		
		this.tabsPane = new JTabbedPane();
		
		this.latestWorkflowsPanel = new LatestWorkflowsPanel(this, this.client, this.logger);
		JScrollPane leftScrollPane1 = new JScrollPane(this.latestWorkflowsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane1.setBorder(BorderFactory.createEtchedBorder());
		
		this.searchWorkflowsPanel = new SearchWorkflowsPanel(this.client, this.logger);
		JScrollPane leftScrollPane2 = new JScrollPane(this.searchWorkflowsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane2.setBorder(BorderFactory.createEtchedBorder());
		
		this.tagsBrowserPanel = new TagsBrowserPanel(this.client, this.logger);
		JScrollPane leftScrollPane3 = new JScrollPane(this.tagsBrowserPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		leftScrollPane3.setBorder(BorderFactory.createEtchedBorder());
		
		this.tabsPane.add("Latest Workflows", this.latestWorkflowsPanel);
		this.tabsPane.add("Search Workflows", this.searchWorkflowsPanel);
		this.tabsPane.add("Tags Browser", this.tagsBrowserPanel);
		
		this.currentWorkflowPanel = new CurrentWorkflowPanel(this.client, this.logger);
		JScrollPane rightScrollPane = new JScrollPane(this.currentWorkflowPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightScrollPane.setBorder(BorderFactory.createEtchedBorder());
		
		this.setLeftComponent(this.tabsPane);
		this.setRightComponent(rightScrollPane);
		
		this.setResizeWeight(0.5);
		this.setContinuousLayout(true);
	}
	
	private void initialiseData() {
		this.logger.debug("Initialising myExperiment Perspective data");
		
		this.latestWorkflowsPanel.refresh();
	}
	
	public class PreviewWorkflowAction extends AbstractAction {
        private int workflowId = 0;
		
		public PreviewWorkflowAction(int workflowId) {
                putValue(SMALL_ICON, TavernaIcons.zoomIcon);
                putValue(NAME,"Preview");
                putValue(SHORT_DESCRIPTION,"Preview this workflow in the Workflow Preview pane");
                
                this.workflowId = workflowId;
        }
   
        public void actionPerformed(ActionEvent actionEvent) {
                        
        }
	}
	
	public class LoadWorkflowAction extends AbstractAction {
        private int workflowId = 0;
		
		public LoadWorkflowAction(int workflowId) {
                putValue(SMALL_ICON, TavernaIcons.importIcon);
                putValue(NAME,"Load");
                putValue(SHORT_DESCRIPTION,"Download and load this workflow in Taverna");
                
                this.workflowId = workflowId;
        }
   
        public void actionPerformed(ActionEvent actionEvent) {
                        
        }
	}
}
