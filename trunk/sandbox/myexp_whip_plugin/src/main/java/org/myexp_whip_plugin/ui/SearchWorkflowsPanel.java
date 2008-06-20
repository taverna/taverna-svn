package org.myexp_whip_plugin.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.myexp_whip_plugin.MyExperimentClient;

public class SearchWorkflowsPanel extends JPanel implements ActionListener, ChangeListener {
	
	private MyExperimentClient client;
	
	public SearchWorkflowsPanel(MyExperimentClient client) {
		this.client = client;
	}
	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
}
