package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class WSDLActivityContextualView extends HTMLBasedActivityContextualView<WSDLActivityConfigurationBean> {

	private static final long serialVersionUID = -4329643934083676113L;

	public WSDLActivityContextualView(Activity<?> activity) {
		super(activity);
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

}
