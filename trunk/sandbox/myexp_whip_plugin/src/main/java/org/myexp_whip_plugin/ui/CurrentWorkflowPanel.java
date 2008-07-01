package org.myexp_whip_plugin.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.myexp_whip_plugin.MyExperimentClient;

public class CurrentWorkflowPanel extends JPanel implements ActionListener, ChangeListener {
	
	private MyExperimentClient client;
	
	private Logger logger;
	
	public CurrentWorkflowPanel(MyExperimentClient client, Logger logger) {
		this.client = client;
		this.logger = logger;
		
		this.setLayout(new BorderLayout());
		
		this.add(new ShadedLabel("Workflow Preview", ShadedLabel.TAVERNA_BLUE), BorderLayout.NORTH);
		this.add(new JPanel(), BorderLayout.CENTER);
	}
	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
}
