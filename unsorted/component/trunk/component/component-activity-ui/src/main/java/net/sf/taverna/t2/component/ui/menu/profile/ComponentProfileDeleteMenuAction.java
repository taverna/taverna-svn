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
public class ComponentProfileDeleteMenuAction extends AbstractMenuAction {

	private static final URI COMPONENT_PROFILE_DELETE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentProfileDelete");

	private static Action profileDeleteAction = new ComponentProfileDeleteAction();

	public ComponentProfileDeleteMenuAction() {
		super(COMPONENT_PROFILE_SECTION, 300, COMPONENT_PROFILE_DELETE_URI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return profileDeleteAction;
	}

}
