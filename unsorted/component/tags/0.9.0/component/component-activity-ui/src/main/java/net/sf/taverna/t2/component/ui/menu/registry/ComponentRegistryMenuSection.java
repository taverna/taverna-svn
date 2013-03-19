/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.registry;

import java.net.URI;

import net.sf.taverna.t2.component.ui.menu.ComponentMenu;
import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

/**
 * @author alanrw
 *
 */
public class ComponentRegistryMenuSection extends AbstractMenuSection {
	
	public static final URI COMPONENT_REGISTRY_SECTION = URI.create("http://taverna.sf.net/2008/t2workbench/menu#componentRegistrySection");
	
	public ComponentRegistryMenuSection() {
		super(ComponentMenu.COMPONENT, 100 , COMPONENT_REGISTRY_SECTION);
	}

}
