package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

import org.myexp_whip_plugin.*;

public class MainComponent extends JSplitPane implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MainComponent.class);

	private ScuflModel model;
	
	private MyExperimentClient client;
	
	private JTabbedPane tabsPane;
	
	private LatestWorkflowsPanel latestWorkflowsPanel;
	
	private SearchWorkflowsPanel searchWorkflowsPanel;
	
	private TagsBrowserPanel tagsBrowserPanel;
	
	private CurrentWorkflowPanel currentWorkflowPanel;
	
	public MainComponent() {
		super();
		
		this.client = new MyExperimentClient();
		
		this.setMaximumSize(new Dimension(500, 700));
		
		// Do the rest in a separate thread to avoid hanging the GUI
		new Thread() {
			public void run() {
				initialise();						
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

	private void initialise() {
		this.tabsPane = new JTabbedPane();
		this.latestWorkflowsPanel = new LatestWorkflowsPanel(this.client);
		this.searchWorkflowsPanel = new SearchWorkflowsPanel(this.client);
		this.tagsBrowserPanel = new TagsBrowserPanel(this.client);
		this.tabsPane.add("Latest Workflows", this.latestWorkflowsPanel);
		this.tabsPane.add("Search Workflows", this.searchWorkflowsPanel);
		this.tabsPane.add("Tags Browser", this.tagsBrowserPanel);
		
		this.currentWorkflowPanel = new CurrentWorkflowPanel(this.client);
		
		this.setLeftComponent(this.tabsPane);
		this.setRightComponent(this.currentWorkflowPanel);
		
		this.setDividerLocation(0.6);
	}
}
