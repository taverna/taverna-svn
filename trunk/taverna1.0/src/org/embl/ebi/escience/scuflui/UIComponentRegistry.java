package org.embl.ebi.escience.scuflui;

import org.apache.commons.discovery.tools.Service;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

// Utility Imports
import java.util.*;

/**
 * A registry that maintains a list of all available UI components
 * @author Tom Oinn
 */
public class UIComponentRegistry {
    
    private static Logger log = Logger.getLogger(UIComponentRegistry.class);
    private static UIComponentRegistry instance;
    private Map components;
    private Map icons;

    public static synchronized UIComponentRegistry instance() {
	if (instance == null) {
	    instance = new UIComponentRegistry();
	    instance.loadInstances(UIComponentRegistry.class.getClassLoader());
	}
	return instance;
    }
    
    public UIComponentRegistry() {
	components = new HashMap();
	icons = new HashMap();
    }
    
    public void loadInstances(ClassLoader classLoader) {
	log.info("Loading all UI components");
	SPInterface spiIF = new SPInterface(ScuflUIComponent.class);
	ClassLoaders loaders = new ClassLoaders();
	loaders.put(classLoader);
	Enumeration spe = Service.providers(spiIF, loaders);
	while (spe.hasMoreElements()) {
	    ScuflUIComponent component = (ScuflUIComponent)spe.nextElement();
	    String componentClassName = component.getClass().getName();
	    String componentDisplayName = component.getName();
	    components.put(componentDisplayName,
			   componentClassName);
	    icons.put(componentDisplayName,
		      component.getIcon());
	}
	log.info("Done");
    }

    public Map getComponents() {
	return this.components;
    }

    public Map getIcons() {
	return this.icons;
    }

}
