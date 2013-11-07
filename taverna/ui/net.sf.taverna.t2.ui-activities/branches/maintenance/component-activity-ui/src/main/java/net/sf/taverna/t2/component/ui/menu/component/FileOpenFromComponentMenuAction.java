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
public class FileOpenFromComponentMenuAction extends AbstractMenuAction {
	private static final URI FILE_OPEN_FROM_COMPONENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentOpen");

	public FileOpenFromComponentMenuAction() {
		super(COMPONENT_SECTION, 700, FILE_OPEN_FROM_COMPONENT_URI);
	}

	@Override
	protected Action createAction() {
		return new OpenWorkflowFromComponentAction(null);
	}

}
