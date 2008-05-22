package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class WSDLActivityViewFactory extends ActivityViewFactory<WSDLActivity>{

	@Override
	public boolean canHandle(Activity<?> activity) {
		return activity instanceof WSDLActivity;
	}

	@Override
	public ActivityView<?> getViewType(WSDLActivity activity) {
		return new WSDLActivityContextualView(activity);
	}

}
