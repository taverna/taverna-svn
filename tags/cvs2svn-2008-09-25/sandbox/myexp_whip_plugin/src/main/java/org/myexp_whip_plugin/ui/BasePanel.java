// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package org.myexp_whip_plugin.ui;

/*
 * @author Jiten Bhagat
 */
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.myexp_whip_plugin.MyExperimentClient;

public abstract class BasePanel extends JPanel {
	
	protected MainComponent parent;
	
	protected MyExperimentClient client;

	protected Logger logger;
	
	public BasePanel(MainComponent parent, MyExperimentClient client, Logger logger) {
		this.parent = parent;
		this.client = client;
		this.logger = logger;
	}
	
	public abstract void refresh();
	
	public abstract void repopulate();
	
	public abstract void clear();
}
