package net.sf.taverna.t2.activities.dataflow.views;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class DataflowActivityViewFactory implements ContextualViewFactory<DataflowActivity>{

	public boolean canHandle(Object object) {
		return object instanceof DataflowActivity;
	}

	public ContextualView getView(DataflowActivity activity) {
		return new DataflowActivityContextualView(activity);
	}

}
