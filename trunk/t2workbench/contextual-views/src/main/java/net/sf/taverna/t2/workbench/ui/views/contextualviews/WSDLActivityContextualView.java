package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;

public class WSDLActivityContextualView extends HTMLBasedActivityContextualView<WSDLActivityConfigurationBean> {

	private static final long serialVersionUID = -4329643934083676113L;

	public WSDLActivityContextualView(WSDLActivityConfigurationBean configBean) {
		super(configBean);
	}

	@Override
	protected String getViewTitle() {
		return "WSDL based activity";
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summary="<tr><td>WSDL</td><td>"+getConfigBean().getWsdl();
		summary+="</td></tr><tr><td>Operation</td><td>"+getConfigBean().getOperation();
		summary+="</td></tr>";
		return summary;
	}

	@Override
	protected void setNewValues() {
		// TODO Auto-generated method stub
		
	}

}
