package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class ActivityViewFactoryRegistry extends
		SPIRegistry<ActivityViewFactory> {

	private static ActivityViewFactoryRegistry instance;

	protected ActivityViewFactoryRegistry() {
		super(ActivityViewFactory.class);
	}
	
	public static synchronized ActivityViewFactoryRegistry getInstance() {
		if (instance == null) {
			instance = new ActivityViewFactoryRegistry();
		}
		return instance;
	}

	public ActivityViewFactory getViewFactoryForBeanType(Activity activityClass) {
		for (ActivityViewFactory factory : getInstances()) {
			if (factory.canHandle(activityClass)) {
				return factory;
			}
		}
		throw new IllegalArgumentException(
				"Can't find factory for activity view class " + activityClass);
	}

}
