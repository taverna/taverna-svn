/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.registry;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 *
 */
public class ComponentRegistryManageMenuAction extends
		AbstractMenuAction {
	
	private static final URI COMPONENT_REGISTRY_MANAGE_URI = URI.create("http://taverna.sf.net/2008/t2workbench/menu#componentRegistryManage");
	
	private static Action registryManageAction = new ComponentRegistryManageAction();
	
	public ComponentRegistryManageMenuAction() {
		super (ComponentRegistryMenuSection.COMPONENT_REGISTRY_SECTION, 100 , COMPONENT_REGISTRY_MANAGE_URI );
	}
	
	public boolean isEnabled() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return registryManageAction;
	}

}
