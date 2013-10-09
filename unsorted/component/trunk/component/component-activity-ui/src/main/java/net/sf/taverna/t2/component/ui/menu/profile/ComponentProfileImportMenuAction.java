/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.profile;

import static net.sf.taverna.t2.component.ui.menu.profile.ComponentProfileMenuSection.COMPONENT_PROFILE_SECTION;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 * 
 */
public class ComponentProfileImportMenuAction extends AbstractMenuAction {

	private static final URI COMPONENT_PROFILE_IMPORT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentProfileImport");

	private static Action profileImportAction = new ComponentProfileImportAction();

	public ComponentProfileImportMenuAction() {
		super(COMPONENT_PROFILE_SECTION, 200, COMPONENT_PROFILE_IMPORT_URI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return profileImportAction;
	}

}
