// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package org.myexp_whip_plugin.ui;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import org.myexp_whip_plugin.ui.MainComponent;

/*
 * @author Jiten Bhagat
 */
public class MainComponentFactory implements UIComponentFactorySPI {

	private static Logger logger = Logger.getLogger(MainComponentFactory.class);
	
	public UIComponentSPI getComponent() {
		logger.info("Making new component " + this);
		return new MainComponent();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "myExperiment Main Panel";
	}

}
