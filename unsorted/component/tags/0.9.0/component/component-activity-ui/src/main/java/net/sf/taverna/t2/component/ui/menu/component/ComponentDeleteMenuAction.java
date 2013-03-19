/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.component.ui.menu.ComponentMenu;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.actions.SaveWorkflowAction;
import net.sf.taverna.t2.workbench.file.impl.menu.FileOpenMenuSection;

/**
 * @author alanrw
 *
 */
public class ComponentDeleteMenuAction extends AbstractMenuAction {

	private static final URI DELETE_COMPONENT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#componentDelete");
	
	private static Action componentDeleteAction = new ComponentDeleteAction();
	

	public ComponentDeleteMenuAction() {
		super(ComponentMenuSection.COMPONENT_SECTION, 1200, DELETE_COMPONENT_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return componentDeleteAction;
	}

}
