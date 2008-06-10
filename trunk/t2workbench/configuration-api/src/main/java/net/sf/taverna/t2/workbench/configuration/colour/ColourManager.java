package net.sf.taverna.t2.workbench.configuration.colour;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.Configurable;

/**
 * A factory class that determines the colour that a Colourable UI component
 * should be displayed as, according to a schema configured by the user.
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @see Colourable
 * 
 */
public class ColourManager implements Configurable {
	private Map<String, Object> defaultPropertyMap = new HashMap<String, Object>();
	private Map<String, Object> propertyMap = new HashMap<String, Object>();

	public String getCategory() {
		return "colour";
	}

	public Map<String, Object> getDefaultPropertyMap() {
		return defaultPropertyMap;
	}

	public String getName() {
		return "Colour Management";
	}

	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}

	/**
	 * Unique identifier for this ColourManager
	 */
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
	 * The {@link Colourable} is stored with its full class name as the key and
	 * an array of strings for its RGB values
	 * 
	 * @param item -
	 *            Colourable item that we wish to know the preferred colour for.
	 * @return the preferred colour for that item
	 * @throws Exception
	 *             if the colours have not been stored as a string array
	 */
	public Color getPreferredColour(Colourable item) throws Exception {

		Object[] colours = (Object[]) propertyMap.get(item.getClass()
				.getCanonicalName());
		if (colours != null) {

			if (colours instanceof String[] && ((String[]) colours).length == 3) {
				return new Color(Integer.parseInt((String) colours[0]), Integer
						.parseInt((String) colours[1]), Integer
						.parseInt((String) colours[2]));
			} else {
				throw new Exception(
						"Properties were not stored as the correct type, should have been as String[] but was "
								+ colours.getClass().getCanonicalName());
			}
		} else {
			throw new Exception(
					"There were no colour properties available for "
							+ item.getClass().getName());
		}
	}

	public void restoreDefaults() {
		// TODO Auto-generated method stub

	}

}
