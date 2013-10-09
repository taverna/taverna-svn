/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static net.sf.taverna.t2.component.ui.menu.ComponentMenu.COMPONENT;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

/**
 * @author alanrw
 * 
 */
public class ComponentMenuSection extends AbstractMenuSection {
	public static final URI COMPONENT_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentSection");

	public ComponentMenuSection() {
		super(COMPONENT, 400, COMPONENT_SECTION);
	}

}
