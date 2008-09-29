package net.sf.taverna.t2.activities.beanshell.views;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityViewFactory implements ActivityViewFactory<BeanshellActivity>{

	public boolean canHandle(Activity<?> activityClass) {
		//changed since local worker sub classes beanshell which means instanceof can't be used any more
		return activityClass.getClass().isAssignableFrom(BeanshellActivity.class);
	}

	
	public BeanshellContextualView getView(BeanshellActivity activity) {
		BeanshellContextualView view = new BeanshellContextualView(activity);
		return view;
	}

}
