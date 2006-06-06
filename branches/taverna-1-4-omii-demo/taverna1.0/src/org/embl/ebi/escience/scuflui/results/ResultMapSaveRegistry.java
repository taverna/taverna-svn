/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Maintains a list of all available providers
 * of the ResultMapSaveSPI interface. These are
 * used to store a Map of DataThing objects to
 * some persistant storage mechanism.
 * @author Tom Oinn
 */
public class ResultMapSaveRegistry {
    
    private static Logger log = 
	Logger.getLogger(ResultMapSaveRegistry.class);
    private static ResultMapSaveRegistry instance;
    private List savePlugins;
    
    /**
     * Get an instance of the ResultMapSaveRegistry,
     * this method initializes from the current system
     * class loader if this hasn't already take place
     */
    private static synchronized ResultMapSaveRegistry instance() {
	if (instance == null) {
	    instance = new ResultMapSaveRegistry();
	    instance.loadInstances(ResultMapSaveRegistry.class.getClassLoader());
	}
	return instance;
    }
    
    /**
     * External classes should never construct an instance of this
     * class directly, the only public functionality here is obtained
     * from the static plugins() method.
     */
    private ResultMapSaveRegistry() {
	savePlugins = new ArrayList();
    }
    
    /**
     * Load the available SPI workers from the specified ClassLoader
     */
    private void loadInstances(ClassLoader classLoader) {
	log.info("Loading save plugins");
	SPInterface spiIF = new SPInterface(ResultMapSaveSPI.class);
	ClassLoaders loaders = new ClassLoaders();
	loaders.put(classLoader);
	Enumeration spe = Service.providers(spiIF, loaders);
	while (spe.hasMoreElements()) {
	    ResultMapSaveSPI rmss = (ResultMapSaveSPI)spe.nextElement();
	    log.info("\t" + rmss.getName());
	    savePlugins.add(rmss);
	}
	log.info("Done");
    }
    
    /**
     * Initialize the plugin mechanism if required then return
     * an array of implementations of the ResultMapSaveSPI
     * service provider interface. These can then be used to
     * populate the save portion of the UI
     */
    public static ResultMapSaveSPI[] plugins() {
	List pluginList = instance().savePlugins;
	return (ResultMapSaveSPI[])(pluginList.toArray(new ResultMapSaveSPI[0]));
    }

}
    
    
