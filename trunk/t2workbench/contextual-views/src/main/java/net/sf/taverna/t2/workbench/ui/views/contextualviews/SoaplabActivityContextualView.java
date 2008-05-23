package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.SoaplabActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class SoaplabActivityContextualView extends HTMLBasedActivityContextualView<SoaplabActivityConfigurationBean> {

	private static final long serialVersionUID = -6470801873448104509L;

	public SoaplabActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getViewTitle() {
		return Messages.getString("soaplab.activity.view.name"); //$NON-NLS-1$
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
	protected Action getConfigureAction() {
		return new SoaplabActivityConfigurationAction((SoaplabActivity)getActivity()) {

			@Override
			public void actionPerformed(ActionEvent action) {
				super.actionPerformed(action);
				refreshView();
			}
		};
	}

	@Override
	protected void setNewValues() {
		// TODO Auto-generated method stub
		
	}
}
