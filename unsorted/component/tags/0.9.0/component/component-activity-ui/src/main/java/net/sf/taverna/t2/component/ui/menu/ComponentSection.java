/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.net.URI;

import net.sf.taverna.t2.ui.menu.AbstractMenuSection;
import net.sf.taverna.t2.ui.menu.ContextualSelection;

/**
 * @author alanrw
 *
 */
public class ComponentSection extends AbstractMenuSection {

	public static final String COMPONENT_SECTION = "Components";
	public static final URI componentSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/components");
	private ContextualSelection contextualSelection;
	
	public static final URI editSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/edit");
	
	public ComponentSection() {
		super(editSection, 100, componentSection);
	}
	
	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return super.isEnabled();
	}
	
	private ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}
	
}
