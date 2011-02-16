package uk.ac.manchester.cs.img.esc.ui.menu;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import uk.ac.manchester.cs.img.esc.EscActivity;
import uk.ac.manchester.cs.img.esc.ui.config.EscConfigureAction;

public class EscConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<EscActivity> {

	public EscConfigureMenuAction() {
		super(EscActivity.class);
	}

	@Override
	protected Action createAction() {
		EscActivity a = findActivity();
		Action result = null;
		result = new EscConfigureAction(findActivity(),
				getParentFrame());
		result.putValue(Action.NAME, "Configure example service");
		addMenuDots(result);
		return result;
	}

}
