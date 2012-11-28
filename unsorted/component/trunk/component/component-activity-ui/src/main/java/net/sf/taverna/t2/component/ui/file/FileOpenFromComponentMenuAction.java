/**
 * 
 */
package net.sf.taverna.t2.component.ui.file;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.impl.menu.FileOpenMenuSection;

/**
 * @author alanrw
 *
 */
public class FileOpenFromComponentMenuAction extends AbstractMenuAction {

	private static final URI FILE_OPEN_FROM_COMPONENT_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenComponent");

	public FileOpenFromComponentMenuAction() {
		super(FileOpenMenuSection.FILE_OPEN_SECTION_URI, 35, FILE_OPEN_FROM_COMPONENT_URI);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.ui.menu.AbstractMenuAction#createAction()
	 */
	@Override
	protected Action createAction() {
		return new OpenWorkflowFromComponentAction(null);
	}

}
