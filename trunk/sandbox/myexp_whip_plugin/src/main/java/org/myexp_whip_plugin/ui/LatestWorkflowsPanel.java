package org.myexp_whip_plugin.ui;

import com.sun.syndication.feed.synd.*;

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
		ChangeListener {
	
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

		this.listPanel.removeAll();

		if (this.workflows != null) {
			this.statusLabel.setText(this.workflows.size() + " workflows found");
			
			for (SyndEntry ent : this.workflows) {
				JPanel mainPanel = new JPanel(new BorderLayout());
				mainPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

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
				css.addRule("p.title {display; block; line-height: 1.0; color: #333333; font-size: 14pt; font-weight: bold; margin-bottom: 5px; margin-top; 0; padding: 0;}");
				css.addRule("div.desc {display; block; border: 1px dotted #DDDDDD; padding-top: 3px; padding-bottom: 3px; padding-left: 6px; padding-right: 6px;}");
				
				try {
					doc.insertAfterStart(doc.getRootElements()[0].getElement(0), content.toString());
				} catch (Exception e) {
					logger.error("Failed to set HTMLDocument root with entry from latest workflows feed", e);
				}
				
				infoTextPane.setEditorKit(kit);
				infoTextPane.setDocument(doc);
				infoTextPane.setContentType("text/html");

				mainPanel.add(infoTextPane, BorderLayout.CENTER);
				
				JPanel buttonsPanel = new JPanel();
				buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
				JButton previewButton = new JButton("Preview");
				previewButton.setAlignmentY(Component.TOP_ALIGNMENT);
				buttonsPanel.add(previewButton);
				JButton loadButton = new JButton("Load");
				loadButton.setAlignmentY(Component.TOP_ALIGNMENT);
				buttonsPanel.add(loadButton);
				mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
				
				this.listPanel.add(mainPanel);
			}
			
			this.listPanel.setPreferredSize(new Dimension(1, 1));
			this.listPanel.setMinimumSize(new Dimension(1, 1));
			this.listScrollPane.getVerticalScrollBar().setValue(0);
		}
		else {
			this.statusLabel.setText("No workflows found");
		}

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
		this.refreshButton.setToolTipText("Click this button to refresh the Latest Workflows list");
		topPanel.add(this.refreshButton, BorderLayout.EAST);
		this.add(topPanel, BorderLayout.NORTH);
		
		this.listPanel = new JPanel();
		this.listPanel.setLayout(new BoxLayout(this.listPanel, BoxLayout.Y_AXIS));
		this.listScrollPane = new JScrollPane(this.listPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(this.listScrollPane, BorderLayout.CENTER);
	}
}
