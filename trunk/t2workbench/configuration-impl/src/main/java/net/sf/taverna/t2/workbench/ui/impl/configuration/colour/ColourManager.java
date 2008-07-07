package net.sf.taverna.t2.workbench.ui.impl.configuration.colour;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;

import org.apache.log4j.Logger;

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
	
	private static Logger logger = Logger.getLogger(ColourManager.class);
	private Map<String, Object> defaultPropertyMap = new HashMap<String, Object>();
	private Map<String, Object> propertyMap = new HashMap<String, Object>();
	private static ColourManager instance = new ColourManager();
	private Map<Object,Color> cachedColours = new HashMap<Object, Color>();

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getCategory()
	 */
	public String getCategory() {
		return "colour";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getDefaultPropertyMap()
	 */
	public Map<String, Object> getDefaultPropertyMap() {
		return defaultPropertyMap;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getName()
	 */
	public String getName() {
		return "Colour Management";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getPropertyMap()
	 */
	public Map<String, Object> getPropertyMap() {
		return propertyMap;
	}

	/**
	 * Unique identifier for this ColourManager
	 */
	public String getUUID() {
		return "d13327f0-0c84-11dd-bd0b-0800200c9a66";
	}

	

	private ColourManager() {
		initialiseDefaults();
		ConfigurationManager manager = ConfigurationManager.getInstance();
		//FIXME: hardcoded to use the temp dir
		try {			
			if (!manager.isBaseLocationSet()) manager.setBaseConfigLocation(new File(System.getProperty("java.io.tmpdir")));
			manager.populate(this);
		} catch (Exception e) {
			logger.error("An error occurred populating the ColourManager configuration, or storing the defaults",e);
		}
	}

	private void initialiseDefaults() {
		defaultPropertyMap.put("net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity", "#98fb98");//palegreen
		defaultPropertyMap.put("net.sf.taverna.t2.activities.beanshell.BeanshellActivity", "#deb887");//burlywood2
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomart.BiomartActivity", "#d1eeee");//lightcyan2
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomoby.BiomobyActivity", "#ffb90f");//darkgoldenrod1
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomoby.BiomobyObjectActivity", "#ffd700");//gold
		defaultPropertyMap.put("net.sf.taverna.t2.activities.biomoby.MobyParseDatatypeActivity", "#ffffff");//white
		defaultPropertyMap.put("net.sf.taverna.t2.activities.dataflow.DataflowActivity", "#ffc0cb");//pink
		defaultPropertyMap.put("net.sf.taverna.t2.activities.rshell.RshellActivity", "#fafad2");//lightgoldenrodyellow
		defaultPropertyMap.put("net.sf.taverna.t2.activities.soaplab.SoaplabActivity", "#fafad2");//lightgoldenrodyellow
		defaultPropertyMap.put("net.sf.taverna.t2.activities.stringconstant.StringConstantActivity", "#b0c4de");//lightsteelblue
		defaultPropertyMap.put("net.sf.taverna.t2.activities.wsdl.WSDLActivity", "#a2cd5a");//darkolivegreen3
		defaultPropertyMap.put("net.sf.taverna.t2.activities.localworker.LocalworkerActivity", "#d15fee"); //mediumorchid2
	}

	/**
	 * @return a Singleton instance of the ColourManager
	 */
	public static ColourManager getInstance() {
		return instance;
	}

	/**
	 * Builds a Color that has been configured and associated with the given Object type.
	 * 
	 * @return the associated Color, or if nothing is associated returns WHITE
	 *             
	 */
	public Color getPreferredColour(Object itemKey) {
		Color colour = cachedColours.get(itemKey);
		if (colour == null) {
			String colourString=(String)getPropertyMap().get(itemKey);
			colour = colourString==null ? Color.WHITE : Color.decode(colourString);
			cachedColours.put(itemKey,colour);
		}
		return colour;
	}

	public void restoreDefaults() {
		propertyMap.clear();
		propertyMap.putAll(defaultPropertyMap);
		cachedColours.clear();
	}

}
