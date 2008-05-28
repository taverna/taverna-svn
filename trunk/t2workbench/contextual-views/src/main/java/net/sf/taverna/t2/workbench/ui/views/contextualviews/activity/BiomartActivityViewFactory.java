package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BiomartActivityViewFactory extends ActivityViewFactory<BiomartActivity> {

	@Override
	public boolean canHandle(Activity<?> activity) {
		return activity instanceof BiomartActivity;
	}

	@Override
	public ActivityContextualView<?> getView(BiomartActivity activity) {
		return new BiomartActivityContextualView(activity);
	}

}
