package net.sf.taverna.t2.activities.localworker.views;

import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class LocalworkerActivityViewFactory implements
		ContextualViewFactory<LocalworkerActivity> {

	public boolean canHandle(Object object) {
		return object instanceof LocalworkerActivity;
	}

	public ContextualView getView(LocalworkerActivity activity) {
		return new LocalworkerActivityContextualView(activity);
	}

}
