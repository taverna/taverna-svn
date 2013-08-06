package net.sf.taverna.t2.activities.interaction.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityType;
import net.sf.taverna.t2.activities.interaction.config.InteractionActivityConfigureAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class InteractionActivityConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<InteractionActivity> {

	public InteractionActivityConfigureMenuAction() {
		super(InteractionActivity.class);
	}

	@Override
	protected Action createAction() {
		final InteractionActivity a = this.findActivity();
		Action result = null;
		if (a.getConfiguration().getInteractionActivityType()
				.equals(InteractionActivityType.LocallyPresentedHtml)) {
			result = new InteractionActivityConfigureAction(
					this.findActivity(), this.getParentFrame());
			result.putValue(Action.NAME, "Configure interaction service");
			this.addMenuDots(result);
		}
		return result;
	}

}
