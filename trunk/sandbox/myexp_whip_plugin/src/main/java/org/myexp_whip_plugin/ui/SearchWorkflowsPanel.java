package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.KeyStore;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.myexp_whip_plugin.MyExperimentClient;
import org.myexp_whip_plugin.Resource;
import org.myexp_whip_plugin.SearchResults;
import org.myexp_whip_plugin.ui.MainComponent.LoadWorkflowAction;
import org.myexp_whip_plugin.ui.MainComponent.PreviewWorkflowAction;

import edu.stanford.ejalbert.BrowserLauncher;

public class SearchWorkflowsPanel extends BasePanel implements ActionListener, ChangeListener, KeyListener {
	
	private static final String ACTION_SEARCH = "search_workflows_search";
	private static final String ACTION_CLEAR = "clear_workflows_search";
	private static final String ACTION_REFRESH = "refresh_workflows_search";
	
	private String searchKeywords = "";
	private SearchResults results = new SearchResults();
	
	private JTextField searchTextField;
	private JButton searchButton;
	
	private JLabel statusLabel;
	private JButton clearButton;
	private JButton refreshButton;
	
	private WorkflowsListPanel workflowsListPanel;
	
	public SearchWorkflowsPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		super(parent, client, logger);
		
		this.initialiseUI();
	}
	
	public void actionPerformed(ActionEvent event) {
		if (ACTION_SEARCH.equals(event.getActionCommand())) {
			this.performSearch();
		}
		else if (ACTION_CLEAR.equals(event.getActionCommand())) {
			this.clear();
		}
		else if (ACTION_REFRESH.equals(event.getActionCommand())) {
			this.refresh();
		}
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getComponent() == this.searchTextField && e.getKeyCode() == KeyEvent.VK_ENTER) {
			this.performSearch();
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

	public void keyTyped(KeyEvent e) {
		
	}
	
	public void setSearchKeywords(String keywords) {
		this.searchTextField.setText(keywords);
		
		this.performSearch();
	}
	
	private void performSearch() {
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
	}
	
	public void refresh() {
		this.performSearch();
	}
	
	public void repopulate() {
		logger.debug("Repopulating Search Workflows tab");

		this.statusLabel.setText(this.results.getWorkflows().size() + " workflows found for '" + this.searchKeywords + "'");
		
		this.workflowsListPanel.setWorkflows(this.results.getWorkflows());
		
		this.clearButton.setEnabled(true);
		this.refreshButton.setEnabled(true);
		
		this.revalidate();
	}
	
	public void clear() {
		this.statusLabel.setText("");
		this.clearButton.setEnabled(false);
		this.refreshButton.setEnabled(false);
		this.workflowsListPanel.clear();
	}
	
	private void initialiseUI() {
		this.setLayout(new BorderLayout());
		
		JPanel searchBoxPanel = new JPanel(new BorderLayout());
		searchBoxPanel.setBorder(BorderFactory.createEtchedBorder());
		
		this.searchTextField = new JTextField();
		this.searchTextField.addKeyListener(this);
		searchBoxPanel.add(this.searchTextField, BorderLayout.CENTER);
		
		this.searchButton = new JButton("Search", TavernaIcons.searchIcon);
		this.searchButton.setActionCommand(ACTION_SEARCH);
		this.searchButton.addActionListener(this);
		searchBoxPanel.add(this.searchButton, BorderLayout.EAST);
		
		this.add(searchBoxPanel, BorderLayout.NORTH);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.setBorder(BorderFactory.createEtchedBorder());
		
		this.statusLabel = new JLabel();
		statusPanel.add(this.statusLabel, BorderLayout.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		this.clearButton = new JButton("Clear", TavernaIcons.deleteIcon);
		this.clearButton.setActionCommand(ACTION_CLEAR);
		this.clearButton.addActionListener(this);
		this.clearButton.setToolTipText("Click this button to clear any search results");
		this.clearButton.setEnabled(false);
		buttonsPanel.add(this.clearButton);
		this.refreshButton = new JButton("Refresh", TavernaIcons.refreshIcon);
		this.refreshButton.setActionCommand(ACTION_REFRESH);
		this.refreshButton.addActionListener(this);
		this.refreshButton.setToolTipText("Click this button to refresh the search results");
		this.refreshButton.setEnabled(false);
		buttonsPanel.add(this.refreshButton);
		
		statusPanel.add(buttonsPanel, BorderLayout.EAST);
		
		mainPanel.add(statusPanel, BorderLayout.NORTH);
		
		this.workflowsListPanel = new WorkflowsListPanel(this.parent, this.client, this.logger);
		
		mainPanel.add(this.workflowsListPanel, BorderLayout.CENTER);
		
		this.add(mainPanel, BorderLayout.CENTER);
	}
}
