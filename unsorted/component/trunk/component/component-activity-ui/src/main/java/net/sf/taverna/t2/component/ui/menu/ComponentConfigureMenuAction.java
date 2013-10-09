package net.sf.taverna.t2.component.ui.menu;

import static javax.swing.Action.NAME;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;

public class ComponentConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<ComponentActivity> {

	public ComponentConfigureMenuAction() {
		super(ComponentActivity.class);
	}

	@Override
	protected Action createAction() {
		Action result = null;
		result = new ComponentConfigureAction(findActivity(), getParentFrame());
		result.putValue(NAME, "Configure component");
		addMenuDots(result);
		return result;
	}

}
