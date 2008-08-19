package net.sf.taverna.t2.activities.soaplab.views;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class SoaplabActivityViewFactory implements
		ContextualViewFactory<SoaplabActivity> {

	public boolean canHandle(Object object) {
		return object instanceof SoaplabActivity;
	}

	public ActivityContextualView<?> getView(SoaplabActivity activity) {
		return new SoaplabActivityContextualView(activity);
	}

}
