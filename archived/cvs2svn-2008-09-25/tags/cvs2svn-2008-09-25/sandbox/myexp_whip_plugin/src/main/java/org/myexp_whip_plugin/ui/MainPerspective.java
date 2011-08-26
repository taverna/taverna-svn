// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package org.myexp_whip_plugin.ui;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;

/*
 * @author Jiten Bhagat
 */
public class MainPerspective extends AbstractPerspective {
	
	static String NAME = "myExperiment (beta)";
	
	public ImageIcon getButtonIcon() {
		URL iconURL = MainPerspective.class.getResource("/myexp_icon.png");
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public InputStream getLayoutResourceStream() {
		return MainPerspective.class.getResourceAsStream("/myexp-whip-plugin.xml");
	}

	public String getText() {
		return NAME;
	}

}
