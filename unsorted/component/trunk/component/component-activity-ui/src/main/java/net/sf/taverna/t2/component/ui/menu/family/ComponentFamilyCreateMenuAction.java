/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.family;

import static net.sf.taverna.t2.component.ui.menu.family.ComponentFamilyMenuSection.COMPONENT_FAMILY_SECTION;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 * 
 */
public class ComponentFamilyCreateMenuAction extends AbstractMenuAction {
	private static final URI COMPONENT_FAMILY_CREATE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentFamilyCreate");
	private static final Action familyCreateAction = new ComponentFamilyCreateAction();

	public ComponentFamilyCreateMenuAction() {
		super(COMPONENT_FAMILY_SECTION, 400, COMPONENT_FAMILY_CREATE_URI);
	}

	@Override
	protected Action createAction() {
		return familyCreateAction;
	}

}
