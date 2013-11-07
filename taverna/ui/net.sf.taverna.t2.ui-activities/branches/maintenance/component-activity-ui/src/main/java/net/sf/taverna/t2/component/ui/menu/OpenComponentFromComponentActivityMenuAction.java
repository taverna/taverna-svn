/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.net.URI;
import java.util.List;

import javax.swing.Action;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * @author alanrw
 * 
 */
public class OpenComponentFromComponentActivityMenuAction extends
		AbstractContextualMenuAction {
	private static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");

	private static OpenComponentFromComponentActivityAction action = new OpenComponentFromComponentActivityAction();

	public OpenComponentFromComponentActivityMenuAction() {
		super(configureSection, 75);
	}

	@Override
	public boolean isEnabled() {
		return (getSelectedActivity() != null);
	}

	@Override
	protected Action createAction() {
		action.setSelection(getSelectedActivity());
		return action;
	}

	private ComponentActivity getSelectedActivity() {
		Object selection = getContextualSelection().getSelection();
		if (!super.isEnabled() || !(selection instanceof Processor))
			return null;

		Processor p = (Processor) selection;
		List<? extends Activity<?>> activities = p.getActivityList();
		if (activities.isEmpty())
			return null;

		Activity<?> a = activities.get(0);
		if (a instanceof ComponentActivity)
			return (ComponentActivity) a;

		return null;
	}
}
