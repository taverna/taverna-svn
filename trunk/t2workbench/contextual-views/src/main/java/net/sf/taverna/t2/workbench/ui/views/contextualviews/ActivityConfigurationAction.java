package net.sf.taverna.t2.workbench.ui.views.contextualviews;

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
