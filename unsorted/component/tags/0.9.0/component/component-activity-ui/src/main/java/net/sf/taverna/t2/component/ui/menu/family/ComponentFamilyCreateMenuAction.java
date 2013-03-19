/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.family;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 *
 */
public class ComponentFamilyCreateMenuAction extends AbstractMenuAction {
	
	private static final URI COMPONENT_FAMILY_CREATE_URI = URI.create("http://taverna.sf.net/2008/t2workbench/menu#componentFamilyCreate");
	
	private static Action familyCreateAction = new ComponentFamilyCreateAction();
	
	public ComponentFamilyCreateMenuAction() {
		super(ComponentFamilyMenuSection.COMPONENT_FAMILY_SECTION, 400, COMPONENT_FAMILY_CREATE_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return familyCreateAction;
	}

}
