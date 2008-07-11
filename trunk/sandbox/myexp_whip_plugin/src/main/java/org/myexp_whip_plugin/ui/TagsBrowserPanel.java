package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.myexp_whip_plugin.MyExperimentClient;
import org.myexp_whip_plugin.SearchResults;
import org.myexp_whip_plugin.Tag;
import org.myexp_whip_plugin.TagCloud;

public class TagsBrowserPanel extends BasePanel implements ActionListener, ChangeListener, HyperlinkListener {
	
	private static final String ACTION_REFRESH_CLOUD = "refresh_cloud_tags_browser";
	private static final String ACTION_REFRESH_RESULTS = "refresh_results_tags_browser";
	private static final String ACTION_CLEAR_RESULTS = "clear_results_tags_browser";
	
	private static final int TAGCLOUD_MAX_FONTSIZE = 36;
	private static final int TAGCLOUD_MIN_FONTSIZE = 12;
	
	private String currentTagName = "";
	
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
		if (ACTION_REFRESH_CLOUD.equals(event.getActionCommand())) {
			this.refreshCloud();
		}
		else if (ACTION_CLEAR_RESULTS.equals(event.getActionCommand())) {
			this.clearResults();
		}
		else if (ACTION_REFRESH_RESULTS.equals(event.getActionCommand())) {
			this.refreshResults();
		}
	}
	
	public void stateChanged(ChangeEvent event) {

	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getSource() == this.cloudTextPane) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					logger.debug("Tag clicked: " + e.getURL());
					String [] s = e.getURL().toString().split("/");
					this.currentTagName = s[s.length-1];
					
					this.refreshResults();
				}
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
		this.refreshCloud();
		this.refreshResults();
	}
	
	public void refreshCloud() {
		this.cloudStatusLabel.setText("Building tag cloud...");
		
		// Make call to myExperiment API in a different thread
		// (then use SwingUtilities.invokeLater to update the UI when ready).
		new Thread("Get tag cloud data for TagsBrowserPanel") {
			public void run() {
				logger.debug("Getting tag cloud data for TagsBrowserPanel");

				try {
					int size = -1;
					if (!cloudAllCheckBox.isSelected()) {
						size = cloudSizeSlider.getValue();
					}
					tagCloudData = client.getTagCloud(size);

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							repopulateCloud();
						}
					});
				} catch (Exception ex) {
					logger.error("Failed to get tag cloud data from myExperiment", ex);
				}
			}
		}.start();
	}
	
	public void refreshResults() {
		if (this.currentTagName != null && !this.currentTagName.equals("")) {
			this.resultsStatusLabel.setText("Searching for workflows with tag '" + this.currentTagName + "' from myExperiment...");
			
			// Make call to myExperiment API in a different thread
			// (then use SwingUtilities.invokeLater to update the UI when ready).
			new Thread("Perform tag search for TagsBrowserPanel") {
				public void run() {
					logger.debug("Performing tag search for Tags Browser tab");

					try {
						tagSearchResults = client.getTagResults(currentTagName);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								repopulateResults();
							}
						});
					} catch (Exception ex) {
						logger.error("Failed to get tag results from myExperiment", ex);
					}
				}
			}.start();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					clearResults();
				}
			});
		}
	}

	public void repopulate() {
		this.repopulateCloud();
		this.repopulateResults();
	}
	
	public void repopulateCloud() {
		logger.debug("Repopulating tag cloud");
		
		this.cloudStatusLabel.setText(this.tagCloudData.getTags().size() + " tags found");
		
		try {
			int maxCount = this.getMaxCountOfTags();
			
			StringBuffer content = new StringBuffer();
			content.append("<div class='outer'>");
			content.append("<br/>");
			content.append("<div class='tag_cloud'>");
			
			for (Tag t : this.tagCloudData.getTags()) {
				// Normalise count and use it to obtain a font size value. 
				// Also chops off based on min and max.
				int fontSize = (int) (((double)t.getCount()/(maxCount/4))*TAGCLOUD_MAX_FONTSIZE);
				if (fontSize < TAGCLOUD_MIN_FONTSIZE) {
					fontSize = TAGCLOUD_MIN_FONTSIZE;
				}
				if (fontSize > TAGCLOUD_MAX_FONTSIZE) {
					fontSize = TAGCLOUD_MAX_FONTSIZE;
				}
				
				content.append("<a style='font-size: " + fontSize + "pt;' href='http://tag/" + t.getTagName() + "'>" + t.getTagName() + "</a>");
				content.append("&nbsp;&nbsp;&nbsp;");
			}
			
			content.append("<br/>");
			content.append("</div>");
			content.append("</div>");
			
			HTMLEditorKit kit = new HTMLEditorKit();
			HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());

			kit.setStyleSheet(this.parent.getStyleSheet());
			
			doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());
			
			this.cloudTextPane.setEditorKit(kit);
			this.cloudTextPane.setDocument(doc);
		}
		catch (Exception e) {
			logger.error("Failed to populate tag cloud", e);
		}
		
		this.revalidate();
	}
	
	public void repopulateResults() {
		logger.debug("Repopulating tag results pane");

		this.resultsStatusLabel.setText(this.tagSearchResults.getWorkflows().size() + " workflows found for tag '" + this.currentTagName + "'");
		
		this.workflowsListPanel.setWorkflows(this.tagSearchResults.getWorkflows());
		
		this.resultsClearButton.setEnabled(true);
		this.resultsRefreshButton.setEnabled(true);
		
		this.revalidate();
	}
	
	public void clear() {
		this.clearCloud();
		this.clearResults();
	}
	
	public void clearCloud() {
		this.cloudStatusLabel.setText("");
		this.cloudTextPane.setDocument(new HTMLDocument());
		this.revalidate();
	}
	
	public void clearResults() {
		this.resultsStatusLabel.setText("");
		this.resultsClearButton.setEnabled(false);
		this.resultsRefreshButton.setEnabled(false);
		this.workflowsListPanel.clear();
		this.revalidate();
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
		//this.cloudSizeSlider.addChangeListener(this);
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
		this.cloudTextPane.setContentType("text/html");
		this.cloudTextPane.addHyperlinkListener(this);
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
	
	private int getMaxCountOfTags() {
		int max = 0;
		
		for (Tag t : this.tagCloudData.getTags()) {
			if (t.getCount() > max) {
				max = t.getCount();
			}
		}
		
		return max;
	}
}
