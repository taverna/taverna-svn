package idaservicetype.idaservicetype.ui.menu;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import idaservicetype.idaservicetype.IDAActivity;
import idaservicetype.idaservicetype.ui.config.IDAConfigureAction;

public class ExampleConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<IDAActivity> {

	public ExampleConfigureMenuAction() {
		super(IDAActivity.class);
	}

	@Override
	protected Action createAction() {
		IDAActivity a = findActivity();
		Action result = null;
		result = new IDAConfigureAction(findActivity(),
				getParentFrame());
		result.putValue(Action.NAME, "Configure example service");
		addMenuDots(result);
		return result;
	}

}
