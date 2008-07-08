package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.myexp_whip_plugin.MyExperimentClient;
import org.myexp_whip_plugin.SearchResults;
import org.myexp_whip_plugin.TagCloud;

import edu.stanford.ejalbert.BrowserLauncher;

public class TagsBrowserPanel extends BasePanel implements ActionListener, ChangeListener, HyperlinkListener {
	
	private static final String ACTION_REFRESH_CLOUD = "refresh_cloud_tags_browser";
	private static final String ACTION_REFRESH_RESULTS = "refresh_results_tags_browser";
	private static final String ACTION_CLEAR_RESULTS = "clear_results_tags_browser";
	
	private String currentTagName = "";
	
	private int cloudSize = -1;
	
	private TagCloud tagCloudData = new TagCloud();
	
	private SearchResults tagSearchResults = new SearchResults();
	
	private JSplitPane mainSplitPane;
	
	private JLabel cloudStatusLabel;
	private JSlider cloudSizeSlider;
	private JCheckBox cloudAllCheckBox;
	private JButton cloudRefreshButton;
	private JScrollPane cloudScrollPane;
	private JTextPane cloudTextPane;
	
	private JLabel resultsStatusLabel;
	private JButton resultsClearButton;
	private JButton resultsRefreshButton;
	private WorkflowsListPanel workflowsListPanel;
	
	public TagsBrowserPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		super(parent, client, logger);
		
