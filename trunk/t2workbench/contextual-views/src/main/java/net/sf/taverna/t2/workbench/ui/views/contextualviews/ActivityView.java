package net.sf.taverna.t2.workbench.ui.views.contextualviews;



public abstract class ActivityView<ConfigBean> extends ContextualView {

	private ConfigBean configBean;
	
	public ActivityView(ConfigBean configBean) {
		super();
		this.configBean = configBean;
		initView();
	}

	public ConfigBean getConfigBean() {
		return this.configBean;
	}

	public void setConfigBean(ConfigBean configBean) {
		this.configBean = configBean;
	}
}
