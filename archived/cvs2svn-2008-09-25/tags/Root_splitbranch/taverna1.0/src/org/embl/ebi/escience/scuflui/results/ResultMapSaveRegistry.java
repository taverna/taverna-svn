/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * Maintains a list of all available providers of the ResultMapSaveSPI
 * interface. These are used to store a Map of DataThing objects to some
 * persistant storage mechanism.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class ResultMapSaveRegistry extends TavernaSPIRegistry<ResultMapSaveSPI> {

	private static Logger log = Logger.getLogger(ResultMapSaveRegistry.class);

	private static ResultMapSaveRegistry instance;

	private List<ResultMapSaveSPI> savePlugins;

	/**
	 * Get an instance of the ResultMapSaveRegistry, this method initializes
	 * from the current system class loader if this hasn't already take place
	 */
	private static synchronized ResultMapSaveRegistry instance() {
		if (instance == null) {
			instance = new ResultMapSaveRegistry();
			instance.loadInstances(ResultMapSaveRegistry.class.getClassLoader());
		}
		return instance;
	}

	/**
	 * External classes should never construct an instance of this class
	 * directly, the only public functionality here is obtained from the static
	 * plugins() method.
	 */
	private ResultMapSaveRegistry() {
		super(ResultMapSaveSPI.class);
		savePlugins = new ArrayList<ResultMapSaveSPI>();
	}

	/**
	 * Load the available SPI workers from the specified ClassLoader
	 */
	private void loadInstances(ClassLoader classLoader) {
		log.info("Loading save plugins");
		savePlugins = findComponents(classLoader);
		log.info("Done");
	}

	/**
	 * Initialize the plugin mechanism if required then return an array of
	 * implementations of the ResultMapSaveSPI service provider interface. These
	 * can then be used to populate the save portion of the UI
	 */
	public static ResultMapSaveSPI[] plugins() {
		return (ResultMapSaveSPI[]) (instance().savePlugins.toArray(new ResultMapSaveSPI[0]));
	}

}
