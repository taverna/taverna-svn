package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAnnotationContainer;

public class ServiceAnnotationContainerImpl extends
		AbstractMutableAnnotatedThing implements ActivityAnnotationContainer {

	private Activity<?> theService;
	
	public ServiceAnnotationContainerImpl(Activity<?> service) {
		this.theService = service;
	}
	
	public Activity<?> getService() {
		return this.theService;
	}

}
