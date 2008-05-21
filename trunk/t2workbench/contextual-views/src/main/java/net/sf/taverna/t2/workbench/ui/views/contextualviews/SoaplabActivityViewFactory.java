package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class SoaplabActivityViewFactory extends ActivityViewFactory<SoaplabActivity> {

	@Override
	public boolean canHandle(Activity<?> activity) {
		return activity instanceof SoaplabActivity;
	}

	@Override
	public ActivityView<?> getViewType(SoaplabActivity activity) {
		return new SoaplabActivityContextualView(activity.getConfiguration());
	}

}
