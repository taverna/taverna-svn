package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class StringConstantActivityViewFactory extends ActivityViewFactory<StringConstantActivity> {

	@Override
	public boolean canHandle(Activity<?> activity) {
		return activity instanceof StringConstantActivity;
	}

	@Override
	public ActivityContextualView<?> getView(StringConstantActivity activity) {
		return new StringConstantActivityContextualView(activity);
	}

}
