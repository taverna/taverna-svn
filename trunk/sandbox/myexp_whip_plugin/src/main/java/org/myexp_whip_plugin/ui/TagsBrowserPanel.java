package org.myexp_whip_plugin.ui;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;
import org.myexp_whip_plugin.MyExperimentClient;

public class TagsBrowserPanel extends JPanel implements ActionListener, ChangeListener {
	
	private MainComponent parent;
	
	private MyExperimentClient client;
	
	private Logger logger;
	
	public TagsBrowserPanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		this.parent = parent;
		this.client = client;
		this.logger = logger;
	}
	
	public void actionPerformed(ActionEvent event) {
		
	}
	
	public void stateChanged(ChangeEvent event) {
		
	}
}
