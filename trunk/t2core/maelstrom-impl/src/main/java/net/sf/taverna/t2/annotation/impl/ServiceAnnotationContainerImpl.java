package net.sf.taverna.t2.annotation.impl;

import net.sf.taverna.t2.workflowmodel.processor.service.Service;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceAnnotationContainer;

public class ServiceAnnotationContainerImpl extends
		AbstractMutableAnnotatedThing implements ServiceAnnotationContainer {

	private Service<?> theService;
	
	public ServiceAnnotationContainerImpl(Service<?> service) {
		this.theService = service;
	}
	
	public Service<?> getService() {
		return this.theService;
	}

}
