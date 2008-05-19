package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BeanshellActivityViewFactory extends ActivityViewFactory<BeanshellActivity, BeanshellContextualView>{

	public BeanshellActivityViewFactory() {
		super(BeanshellActivity.class, BeanshellContextualView.class);
	}

	@Override
	public boolean canHandle(Activity activityClass) {
		return activityClass instanceof BeanshellActivity;
	}

	@Override
	public BeanshellContextualView getViewType(BeanshellActivity activity) {
		BeanshellActivityConfigurationBean configuration = activity.getConfiguration();
		BeanshellContextualView view = new BeanshellContextualView(configuration);
		return view;
	}

}
