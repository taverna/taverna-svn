/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 *
 */
public class ComponentWorkflowCreatorMenuAction extends AbstractMenuAction {
	
	public static final URI FILE_RUN_SECTION_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#fileRunSection");
	
	private static final URI COMPONENT_WORKFLOW_CREATE_URI = URI.create("http://taverna.sf.net/2008/t2workbench/menu#componentWorkflowCreate");
	
	public ComponentWorkflowCreatorMenuAction() {
		super(FILE_RUN_SECTION_URI, 10, COMPONENT_WORKFLOW_CREATE_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return new ComponentWorkflowCreatorAction();
	}

}
