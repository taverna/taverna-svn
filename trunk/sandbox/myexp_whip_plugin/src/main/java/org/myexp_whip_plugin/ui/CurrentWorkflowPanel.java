package org.myexp_whip_plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.myexp_whip_plugin.MyExperimentClient;

public class CurrentWorkflowPanel extends JPanel implements ActionListener, ChangeListener {
	
	private MyExperimentClient client;
	
	public CurrentWorkflowPanel(MyExperimentClient client) {
		this.client = client;
	}
	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
}
