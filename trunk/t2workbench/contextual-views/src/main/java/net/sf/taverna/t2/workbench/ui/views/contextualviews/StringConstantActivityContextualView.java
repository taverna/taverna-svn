package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;

public class StringConstantActivityContextualView extends HTMLBasedActivityContextualView<StringConstantConfigurationBean> {

	private static final long serialVersionUID = -553974544001808511L;

	public StringConstantActivityContextualView(
			StringConstantConfigurationBean configBean) {
		super(configBean);
	}

	@Override
	protected String getViewTitle() {
		return "String constant activity";
	}


	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><td>Value</td><td>"+getConfigBean().getValue()+"</td></tr>";
		return html;
	}

	@Override
	protected void setNewValues() {
		// TODO Auto-generated method stub
		
	}

}
