package net.sf.taverna.t2.activities.localworker.views;


import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class LocalworkerActivityViewFactory implements ActivityViewFactory<LocalworkerActivity>{

	public boolean canHandle(Activity<?> activity) {
		return activity instanceof LocalworkerActivity;
	}

	public ContextualView getView(LocalworkerActivity activity) {
		return new LocalworkerActivityContextualView(activity);
	}

}
