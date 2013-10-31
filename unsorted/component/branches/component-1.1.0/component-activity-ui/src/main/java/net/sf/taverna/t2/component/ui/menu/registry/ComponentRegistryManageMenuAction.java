/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.registry;

import static net.sf.taverna.t2.component.ui.menu.registry.ComponentRegistryMenuSection.COMPONENT_REGISTRY_SECTION;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 * 
 */
public class ComponentRegistryManageMenuAction extends AbstractMenuAction {
	private static final URI COMPONENT_REGISTRY_MANAGE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentRegistryManage");
	private static final Action registryManageAction = new ComponentRegistryManageAction();

	public ComponentRegistryManageMenuAction() {
		super(COMPONENT_REGISTRY_SECTION, 100, COMPONENT_REGISTRY_MANAGE_URI);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	protected Action createAction() {
		return registryManageAction;
	}
}
