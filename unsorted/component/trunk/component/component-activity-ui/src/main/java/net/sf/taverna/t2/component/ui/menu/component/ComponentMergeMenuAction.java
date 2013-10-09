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
public class ComponentMergeMenuAction extends AbstractMenuAction {

	private static final URI MERGE_COMPONENT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#componentMerge");

	private static Action componentMergeAction = new ComponentMergeAction();

	public ComponentMergeMenuAction() {
		super(COMPONENT_SECTION, 900, MERGE_COMPONENT_URI);
	}

	@Override
	protected Action createAction() {
		return componentMergeAction;
	}
}
