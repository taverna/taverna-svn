/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static net.sf.taverna.t2.component.ui.menu.component.ComponentMenuSection.COMPONENT_SECTION;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 * 
 */
public class ComponentSearchMenuAction extends AbstractMenuAction {
	private static final URI SEARCH_COMPONENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentSearch");
	private static final Action componentSearchAction = new ComponentSearchAction();

	public ComponentSearchMenuAction() {
		super(COMPONENT_SECTION, 1500, SEARCH_COMPONENT_URI);
	}

	@Override
	protected Action createAction() {
		return componentSearchAction;
	}
}
