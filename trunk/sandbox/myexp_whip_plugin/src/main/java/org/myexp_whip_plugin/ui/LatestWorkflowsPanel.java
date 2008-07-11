package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import org.myexp_whip_plugin.Workflow;

import com.sun.syndication.feed.synd.SyndEntry;

import edu.stanford.ejalbert.BrowserLauncher;

public class LatestWorkflowsPanel extends BasePanel implements ActionListener, ChangeListener {
	
	private static final String ACTION_REFRESH = "refresh_latest_workflows";

	private JLabel statusLabel;
	private JButton refreshButton;
	
	private List<Workflow> workflows = new ArrayList<Workflow>(); 
	
	private WorkflowsListPanel workflowsListPanel;

	public LatestWorkflowsPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		super(parent, client, logger);
		
		this.initialiseUI();
	}
	
	public void actionPerformed(ActionEvent event) {
		if (ACTION_REFRESH.equals(event.getActionCommand())) {
			this.refresh();
		}
	}

	public void stateChanged(ChangeEvent event) {

	}

	public void refresh() {
		this.statusLabel.setText("Fetching latest workflows from myExperiment...");
		
		// Make call to myExperiment API in a different thread
		// (then use SwingUtilities.invokeLater to update the UI when ready).
		new Thread("Refresh for LatestWorkflowsPanel") {
			public void run() {
				logger.debug("Refreshing Latest Workflows tab");

				try {
					workflows = client.getLatestWorkflows();

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							repopulate();
						}
					});
				} catch (Exception ex) {
					logger.error("Failed to fetch Latest Workflows from myExperiment", ex);
				}
			}
		}.start();
	}

	public void repopulate() {
		logger.debug("Repopulating Latest Workflows tab");

		this.statusLabel.setText(this.workflows.size() + " latest workflows found");
		
		this.workflowsListPanel.setWorkflows(this.workflows);
		
		this.revalidate();
	}
	
	public void clear() {
		this.statusLabel.setText("");
		this.workflowsListPanel.clear();
	}
	
	private void initialiseUI() {
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEtchedBorder());
		this.statusLabel = new JLabel();
		topPanel.add(this.statusLabel, BorderLayout.CENTER);
		this.refreshButton = new JButton("Refresh", TavernaIcons.refreshIcon);
		this.refreshButton.setActionCommand(ACTION_REFRESH);
		this.refreshButton.addActionListener(this);
		this.refreshButton.setToolTipText("Click this button to refresh the Latest Workflows list");
		topPanel.add(this.refreshButton, BorderLayout.EAST);
		this.add(topPanel, BorderLayout.NORTH);
		
		this.workflowsListPanel = new WorkflowsListPanel(this.parent, this.client, this.logger);
		
		this.add(this.workflowsListPanel, BorderLayout.CENTER);
	}
}
