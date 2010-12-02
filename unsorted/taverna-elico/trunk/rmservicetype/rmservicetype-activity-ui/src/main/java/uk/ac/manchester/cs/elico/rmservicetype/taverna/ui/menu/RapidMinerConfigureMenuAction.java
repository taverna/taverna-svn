package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.menu;

import javax.swing.Action;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivity;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config.RapidMinerConfigureAction;

public class RapidMinerConfigureMenuAction extends
		AbstractConfigureActivityMenuAction<RapidMinerActivity> {

	public RapidMinerConfigureMenuAction() {
		super(RapidMinerActivity.class);
	}

	@Override
	protected Action createAction() {
		RapidMinerActivity a = findActivity();
		Action result = null;
		result = new RapidMinerConfigureAction(findActivity(),
				getParentFrame());
		result.putValue(Action.NAME, "Configure Rapid Miner service");
		addMenuDots(result);
		return result;
	}

}
