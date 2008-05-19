package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public abstract class ActivityViewFactory<ActivityType, ViewType> {

	private Class<ActivityType> activityType;
	private Class<ViewType> viewType;

	
	protected ActivityViewFactory(Class<ActivityType> activityType, Class<ViewType> viewType) {
		this.activityType = activityType;
		this.viewType = viewType;
	}
	
	public Class<ActivityType> getActivityType() {
		return this.activityType;
	}
	
	public abstract ActivityView getViewType(ActivityType activity);

	public abstract boolean canHandle(Activity activityClass);

}