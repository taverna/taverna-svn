/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * @author alanrw
 * 
 */
public class ReplaceByComponentMenuAction extends AbstractContextualMenuAction {
	private static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");
	private static final ReplaceByComponentAction action = new ReplaceByComponentAction();

	public ReplaceByComponentMenuAction() {
		super(configureSection, 75);
	}

	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		if (!super.isEnabled())
			return false;
		return (selection instanceof Processor);
	}

	@Override
	protected Action createAction() {
		action.setSelection((Processor) getContextualSelection().getSelection());
		return action;
	}
}
