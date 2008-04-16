package net.sf.taverna.t2.workbench.configuration.colour;

import java.awt.Color;

public class ColourManager {
	private static ColourManager instance = new ColourManager();
	
	public static ColourManager getInstance() {
		return instance;
	}
	
	public Color getPreferredColour(Colourable item) {
		
		//FIXME: dummy code for now
		return new Color(1f,1f,1f);
	}
	
}
