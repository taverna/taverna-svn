/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;

/**
 * @author alanrw
 * 
 */
public class ComponentSection extends AbstractMenuSection {
	public static final String COMPONENT_SECTION = "Components";
	public static final URI componentSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/components");
	public static final URI editSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/edit");

	public ComponentSection() {
		super(editSection, 100, componentSection);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}
}
