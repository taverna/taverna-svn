/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 *
 */
public class ComponentCloseMenuAction extends AbstractMenuAction {

	private static final URI CLOSE_COMPONENT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#componentClose");
	
	private static Action componentCloseAction = new ComponentCloseAction();
	

	public ComponentCloseMenuAction() {
		super(ComponentMenuSection.COMPONENT_SECTION, 1000, CLOSE_COMPONENT_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return componentCloseAction;
	}

}
