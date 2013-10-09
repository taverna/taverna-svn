/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

/**
 * @author alanrw
 * 
 */
public class ComponentCopyMenuAction extends AbstractMenuAction {
	private static final URI COPY_COMPONENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentCopy");
	private static final Action componentCopyAction = new ComponentCopyAction();

	public ComponentCopyMenuAction() {
		super(ComponentMenuSection.COMPONENT_SECTION, 800, COPY_COMPONENT_URI);
	}

	@Override
	protected Action createAction() {
		return componentCopyAction;
	}
}
