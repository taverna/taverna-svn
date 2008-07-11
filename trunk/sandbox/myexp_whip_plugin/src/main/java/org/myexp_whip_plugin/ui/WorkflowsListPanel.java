package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;
import org.myexp_whip_plugin.MyExperimentClient;
import org.myexp_whip_plugin.Resource;
import org.myexp_whip_plugin.Workflow;

import edu.stanford.ejalbert.BrowserLauncher;

public class WorkflowsListPanel extends BasePanel implements HyperlinkListener {
	
	private JScrollPane listScrollPane;
	private JPanel listPanel;
	
	private List<Workflow> workflows;
	
	public WorkflowsListPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		super(parent, client, logger);
		
		this.initialiseUI();
	}
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String url = e.getURL().toString();
				
				if (url.startsWith("http://workflow/")) {
					String [] s = e.getURL().toString().split("/");
					this.parent.setCurrentWorkflow(s[s.length-1]);
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
	
	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
		
		this.repopulate();
	}

	public void clear() {
		this.listPanel.removeAll();
	}

	public void refresh() {
		if (this.workflows != null) {
			this.repopulate();
		}
	}

	public void repopulate() {
		if (this.workflows != null) {
			this.clear();
			
			for (Workflow workflow : this.workflows) {
				try {
					JPanel mainPanel = new JPanel(new BorderLayout());
					mainPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

					JTextPane infoTextPane = new JTextPane();
					infoTextPane.setBorder(BorderFactory.createEmptyBorder());
					infoTextPane.setEditable(false);
					
					StringBuffer content = new StringBuffer();
					content.append("<div class='outer'>");
					content.append("<div class='list_item'>");
					content.append("<p class='title'>");
					content.append("<a href='http://workflow/" + workflow.getId() + "'>" + workflow.getTitle() + " (" + workflow.getUploader().getName() + ")</a>");
					content.append("&nbsp;&nbsp;&nbsp;&nbsp;");
					content.append("<span class='ext'>(<a class='ext_link' href='" + workflow.getResource() + "'>Open in myExperiment</a>)</span>");
					content.append("</p>");
					if (workflow.getDescription() != null && workflow.getDescription().length() > 0) {
						content.append("<div class='desc'>");
						content.append(workflow.getDescription());
						content.append("</div>");
						content.append("<br/>");
					}
					else {
						content.append("<br/>");
					}
					content.append("</div>");
					content.append("</div>");
					
					HTMLEditorKit kit = new HTMLEditorKit();
					HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());

					kit.setStyleSheet(this.parent.getStyleSheet());
					
					doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());
					
					infoTextPane.setEditorKit(kit);
					infoTextPane.setDocument(doc);
					infoTextPane.setContentType("text/html");
					infoTextPane.addHyperlinkListener(this);

					mainPanel.add(infoTextPane, BorderLayout.CENTER);
					
					JPanel buttonsPanel = new JPanel();
					buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
					//buttonsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					JButton previewButton = new JButton();
					previewButton.setAction(this.parent.new PreviewWorkflowAction(workflow.getId()));
					buttonsPanel.add(previewButton);
					JButton loadButton = new JButton();
					loadButton.setAction(this.parent.new LoadWorkflowAction(workflow.getId()));
					buttonsPanel.add(loadButton);
					mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
					
					this.listPanel.add(mainPanel);
				}
				catch (Exception e) {
					logger.error("Failed to add item entry to WorkflowsListPanel", e);
				}
			}
		}
	}
	
	private void initialiseUI() {
		this.setLayout(new BorderLayout());
		
		this.listPanel = new JPanel();
		this.listPanel.setLayout(new BoxLayout(this.listPanel, BoxLayout.PAGE_AXIS));
		this.listPanel.setBorder(BorderFactory.createEmptyBorder());
		this.listScrollPane = new JScrollPane(this.listPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		this.add(this.listScrollPane, BorderLayout.CENTER);
	}
}
