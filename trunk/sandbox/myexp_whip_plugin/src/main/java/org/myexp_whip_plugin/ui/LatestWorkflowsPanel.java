package org.myexp_whip_plugin.ui;

import com.sun.syndication.feed.synd.*;

import edu.stanford.ejalbert.BrowserLauncher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Scrollbar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.myexp_whip_plugin.MyExperimentClient;

public class LatestWorkflowsPanel extends JPanel implements ActionListener,
		ChangeListener, HyperlinkListener {
	
	private static final String ACTION_REFRESH = "refresh_latest_workflows"; 

	private MyExperimentClient client;

	private Logger logger;

	private List<SyndEntry> workflows;

	private JLabel statusLabel;
	
	private JButton refreshButton;
	
	private JScrollPane listScrollPane; 
	
	private JPanel listPanel;

	public LatestWorkflowsPanel(MyExperimentClient client, Logger logger) {
		this.client = client;
		this.logger = logger;
		
		this.initialiseUI();
	}
	
	public void actionPerformed(ActionEvent event) {
		if (ACTION_REFRESH.equals(event.getActionCommand())) {
			this.refresh();
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

		this.cleanupListPanel();

		if (this.workflows != null) {
			this.statusLabel.setText(this.workflows.size() + " workflows found");
			
			for (SyndEntry ent : this.workflows) {
				JPanel mainPanel = new JPanel(new BorderLayout());
				mainPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

				JTextPane infoTextPane = new JTextPane();
				infoTextPane.setBorder(BorderFactory.createEmptyBorder());
				infoTextPane.setEditable(false);
				
				StringBuffer content = new StringBuffer();
				content.append("<div class=\"outer\">");
				content.append("<p class=\"title\">");
				content.append(ent.getTitle());
				content.append("</p>");
				if (ent.getDescription().getValue().length() > 0) {
					content.append("<div class=\"desc\">");
					content.append(ent.getDescription().getValue());
					content.append("<br/>");
					content.append("</div>");
				}
				else {
					content.append("<br/>");
				}
				content.append("</div>");
				
				HTMLEditorKit kit = new HTMLEditorKit();
				HTMLDocument doc = (HTMLDocument) (kit.createDefaultDocument());
				StyleSheet css = kit.getStyleSheet();
				
				css.addRule("body {font-family: arial,helvetica,clean,sans-serif; margin: 0; padding: 0;}");
				css.addRule("div.outer {display; block; text-align: left; padding-top: 0; padding-bottom: 0; padding-left: 10px; padding-right: 10px;}");
				css.addRule("p.title {display; block; line-height: 1.0; color: #000066; font-size: large; font-weight: bold; margin-bottom: 0; margin-top; 0; padding: 0;}");
				css.addRule("div.desc {display; block; font-size: medium; padding-top: 0; padding-bottom: 0; padding-left: 15px; padding-right: 5px;}");
				
				try {
					doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());
				} catch (Exception e) {
					logger.error("Failed to set JTextPane's HTMLDocument with entry from latest workflows feed", e);
				}
				
				infoTextPane.setEditorKit(kit);
				infoTextPane.setDocument(doc);
				infoTextPane.setContentType("text/html");
				infoTextPane.addHyperlinkListener(this);

				mainPanel.add(infoTextPane, BorderLayout.CENTER);
				
				JPanel buttonsPanel = new JPanel();
				buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
				//buttonsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				JButton previewButton = new JButton("Preview");
				previewButton.setToolTipText("Click this button to preview this workflow in the Preview Workflow pane");
				buttonsPanel.add(previewButton);
				JButton loadButton = new JButton("Load");
				loadButton.setToolTipText("Click this button to download and load this workflow in Taverna");
				buttonsPanel.add(loadButton);
				mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
				
				this.listPanel.add(mainPanel);
			}
			
			//this.listPanel.setPreferredSize(null);
			//this.listPanel.setPreferredSize(new Dimension(1, this.listPanel.getSize().height));
			
			this.listScrollPane.getVerticalScrollBar().setValue(0);
		}
		else {
			this.statusLabel.setText("No workflows found");
		}
		
		this.revalidate();
	}
	
	public void revalidateScrollPane() {
		this.listScrollPane.revalidate();
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
		
		this.listPanel = new JPanel();
		this.listPanel.setLayout(new BoxLayout(this.listPanel, BoxLayout.Y_AXIS));
		JPanel dummyPanel = new JPanel(new BorderLayout());
		dummyPanel.add(this.listPanel, BorderLayout.WEST);
		this.listScrollPane = new JScrollPane(dummyPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(this.listScrollPane, BorderLayout.CENTER);
	}

	private void cleanupListPanel() {
		
		// Apparently not required...
		/*
		for (Component c : this.listPanel.getComponents()) {
			if (c instanceof JPanel) {
				for (Component p : ((JPanel)c).getComponents()) {
					// Remove hyperlink listener to prevent memory leak!
					if (p instanceof JTextPane) {
						logger.debug("Removing hyperlink listener from JTextPane");
						((JTextPane)p).removeHyperlinkListener(this);
					}
				}
			}
		}
		*/
		
		this.listPanel.removeAll();
	}
}
