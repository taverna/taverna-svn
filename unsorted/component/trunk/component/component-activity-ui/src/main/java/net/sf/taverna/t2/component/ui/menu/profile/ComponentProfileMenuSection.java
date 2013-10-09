/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import static net.sf.taverna.t2.component.ui.menu.ComponentMenu.COMPONENT;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

/**
 * @author alanrw
 * 
 */
public class ComponentProfileMenuSection extends AbstractMenuSection {

	public static final URI COMPONENT_PROFILE_SECTION = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentProfileSection");

	public ComponentProfileMenuSection() {
		super(COMPONENT, 200, COMPONENT_PROFILE_SECTION);
	}

}
