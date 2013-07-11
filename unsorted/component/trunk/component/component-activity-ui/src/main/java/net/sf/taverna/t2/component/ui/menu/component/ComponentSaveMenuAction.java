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
public class ComponentSaveMenuAction extends AbstractMenuAction {

	private static final URI SAVE_COMPONENT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#componentSave");
	
	private static Action componentSaveAction = new ComponentSaveAction();
	

	public ComponentSaveMenuAction() {
		super(ComponentMenuSection.COMPONENT_SECTION, 1100, SAVE_COMPONENT_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return componentSaveAction;
	}

}
