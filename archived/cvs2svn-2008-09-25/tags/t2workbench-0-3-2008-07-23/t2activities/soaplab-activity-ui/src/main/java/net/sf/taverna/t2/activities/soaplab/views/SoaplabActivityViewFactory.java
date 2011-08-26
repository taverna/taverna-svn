package net.sf.taverna.t2.activities.soaplab.views;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class SoaplabActivityViewFactory implements ActivityViewFactory<SoaplabActivity> {

	public boolean canHandle(Activity<?> activity) {
		return activity instanceof SoaplabActivity;
	}


	public ActivityContextualView<?> getView(SoaplabActivity activity) {
		return new SoaplabActivityContextualView(activity);
	}

}
