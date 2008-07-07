package org.myexp_whip_plugin.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;
import org.myexp_whip_plugin.MyExperimentClient;

public class ExampleWorkflowsPanel extends BasePanel implements ActionListener, ChangeListener {
	
	public ExampleWorkflowsPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		super(parent, client, logger);
	}
	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	public void repopulate() {
		// TODO Auto-generated method stub
		
	}
}

