package net.sf.taverna.t2.activities.interaction.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.config.InteractionActivityConfigureAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class InteractionActivityConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<InteractionActivity> {

	public InteractionActivityConfigureMenuAction() {
		super(InteractionActivity.class);
	}

	@Override
	protected Action createAction() {
		InteractionActivity a = findActivity();
		Action result = null;
		result = new InteractionActivityConfigureAction(findActivity(),
				getParentFrame());
		result.putValue(Action.NAME, "Configure example service");
		addMenuDots(result);
		return result;
	}

}
