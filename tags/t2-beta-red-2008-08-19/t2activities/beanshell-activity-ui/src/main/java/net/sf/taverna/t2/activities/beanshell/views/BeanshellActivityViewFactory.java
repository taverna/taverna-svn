package net.sf.taverna.t2.activities.beanshell.views;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityViewFactory implements ContextualViewFactory<BeanshellActivity>{

	public boolean canHandle(Object object) {
		//changed since local worker sub classes beanshell which means instanceof can't be used any more
		return object.getClass().isAssignableFrom(BeanshellActivity.class);
	}

	
	public BeanshellContextualView getView(BeanshellActivity activity) {
		BeanshellContextualView view = new BeanshellContextualView(activity);
		return view;
	}

}
