package net.sf.taverna.t2.activities.biomart.views;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BiomartActivityViewFactory implements ActivityViewFactory<BiomartActivity> {

	
	public boolean canHandle(Activity<?> activity) {
		return activity instanceof BiomartActivity;
	}

	
	public ActivityContextualView<?> getView(BiomartActivity activity) {
		return new BiomartActivityContextualView(activity);
	}

}