		this.initialiseUI();
		
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	mainSplitPane.setDividerLocation(0.5);
		    	mainSplitPane.setOneTouchExpandable(true);
				mainSplitPane.setDoubleBuffered(true);
		    }
		});
	}
	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == this.cloudSizeSlider) {
			JSlider source = (JSlider)event.getSource();
		    if (!source.getValueIsAdjusting()) {
		        this.cloudSize = source.getValue();
		    }
		}
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				//BrowserLauncher launcher = new BrowserLauncher();
				//launcher.openURLinBrowser(e.getURL().toString());
			}
		} catch (Exception ex) {
			logger.error("Error occurred whilst clicking a hyperlink", ex);
		}
	}
	
	public void setTag(String tagName) {
		this.currentTagName = tagName;
		
		this.refreshResults();
	}

	public void refresh() {
		/*
		this.searchKeywords = this.searchTextField.getText();
		
		if (this.searchKeywords != null && !this.searchKeywords.equalsIgnoreCase("")) {
			this.statusLabel.setText("Searching for workflows from myExperiment...");
			
			// Make call to myExperiment API in a different thread
			// (then use SwingUtilities.invokeLater to update the UI when ready).
			new Thread("Perform search for SearchWorkflowsPanel") {
				public void run() {
					logger.debug("Performing search for Search Workflows tab");

					try {
						results = client.searchWorkflows(searchKeywords);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								repopulate();
							}
						});
					} catch (Exception ex) {
						logger.error("Failed to search for workflows from myExperiment", ex);
					}
				}
			}.start();
		}
		else {
			this.statusLabel.setText("Please enter valid keyword(s)");
		}
		*/
	}
	
	public void refreshCloud() {
		
	}
	
	public void refreshResults() {
		
	}

	public void repopulate() {
		
		
		this.revalidate();
		
	}
	
	public void repopulateCloud() {
		
	}
	
	public void repopulateResults() {
		logger.debug("Repopulating Search Workflows tab");

		this.resultsStatusLabel.setText(this.tagSearchResults.getWorkflows().size() + " workflows found for tag '" + this.currentTagName + "'");
		
		this.workflowsListPanel.setWorkflows(this.tagSearchResults.getWorkflows());
		
		this.resultsClearButton.setEnabled(true);
		this.resultsRefreshButton.setEnabled(true);
	}
	
	public void clear() {
		this.resultsStatusLabel.setText("");
		this.resultsClearButton.setEnabled(false);
		this.resultsRefreshButton.setEnabled(false);
		this.workflowsListPanel.clear();
	}
	
	private void initialiseUI() {
		this.setLayout(new BorderLayout());
		
		this.mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		
		JPanel cloudStatusPanel = new JPanel(new BorderLayout());
		cloudStatusPanel.setBorder(BorderFactory.createEtchedBorder());
		
		this.cloudStatusLabel = new JLabel();
		cloudStatusPanel.add(this.cloudStatusLabel, BorderLayout.CENTER);
		
		JPanel cloudConfigPanel = new JPanel();
		cloudConfigPanel.setLayout(new BoxLayout(cloudConfigPanel, BoxLayout.LINE_AXIS));
		this.cloudSizeSlider = new JSlider(1, 100, 50);
		this.cloudSizeSlider.addChangeListener(this);
		this.cloudSizeSlider.setToolTipText("Drag the slider to select how big the tag cloud should be, or check the \"All tags\" box to get the full tag cloud.");
		cloudConfigPanel.add(this.cloudSizeSlider);
		this.cloudAllCheckBox = new JCheckBox("All tags", true);
		cloudConfigPanel.add(this.cloudAllCheckBox);
		this.cloudRefreshButton = new JButton("Refresh", TavernaIcons.refreshIcon);
		this.cloudRefreshButton.setActionCommand(ACTION_REFRESH_CLOUD);
		this.cloudRefreshButton.addActionListener(this);
		this.cloudRefreshButton.setToolTipText("Click this button to refresh the Tag Cloud");
		cloudConfigPanel.add(this.cloudRefreshButton);
		
		cloudStatusPanel.add(cloudConfigPanel, BorderLayout.EAST);
		
		topPanel.add(cloudStatusPanel, BorderLayout.NORTH);
		
		this.cloudTextPane = new JTextPane();
		this.cloudTextPane.setBorder(BorderFactory.createEmptyBorder());
		this.cloudTextPane.setEditable(false);
		this.cloudScrollPane = new JScrollPane(this.cloudTextPane,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.cloudScrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.cloudScrollPane.setOpaque(true);
		topPanel.add(this.cloudScrollPane, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		
		JPanel resultsStatusPanel = new JPanel(new BorderLayout());
		resultsStatusPanel.setBorder(BorderFactory.createEtchedBorder());
		
		this.resultsStatusLabel = new JLabel();
		resultsStatusPanel.add(this.resultsStatusLabel, BorderLayout.CENTER);
		
		JPanel resultsButtonsPanel = new JPanel();
		resultsButtonsPanel.setLayout(new BoxLayout(resultsButtonsPanel, BoxLayout.LINE_AXIS));
		this.resultsClearButton = new JButton("Clear", TavernaIcons.deleteIcon);
		this.resultsClearButton.setActionCommand(ACTION_CLEAR_RESULTS);
		this.resultsClearButton.addActionListener(this);
		this.resultsClearButton.setToolTipText("Click this button to clear the tag results");
		this.resultsClearButton.setEnabled(false);
		resultsButtonsPanel.add(this.resultsClearButton);
		this.resultsRefreshButton = new JButton("Refresh", TavernaIcons.refreshIcon);
		this.resultsRefreshButton.setActionCommand(ACTION_REFRESH_RESULTS);
		this.resultsRefreshButton.addActionListener(this);
		this.resultsRefreshButton.setToolTipText("Click this button to refresh the tag results");
		this.resultsRefreshButton.setEnabled(false);
		resultsButtonsPanel.add(this.resultsRefreshButton);
		
		resultsStatusPanel.add(resultsButtonsPanel, BorderLayout.EAST);
		
		bottomPanel.add(resultsStatusPanel, BorderLayout.NORTH);
		
		this.workflowsListPanel = new WorkflowsListPanel(this.parent, this.client, this.logger);
		
		bottomPanel.add(this.workflowsListPanel, BorderLayout.CENTER);
		
		this.mainSplitPane.setTopComponent(topPanel);
		this.mainSplitPane.setBottomComponent(bottomPanel);
		
		this.add(this.mainSplitPane, BorderLayout.CENTER);
	}
}
