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
public class ComponentSearchMenuAction extends AbstractMenuAction {

	private static final URI SEARCH_COMPONENT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#componentSearch");
	
	private static Action componentSearchAction = new ComponentSearchAction();
	

	public ComponentSearchMenuAction() {
		super(ComponentMenuSection.COMPONENT_SECTION, 1500, SEARCH_COMPONENT_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return componentSearchAction;
	}

}
