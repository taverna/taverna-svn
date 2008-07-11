package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

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
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.myexp_whip_plugin.MyExperimentClient;
import org.myexp_whip_plugin.Tag;
import org.myexp_whip_plugin.User;
import org.myexp_whip_plugin.Workflow;

import edu.stanford.ejalbert.BrowserLauncher;

public class WorkflowPreviewPanel extends BasePanel implements ActionListener, ChangeListener, HyperlinkListener {
	
	private static final String ACTION_SYNC = "sync_workflow_preview";
	private static final String ACTION_REFRESH = "refresh_workflow_preview";
	private static final String ACTION_CLEAR = "clear_workflow_preview";

	private int currentWorkflowId = 0;
	
	private Workflow currentWorkflow = null;
	
	private JLabel statusLabel;
	
	private JButton refreshButton;
	private JButton clearButton;
	
	private JScrollPane contentScrollPane; 
	private JPanel contentPanel;
	private JTextPane contentTextPane;
	
	private JButton syncButton;
	private JButton loadButton;
	private JButton importButton;
	
	public WorkflowPreviewPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		super(parent, client, logger);
		
		this.initialiseUI();
		
		//this.populateDummy();
	}
	
	public void actionPerformed(ActionEvent event) {
		if (ACTION_REFRESH.equals(event.getActionCommand())) {
			this.refresh();
		}
		else if (ACTION_CLEAR.equals(event.getActionCommand())) {
			this.clear();
		}
		else if (ACTION_SYNC.equals(event.getActionCommand())) {
			this.setWorkfowId(this.client.getWorkflowIdByResourceUrl(LoadWhipWorkflowAction.getCurrentWorkflowId()));
		}
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
	

	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String url = e.getURL().toString();
				
				if (url.startsWith("http://tag/")) {
					String [] s = e.getURL().toString().split("/");
					this.parent.browseTag(s[s.length-1]);
				}
				else {
					BrowserLauncher launcher = new BrowserLauncher();
					launcher.openURLinBrowser(url);
				}
			}
		} catch (Exception ex) {
			logger.error("Error occurred whilst clicking a hyperlink", ex);
		}
	}
	
	public void setWorkfowId(int id) {
		this.currentWorkflowId = id;
		
		this.setLoadAndImportActions();
		
		this.refresh();
	}
	
	public void refresh() {
		this.clearContentTextPane();
		
		if (this.currentWorkflowId > 0) {
			this.statusLabel.setText("Fetching workflow information from myExperiment...");
			this.refreshButton.setEnabled(true);
			
			// Make call to myExperiment API in a different thread
			// (then use SwingUtilities.invokeLater to update the UI when ready).
			new Thread("Refresh for WorkflowPreviewPanel") {
				public void run() {
					logger.debug("Refreshing Workflow Preview pane");

					try {
						currentWorkflow = client.getWorkflowInfo(currentWorkflowId);

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								repopulate();
							}
						});
					} catch (Exception ex) {
						logger.error("Failed to refresh Workflow Preview pane", ex);
					}
				}
			}.start();
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusLabel.setText("");
					clearContentTextPane();
					disableButtons();
				}
			});
		}
	}
	
	public void repopulate() {
		logger.debug("Repopulating Workflow Preview pane");
		
		if (this.currentWorkflow != null) {
			try {
				StringBuffer content = new StringBuffer();
				content.append("<div class='outer'>");
				content.append("<div class='workflow'>");
				
				content.append("<br/>");
				
				content.append("<p class='title'>");
				content.append("Workflow Entry: <a href='" + this.currentWorkflow.getResource() + "'>" + this.currentWorkflow.getTitle() + "</a> (version" + this.currentWorkflow.getVersion() + ")");
				content.append("</p>");
				
				content.append("<br/>");
				
				content.append("<p class='info'>");
				content.append("<b>Uploader:</b> <a href='" + this.currentWorkflow.getUploader().getResource() + "'>" + this.currentWorkflow.getUploader().getName() + "</a><br/>");
				content.append("<b>Created at: </b> " + this.currentWorkflow.getCreatedAt() + "<br/>");
				content.append("<b>License: </b> <a href='" + this.currentWorkflow.getLicense().getLink() + "'>" + this.currentWorkflow.getLicense().getText() + "</a>");
				content.append("</p>");
				
				content.append("<br/>");
				
				content.append("<img class='preview' src='" + this.currentWorkflow.getThumbnailBig() + "'/>");
				
				content.append("<br/>");
				content.append("<br/>");
				
				if (!this.currentWorkflow.getDescription().equals("")) {
					content.append("<p class='desc'>");
					content.append("<br/>");
					content.append(this.currentWorkflow.getDescription());
					content.append("<br/>");
					content.append("<br/>");
					content.append("</p>");
				}
				else {
					content.append("<span class='none_text'>No description</span>");
				}
				
				content.append("<br/>");
				content.append("<br/>");
				
				content.append("<p style='text-align: center;'><b>Tags</b></p>");
				content.append("<br/>");
				content.append("<p class='tags'>");
				content.append("&nbsp;&nbsp;&nbsp;");
				
				if (this.currentWorkflow.getTags().size() > 0) {
					for (Tag t : this.currentWorkflow.getTags()) {
						content.append("<a href='http://tag/" + t.getTagName() + "'>" + t.getTagName() + "</a>");
						content.append("&nbsp;&nbsp;&nbsp;");
					}
				}
				else {
					content.append("<span class='none_text'>None</span>");
					content.append("&nbsp;&nbsp;&nbsp;");
				}
				
				content.append("</p>");
				
				content.append("<br/>");
				content.append("<br/>");
				
				content.append("<p style='text-align: center;'><b>Credits</b></p>");
				content.append("<br/>");
				content.append("<p class='credits'>");
				content.append("&nbsp;&nbsp;&nbsp;");
				
				if (this.currentWorkflow.getCredits().size() > 0) {
					for (User u : this.currentWorkflow.getCredits()) {
						content.append("<a href='" + u.getResource() + "'>" + u.getName() + "</a>");
						content.append("&nbsp;&nbsp;&nbsp;");
					}
				}
				else {
					content.append("<span class='none_text'>None</span>");
					content.append("&nbsp;&nbsp;&nbsp;");
				}
				
				content.append("</p>");
				
				content.append("</div>");
				content.append("</div>");
				
				HTMLEditorKit kit = new HTMLEditorKit();
				HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());

				kit.setStyleSheet(this.parent.getStyleSheet());
				
				doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());
				
				this.contentTextPane.setEditorKit(kit);
				this.contentTextPane.setDocument(doc);
				
				this.statusLabel.setText("Workflow information found. Last fetched: " + new Date().toString());
				
				this.clearButton.setEnabled(true);
				this.refreshButton.setEnabled(true);
				this.loadButton.setEnabled(true);
				this.importButton.setEnabled(true);
			}
			catch (Exception e) {
				logger.error("Failed to populate Workflow Preview pane", e);
			}
		}
		else {
			statusLabel.setText("Could not find information for workflow ID: " + currentWorkflowId);
			clearContentTextPane();
			disableButtons();
		}
		
		this.revalidate();
	}
	
	public void clear() {
		this.setWorkfowId(0);
	}
	
	private void disableButtons() {
		this.clearButton.setEnabled(false);
		this.refreshButton.setEnabled(false);
		this.loadButton.setEnabled(false);
		this.importButton.setEnabled(false);
	}
	
	private void initialiseUI() {
		this.setLayout(new BorderLayout());
		
		this.add(new ShadedLabel("Workflow Preview", ShadedLabel.TAVERNA_BLUE), BorderLayout.NORTH);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.setOpaque(true);
		mainPanel.setBackground(Color.WHITE);
		
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.setBorder(BorderFactory.createEtchedBorder());
		
		this.statusLabel = new JLabel();
		statusPanel.add(this.statusLabel, BorderLayout.CENTER);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		this.syncButton = new JButton("Sync", TavernaIcons.copyIcon);
		this.syncButton.setActionCommand(ACTION_SYNC);
		this.syncButton.addActionListener(this);
		this.syncButton.setToolTipText("Click this button to try and sync the Preview Workflow pane with the current workflow in the Design section");
		buttonsPanel.add(this.syncButton);
		this.clearButton = new JButton("Clear", TavernaIcons.deleteIcon);
		this.clearButton.setActionCommand(ACTION_CLEAR);
		this.clearButton.addActionListener(this);
		this.clearButton.setToolTipText("Click this button to clear the Preview Workflow pane");
		buttonsPanel.add(this.clearButton);
		this.refreshButton = new JButton("Refresh", TavernaIcons.refreshIcon);
		this.refreshButton.setActionCommand(ACTION_REFRESH);
		this.refreshButton.addActionListener(this);
		this.refreshButton.setToolTipText("Click this button to refresh the Preview Workflow pane");
		buttonsPanel.add(this.refreshButton);
		
		statusPanel.add(buttonsPanel, BorderLayout.EAST);
		
		mainPanel.add(statusPanel, BorderLayout.NORTH);
		
		this.contentPanel = new JPanel(new BorderLayout());
		this.contentPanel.setBorder(BorderFactory.createEmptyBorder());
		this.contentScrollPane = new JScrollPane(this.contentPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.contentScrollPane.setOpaque(true);
		this.contentScrollPane.setBackground(Color.WHITE);
		mainPanel.add(this.contentScrollPane, BorderLayout.CENTER);
		
		this.contentTextPane = new JTextPane();
		this.contentTextPane.setBorder(BorderFactory.createEmptyBorder());
		this.contentTextPane.setEditable(false);
		this.contentTextPane.addHyperlinkListener(this);
		this.contentPanel.add(this.contentTextPane, BorderLayout.CENTER);
		
		JPanel loadPanel = new JPanel();
		loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.LINE_AXIS));
		loadPanel.setBorder(BorderFactory.createEmptyBorder());
		
		this.loadButton = new JButton();
		loadPanel.add(this.loadButton);
		
		this.importButton = new JButton();
		loadPanel.add(this.importButton);
		
		this.setLoadAndImportActions();
		
		mainPanel.add(loadPanel, BorderLayout.SOUTH);
		
		this.add(mainPanel, BorderLayout.CENTER);
		
		this.disableButtons();
	}
	
	private void setLoadAndImportActions() {
		this.loadButton.setAction(this.parent.new LoadWorkflowAction(this.currentWorkflowId));
		this.importButton.setAction(this.parent.new ImportWorkflowAction(this.currentWorkflowId));
	}
	
	private void clearContentTextPane() {
		this.contentTextPane.setDocument(new HTMLDocument());
	}
	
	private void populateDummy(){
		try {
			HTMLEditorKit kit = new HTMLEditorKit();
			HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
			StyleSheet css = kit.getStyleSheet();
			
			StringBuffer content = new StringBuffer();
			content.append("<div class='outer'>");
			content.append("<div class='workflow'>");
			content.append("<p class='title'>");
			content.append("Workflow Entry: <a href='http://sandbox.myexperiment.org/workflows/157'>Example of a conditional execution workflow</a>");
			content.append("</p>");
			content.append("<p class='info'>");
			content.append("<b>Uploader:</b> <a href=''>Alan Williams</a><br/>");
			content.append("<b>Created at: </b> Wed 5th March 2008 13:55:45<br/>");
			content.append("<b>License: </b> <a href=''>Creative Commons Attribution 3.0 License</a>");
			content.append("</p>");
			content.append("<br/>");
			content.append("<img class='preview' src='http://sandbox.myexperiment.org/workflow/image/157/example_of_a_conditional_execution_workflow_7882_1.png' />");
			content.append("<p class='desc'>");
			content.append("If the input is true then the string 'foo' is emited, if false then 'bar'. Just a simple example to show how the monster works, so to speak.");
			content.append("</p>");
			content.append("<br/>");
			content.append("<p style='text-align: center'><b>Tags</b></p>");
			content.append("<div style='text-align: center'>");
			content.append("&nbsp;&nbsp;&nbsp;");
			content.append("<a href='' style='color: #000066;'>example</a>");
			content.append("&nbsp;&nbsp;&nbsp;");
			content.append("<a href='' style='color: #000066;'>mygrid</a>");
			content.append("&nbsp;&nbsp;&nbsp;");
			content.append("<a href='' style='color: #000066;'>taverna</a>");
			content.append("&nbsp;&nbsp;&nbsp;");
			content.append("<a href='' style='color: #000066;'>condition</a>");
			content.append("</div>");
			content.append("<br/>");
			content.append("<p style='text-align: center'><b>Credits</b></p>");
			content.append("<div style='text-align: center'>");
			content.append("<a href=''>Tom Oinn</a>");
			content.append("</div>");
			content.append("</div>");
			content.append("</div>");
			
			css.addRule("body {font-family: arial,helvetica,clean,sans-serif; margin: 0; padding: 0;}");
			css.addRule("div.outer {padding-top: 0; padding-bottom: 0; padding-left: 10px; padding-right: 10px;}");
			css.addRule("div.workflow {text-align: center;}");
			css.addRule(".workflow p.info {line-height: 1.5; text-align: center; color: #333333;}");
			css.addRule(".workflow p.title {text-align: center; line-height: 1.0; color: #333333; font-size: large; font-weight: bold; margin-bottom: 0; margin-top; 0; padding: 0;}");
			css.addRule(".workflow img.preview {padding: 5px;}");
			css.addRule(".workflow p.desc {background-color: #EEEEEE; text-align: left; width: 400px; font-size: medium; padding: 8px;}");
			
			doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());
			
			this.contentTextPane.setEditorKit(kit);
			this.contentTextPane.setDocument(doc);
			
			this.statusLabel.setText("NOTE: dummy data!");
			
			this.clearButton.setEnabled(true);
			this.refreshButton.setEnabled(true);
			this.loadButton.setEnabled(true);
		}
		catch (Exception e) {
			logger.error("Failed to populate Workflow Preview pane", e);
		}
	}
}
