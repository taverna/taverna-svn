package net.sf.taverna.t2.activities.dataflow.views;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class DataflowActivityViewFactory implements ActivityViewFactory<DataflowActivity>{

	public boolean canHandle(Activity<?> activity) {
		return activity instanceof DataflowActivity;
	}

	public ContextualView getView(DataflowActivity activity) {
		return new DataflowActivityContextualView(activity);
	}

}
