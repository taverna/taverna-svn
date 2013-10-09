/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static net.sf.taverna.t2.component.ui.menu.component.ComponentMenuSection.COMPONENT_SECTION;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 * 
 */
public class ComponentWorkflowCreatorMenuAction extends AbstractMenuAction {
	private static final URI COMPONENT_WORKFLOW_CREATE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentCreate");

	private static final Action creatorAction = new ComponentWorkflowCreatorAction();

	public ComponentWorkflowCreatorMenuAction() {
		super(COMPONENT_SECTION, 600, COMPONENT_WORKFLOW_CREATE_URI);
	}

	@Override
	protected Action createAction() {
		return creatorAction;
	}
}
