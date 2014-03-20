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
public class ComponentProfileCopyMenuAction extends AbstractMenuAction {
	private static final URI COMPONENT_PROFILE_COPY_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentProfileCopy");

	private static Action profileCopyAction = new ComponentProfileCopyAction();

	public ComponentProfileCopyMenuAction() {
		super(COMPONENT_PROFILE_SECTION, 250, COMPONENT_PROFILE_COPY_URI);
	}

	@Override
	protected Action createAction() {
		return profileCopyAction;
	}
}
