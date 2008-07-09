package org.myexp_whip_plugin.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.actions.OpenWorkflowFromFileAction;
import org.embl.ebi.escience.scuflui.actions.PasswordInput;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.myexp_whip_plugin.MyExperimentClient;

public class MainComponent extends JSplitPane implements WorkflowModelViewSPI {

	private static final long serialVersionUID = 1L;
	
	private URL baseUrl;

	private final Logger logger = Logger.getLogger(MainComponent.class);

	private ScuflModel model;
	
	private MyExperimentClient client;
	
	private JTabbedPane tabsPane;
	
	private ExampleWorkflowsPanel exampleWorkflowsPanel;
	
	private LatestWorkflowsPanel latestWorkflowsPanel;
	
	private SearchWorkflowsPanel searchWorkflowsPanel;
	
	private TagsBrowserPanel tagsBrowserPanel;
	
	private WorkflowPreviewPanel workflowPreviewPanel;
	
	public MainComponent() {
		super();
		
		try {
			this.baseUrl = new URL("http://sandbox.myexperiment.org/");
			this.client = new MyExperimentClient(this.logger, this.baseUrl);
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
		
		this.exampleWorkflowsPanel = new ExampleWorkflowsPanel(this, this.client, this.logger);
		this.latestWorkflowsPanel = new LatestWorkflowsPanel(this, this.client, this.logger);
		this.searchWorkflowsPanel = new SearchWorkflowsPanel(this, this.client, this.logger);
		this.tagsBrowserPanel = new TagsBrowserPanel(this, this.client, this.logger);
		this.workflowPreviewPanel = new WorkflowPreviewPanel(this, this.client, this.logger);
		
		this.tabsPane.add("Example Workflows", this.exampleWorkflowsPanel);
		this.tabsPane.add("Latest Workflows", this.latestWorkflowsPanel);
		this.tabsPane.add("Search Workflows", this.searchWorkflowsPanel);
		this.tabsPane.add("Tags Browser", this.tagsBrowserPanel);
		
		this.setLeftComponent(this.tabsPane);
		this.setRightComponent(this.workflowPreviewPanel);
		
		this.setResizeWeight(0.5);
		this.setContinuousLayout(true);
	}
	
	private void initialiseData() {
		this.logger.debug("Initialising myExperiment Perspective data");
		
		this.latestWorkflowsPanel.refresh();
		this.tagsBrowserPanel.refresh();
		this.exampleWorkflowsPanel.refresh();
	}
	
	public void browseTag(String tagName) {
		this.tabsPane.setSelectedComponent(this.tagsBrowserPanel);
		this.tagsBrowserPanel.setTag(tagName);
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
        	workflowPreviewPanel.setWorkfowId(this.workflowId);            
        }
	}
	
	public class LoadWorkflowAction extends AbstractAction {
        private int workflowId = 0;
		
		public LoadWorkflowAction(int workflowId) {
                putValue(SMALL_ICON, TavernaIcons.importIcon);
                putValue(NAME,"Open");
                putValue(SHORT_DESCRIPTION,"Download and open this workflow in Design mode");
                
                this.workflowId = workflowId;
        }
   
        public void actionPerformed(ActionEvent actionEvent) {
        	try {
        		URL url = new URL(baseUrl, "workflows/" + this.workflowId + "/download");
        		
        		logger.debug("Downloading and opening workflow from URL: " + url.toString());
        		
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setRequestProperty("Accept", "text/xml");
				OpenWorkflowFromFileAction action = new OpenWorkflowFromFileAction(MainComponent.this);
				
                if (conn.getResponseCode() == 401) { //authentication required.
                        PasswordInput input =  new PasswordInput((JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, MainComponent.this));
                        input.setUrl(url);
                        input.setSize(new Dimension(323,222));
                        input.setLocationRelativeTo(MainComponent.this);
                        input.setVisible(true);
                        
                        if (input.getPassword()!=null && input.getUsername()!=null) {
                                conn = (HttpURLConnection)url.openConnection();
                                String userPassword = input.getUsername()+":"+input.getPassword();
                                String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
                                conn.setRequestProperty("Authorization", "Basic " + encoding);
                                conn.setRequestProperty("Accept", "text/xml");
                                action.openFromURL(conn);
                        }
                } else {
                		action.openFromURL(conn);
                }
				
			} catch (IOException e) {
				logger.error("Failed to open connection to URL to download and open workflow, from myExperiment.");
			}
        }
	}
}
