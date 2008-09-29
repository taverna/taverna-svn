package net.sf.taverna.t2.activities.stringconstant.views;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class StringConstantActivityViewFactory implements ActivityViewFactory<StringConstantActivity> {

	
	public boolean canHandle(Activity<?> activity) {
		return activity instanceof StringConstantActivity;
	}

	public ActivityContextualView<?> getView(StringConstantActivity activity) {
		return new StringConstantActivityContextualView(activity);
	}

}
