/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.component.ui.menu.ComponentMenu;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.menu.FileOpenMenuSection;

/**
 * @author alanrw
 *
 */
public class FileOpenFromComponentMenuAction extends AbstractMenuAction {

	private static final URI FILE_OPEN_FROM_COMPONENT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#componentOpen");

	public FileOpenFromComponentMenuAction() {
		super(ComponentMenuSection.COMPONENT_SECTION, 700, FILE_OPEN_FROM_COMPONENT_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return new OpenWorkflowFromComponentAction(null);
	}

}
