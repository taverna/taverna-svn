package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.myexp_whip_plugin.MyExperimentClient;

import edu.stanford.ejalbert.BrowserLauncher;

public class WorkflowPreviewPanel extends JPanel implements ActionListener, ChangeListener, HyperlinkListener {
	
	private static final String ACTION_REFRESH = "refresh_workflow_preview";
	private static final String ACTION_CLEAR = "clear_workflow_preview";
	
	private MainComponent parent;
	
	private MyExperimentClient client;
	
	private Logger logger;
	
	private int workflowId = 0;
	
	private JLabel statusLabel;
	
	private JButton refreshButton;
	private JButton clearButton;
	
	private JScrollPane contentScrollPane; 
	private JPanel contentPanel;
	
	public WorkflowPreviewPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		this.parent = parent;
		this.client = client;
		this.logger = logger;
		
		this.initialiseUI();
	}
	
	public void actionPerformed(ActionEvent event) {
		if (ACTION_REFRESH.equals(event.getActionCommand())) {
			this.refresh();
		}
		else if (ACTION_CLEAR.equals(event.getActionCommand())) {
			this.clear();
		}
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
	
	@Override
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
	
	public void setWorkfowId(int id) {
		this.workflowId = id;
		
		this.refresh();
	}
	
	public void refresh() {
		if (this.workflowId > 0) {
			this.statusLabel.setText("Fetching workflow information from myExperiment...");
			
			// Make call to myExperiment API in a different thread
			// (then use SwingUtilities.invokeLater to update the UI when ready).
			new Thread("Refresh for WorkflowPreviewPanel") {
				public void run() {
					logger.debug("Refreshing Workflow Preview pane");

					try {
						//workflows = client.getLatestWorkflows();

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								repopulate();
							}
						});
					} catch (Exception ex) {
						logger.error("Failed to fetch workflow data from myExperiment", ex);
					}
				}
			}.start();
		}
		else {
			this.statusLabel.setText("");
			
			this.clearButton.setEnabled(false);
			this.refreshButton.setEnabled(false);
			
			this.contentPanel.setVisible(false);
		}
	}
	
	public void repopulate() {
		logger.debug("Repopulating Workflow Preview pane");
		
		
		
		this.clearButton.setEnabled(true);
		this.refreshButton.setEnabled(true);
		
		this.contentPanel.setVisible(true);
		
		
		this.revalidate();
	}
	
	public void clear() {
		this.setWorkfowId(0);
	}
	
	private void initialiseUI() {
		this.setLayout(new BorderLayout());
		
		this.add(new ShadedLabel("Workflow Preview", ShadedLabel.TAVERNA_BLUE), BorderLayout.NORTH);
		
		JPanel middlePanel = new JPanel(new BorderLayout());
		this.add(middlePanel, BorderLayout.CENTER);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEtchedBorder());
		
		this.statusLabel = new JLabel();
		topPanel.add(this.statusLabel, BorderLayout.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		this.clearButton = new JButton("Clear", TavernaIcons.deleteIcon);
		this.clearButton.setActionCommand(ACTION_CLEAR);
		this.clearButton.addActionListener(this);
		this.clearButton.setToolTipText("Click this button to clear the Preview Workflow pane");
		this.clearButton.setEnabled(false);
		buttonsPanel.add(this.clearButton);
		this.refreshButton = new JButton("Refresh", TavernaIcons.refreshIcon);
		this.refreshButton.setActionCommand(ACTION_REFRESH);
		this.refreshButton.addActionListener(this);
		this.refreshButton.setToolTipText("Click this button to refresh the Preview Workflow pane");
		this.refreshButton.setEnabled(false);
		buttonsPanel.add(this.refreshButton);
		
		topPanel.add(buttonsPanel, BorderLayout.EAST);
		
		middlePanel.add(topPanel, BorderLayout.NORTH);
		
		this.contentPanel = new JPanel();
		this.contentPanel.setVisible(false);
		this.contentScrollPane = new JScrollPane(this.contentPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		middlePanel.add(this.contentScrollPane, BorderLayout.CENTER);
	}
}
