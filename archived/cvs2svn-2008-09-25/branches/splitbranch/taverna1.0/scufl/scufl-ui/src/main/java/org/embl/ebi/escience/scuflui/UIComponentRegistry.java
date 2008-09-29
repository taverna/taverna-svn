package org.embl.ebi.escience.scuflui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.utils.TavernaSPIRegistry;

/**
 * A registry that maintains a list of all available UI components
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class UIComponentRegistry extends TavernaSPIRegistry<ScuflUIComponent>{

	private static Logger logger = Logger.getLogger(UIComponentRegistry.class);

	private static UIComponentRegistry instance;

	private Map<String,String> components;

	private Map<String,ImageIcon> icons;

	public static synchronized UIComponentRegistry instance() {
		if (instance == null) {
			instance = new UIComponentRegistry();
			instance.loadInstances();
		}
		return instance;
	}

	public static void forceReload() {
		flushCache(UIComponentRegistry.class);
		instance.loadInstances();
	}

	private UIComponentRegistry() {
		super(ScuflUIComponent.class);
		components = new HashMap<String,String>();
		icons = new HashMap<String,ImageIcon>();
	}

	public void loadInstances() {
		logger.info("Loading all UI components");
		
		List<ScuflUIComponent> uicomponents=findComponents();
		
		for (ScuflUIComponent component : uicomponents) {
			components.put(component.getName(),component.getClass().getName());
			icons.put(component.getName(),component.getIcon());
		}		
		logger.info("Done");
	}

	public Map getComponents() {
		return this.components;
	}

	public Map getIcons() {
		return this.icons;
	}

}
