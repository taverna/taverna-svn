package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityViewFactory implements ActivityViewFactory<BeanshellActivity>{

	public boolean canHandle(Activity<?> activityClass) {
		return activityClass instanceof BeanshellActivity;
	}

	
	public BeanshellContextualView getView(BeanshellActivity activity) {
		BeanshellContextualView view = new BeanshellContextualView(activity);
		return view;
	}

}
