package net.sf.taverna.t2.workbench.ui.actions.activity;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class ActivityConfigurationAction<A extends Activity<?>> extends AbstractAction {

	private A activity;
	
	public ActivityConfigurationAction(A activity) {
		this.activity=activity;
	}
	
	protected A getActivity() {
		return activity;
	}

}
