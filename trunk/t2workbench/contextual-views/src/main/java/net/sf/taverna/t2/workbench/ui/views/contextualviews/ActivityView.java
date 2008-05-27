package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;



public abstract class ActivityView<ConfigBean> extends ContextualView {

	private Activity<?> activity;
	
	public ActivityView(Activity<?> activity) {
		super();
		this.activity = activity;
		initView();
	}

	public ConfigBean getConfigBean() {
		return (ConfigBean)activity.getConfiguration();
	}

	protected Activity<?> getActivity() {
		return this.activity;
	}
}
