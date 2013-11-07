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
public class ComponentFamilyDeleteMenuAction extends AbstractMenuAction {

	private static final URI COMPONENT_FAMILY_DELETE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentFamilyDelete");

	private static Action familyDeleteAction = new ComponentFamilyDeleteAction();

	public ComponentFamilyDeleteMenuAction() {
		super(ComponentFamilyMenuSection.COMPONENT_FAMILY_SECTION, 500,
				COMPONENT_FAMILY_DELETE_URI);
	}

	@Override
	protected Action createAction() {
		return familyDeleteAction;
	}

}
