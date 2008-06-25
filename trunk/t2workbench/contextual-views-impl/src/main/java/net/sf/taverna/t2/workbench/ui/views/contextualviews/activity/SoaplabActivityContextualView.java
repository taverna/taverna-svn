package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workbench.ui.actions.activity.SoaplabActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class SoaplabActivityContextualView extends HTMLBasedActivityContextualView<SoaplabActivityConfigurationBean> {

	private static final long serialVersionUID = -6470801873448104509L;

	public SoaplabActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getViewTitle() {
		return "Soaplab";
	}

	@Override
	protected String getRawTableRowsHtml() {
		SoaplabActivityConfigurationBean bean = getConfigBean();
		String html="<tr><td>Endpoint</td><td>"+bean.getEndpoint()+"</td></tr>";
		html+="<tr><td>Polling interval</td><td>"+bean.getPollingInterval()+"</td></tr>";
		html+="<tr><td>Polling backoff</td><td>"+bean.getPollingBackoff()+"</td></tr>";
		html+="<tr><td>Polling interval max</td><td>"+bean.getPollingIntervalMax()+"</td></tr>";
		return html;
	}

	@SuppressWarnings("serial")
	@Override
	public Action getConfigureAction() {
		return new SoaplabActivityConfigurationAction((SoaplabActivity)getActivity()) {

			@Override
			public void actionPerformed(ActionEvent action) {
				super.actionPerformed(action);
				refreshView();
			}
		};
	}

}
