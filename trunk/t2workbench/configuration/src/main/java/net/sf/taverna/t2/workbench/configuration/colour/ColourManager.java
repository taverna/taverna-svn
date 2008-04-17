package net.sf.taverna.t2.workbench.configuration.colour;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.Configurable;

/**
 * A factory class that determines the colour that a Colourable UI component should be displayed
 * as, according to a schema configured by the user.
 * 
 * @author Stuart Owen
 * @see Colourable
 *
 */
public class ColourManager implements Configurable {
	private Map<String,Object> defaultPropertyMap=new HashMap<String,Object>();
	private Map<String,Object> propertyMap=new HashMap<String,Object>();
	
	public String getCategory() {
		return "colour";
	}

	public Map<String,Object> getDefaultPropertyMap() {
		return defaultPropertyMap;
	}

	public String getName() {
		return "Colour Management";
	}

	public Map<String,Object> getPropertyMap() {
		return propertyMap;
	}

	public String getUUID() {
		return "d13327f0-0c84-11dd-bd0b-0800200c9a66";
	}

	private static ColourManager instance = new ColourManager();
	
	private ColourManager() {
		
	}
	
	/**
	 * @return a Singleton instance of the ColourManager
	 */
	public static ColourManager getInstance() {
		return instance;
	}
	
	/**
	 * @param item - Colourable item that we wish to know the preferred colour for
	 * @return the preferred colour for that item
	 */
	public Color getPreferredColour(Colourable item) {
		
		//FIXME: dummy code for now
		return new Color(1f,1f,1f);
	}
	
}
