package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.myexp_whip_plugin.MyExperimentClient;
import org.myexp_whip_plugin.Resource;

import edu.stanford.ejalbert.BrowserLauncher;

public class ExampleWorkflowsPanel extends BasePanel implements ActionListener, ChangeListener, HyperlinkListener {
	
	private static final String ACTION_REFRESH = "refresh_example_workflows";
	
	private JLabel statusLabel;
	private JButton refreshButton;
	
	private List<Resource> workflows = new ArrayList<Resource>(); 
	
	private WorkflowsListPanel workflowsListPanel;
	
	public ExampleWorkflowsPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
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
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				BrowserLauncher launcher = new BrowserLauncher();
				launcher.openURLinBrowser(e.getURL().toString());
			}
		} catch (Exception ex) {
			logger.error("Error occurred whilst clicking a hyperlink", ex);
		}
	}

	public void clear() {
		this.workflowsListPanel.clear();
	}

	public void refresh() {
		this.statusLabel.setText("Fetching example workflows from myExperiment...");
		
		// Make call to myExperiment API in a different thread
		// (then use SwingUtilities.invokeLater to update the UI when ready).
		new Thread("Refresh for ExampleWorkflowsPanel") {
			public void run() {
				logger.debug("Refreshing Example Workflows tab");

				try {
					workflows = client.getExampleWorkflows();

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							repopulate();
						}
					});
				} catch (Exception ex) {
					logger.error("Failed to refresh Example Workflows panel", ex);
				}
			}
		}.start();
		
	}

	public void repopulate() {
		logger.debug("Repopulating Example Workflows tab");

		this.statusLabel.setText(this.workflows.size() + " example workflows found");
		
		this.workflowsListPanel.setWorkflows(this.workflows);
		
		this.revalidate();
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
		this.refreshButton.setToolTipText("Click this button to refresh the Example Workflows list");
		topPanel.add(this.refreshButton, BorderLayout.EAST);
		this.add(topPanel, BorderLayout.NORTH);
		
		this.workflowsListPanel = new WorkflowsListPanel(this.parent, this.client, this.logger);
		
		this.add(this.workflowsListPanel, BorderLayout.CENTER);
	}
}

