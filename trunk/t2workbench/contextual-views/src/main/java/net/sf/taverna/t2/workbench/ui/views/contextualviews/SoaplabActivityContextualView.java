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
		return new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				final SoaplabConfigurationPanel panel = new SoaplabConfigurationPanel(getConfigBean());
				final JDialog dialog = new JDialog();
				dialog.add(panel);
				panel.setOKClickedListener(new ActionListener() {
					SoaplabActivity activity = (SoaplabActivity)getActivity();
					public void actionPerformed(ActionEvent e) {
						if (panel.validateValues()) {
							int interval=0;
							int intervalMax=0;
							double backoff=1.1;
							
							if (panel.isAllowPolling()) {
								interval=panel.getInterval();
								intervalMax=panel.getIntervalMax();
								backoff=panel.getBackoff();
							}
							
							SoaplabActivityConfigurationBean bean = activity.getConfiguration();
							bean.setPollingBackoff(backoff);
							bean.setPollingInterval(interval);
							bean.setPollingIntervalMax(intervalMax);
							
							try {
								activity.configure(bean);
							} catch (ActivityConfigurationException ex) {
								JOptionPane.showMessageDialog(null, "There was an error configuring the Soaplab activity with the new settings:"+ex.getMessage(),"Activity update error",JOptionPane.ERROR_MESSAGE);
							}
							dialog.setVisible(false);
						}
					}
					
				});
				
				panel.setCancelClickedListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						dialog.setVisible(false);
					}
					
				});
				
				dialog.setModal(true);
				dialog.setVisible(true);
				
				refreshView();
			}
		};
	}

	@Override
	protected void setNewValues() {
		// TODO Auto-generated method stub
		
	}
}
