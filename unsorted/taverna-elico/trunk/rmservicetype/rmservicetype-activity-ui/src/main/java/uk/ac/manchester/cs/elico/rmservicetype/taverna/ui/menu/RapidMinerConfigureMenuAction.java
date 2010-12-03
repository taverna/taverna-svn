package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.menu;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config.RapidMinerConfigureAction;

public class RapidMinerConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<RapidMinerExampleActivity> {

	public RapidMinerConfigureMenuAction() {
		super(RapidMinerExampleActivity.class);
	}

	@Override
	protected Action createAction() {
		RapidMinerExampleActivity a = findActivity();
		Action result = null;
		result = new RapidMinerConfigureAction(findActivity(),
				getParentFrame());
		result.putValue(Action.NAME, "Configure example service");
		addMenuDots(result);
		return result;
	}

}
