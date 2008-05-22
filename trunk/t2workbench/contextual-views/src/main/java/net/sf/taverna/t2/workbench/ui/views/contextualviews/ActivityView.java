package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;



public abstract class ActivityView<ConfigBean> extends ContextualView {

	private Activity<?> activity;
	ConfigBean configBean;
	
	public ActivityView(Activity<?> activity) {
		super();
		this.activity = activity;
		this.configBean=(ConfigBean)activity.getConfiguration();
		initView();
	}

	public ConfigBean getConfigBean() {
		return this.configBean;
	}

	protected Activity<?> getActivity() {
		return this.activity;
	}
}
