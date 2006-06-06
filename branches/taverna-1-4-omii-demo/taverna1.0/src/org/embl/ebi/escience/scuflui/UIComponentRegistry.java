package org.embl.ebi.escience.scuflui;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.tools.SPInterface;
import org.apache.commons.discovery.tools.Service;
import org.apache.log4j.Logger;

/**
 * A registry that maintains a list of all available UI components
 * 
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
			instance.loadInstances();
		}
		return instance;
	}

	public static void forceReload() {
		instance.loadInstances();
	}

	public UIComponentRegistry() {
		components = new HashMap();
		icons = new HashMap();
	}

	public void loadInstances() {
		ClassLoader classLoader = UIComponentRegistry.class.getClassLoader();
		log.info("Loading all UI components");
		SPInterface spiIF = new SPInterface(ScuflUIComponent.class);
		ClassLoaders loaders = new ClassLoaders();
		loaders.put(classLoader);
		Enumeration spe = Service.providers(spiIF, loaders);
		// FIXME: Throws UnsupportedClassVersionError on 1.5 classes from 1.4 VM
		while (spe.hasMoreElements()) {
			ScuflUIComponent component = (ScuflUIComponent) spe.nextElement();			
			String componentClassName = component.getClass().getName();
			String componentDisplayName = component.getName();
			try {
				components.put(componentDisplayName, componentClassName);
				icons.put(componentDisplayName, component.getIcon());
			} catch (RuntimeException re) {
				//
			}
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
