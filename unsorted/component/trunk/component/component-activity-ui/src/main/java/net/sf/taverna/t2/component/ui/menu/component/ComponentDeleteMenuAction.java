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
public class ComponentDeleteMenuAction extends AbstractMenuAction {
	private static final URI DELETE_COMPONENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentDelete");
	private static final Action componentDeleteAction = new ComponentDeleteAction();

	public ComponentDeleteMenuAction() {
		super(COMPONENT_SECTION, 1200, DELETE_COMPONENT_URI);
	}

	@Override
	protected Action createAction() {
		return componentDeleteAction;
	}
}
