/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import org.embl.ebi.escience.scuflui.spi.ResultMapSaveSPI;
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

	private static ResultMapSaveRegistry instance;

	/**
	 * Get an instance of the ResultMapSaveRegistry, this method initializes
	 * from the current system class loader if this hasn't already take place
	 */
	private static synchronized ResultMapSaveRegistry instance() {
		if (instance == null) {
			instance = new ResultMapSaveRegistry();
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
	}

	/**
	 * Initialize the plugin mechanism if required then return an array of
	 * implementations of the ResultMapSaveSPI service provider interface. These
	 * can then be used to populate the save portion of the UI
	 */
	public static ResultMapSaveSPI[] plugins() {
		return instance().findComponents().toArray(new ResultMapSaveSPI[0]);
	}

}
