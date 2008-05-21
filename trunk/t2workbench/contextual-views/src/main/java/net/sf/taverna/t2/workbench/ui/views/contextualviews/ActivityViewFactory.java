package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class ActivityViewFactory<ActivityType> {
	
	public abstract ActivityView<?> getViewType(ActivityType activity);

	public abstract boolean canHandle(Activity<?> activity);

}