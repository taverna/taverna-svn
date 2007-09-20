package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAnnotationContainer;

public class ActivityAnnotationContainerImpl extends
		AbstractMutableAnnotatedThing implements ActivityAnnotationContainer {

	private Activity<?> theActivity;
	
	public ActivityAnnotationContainerImpl(Activity<?> activity) {
		this.theActivity = activity;
	}
	
	public Activity<?> getActivity() {
		return this.theActivity;
	}

}
