package net.sf.taverna.t2.activities.soaplab.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class SoaplabActivityConfigurationAction extends
		ActivityConfigurationAction<SoaplabActivity> {

	private static final long serialVersionUID = 5076721332542691094L;
	private final Frame owner;

	public SoaplabActivityConfigurationAction(SoaplabActivity activity,Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent action) {

		final SoaplabConfigurationPanel panel = new SoaplabConfigurationPanel(
				getActivity().getConfiguration());
		final JDialog frame = new JDialog(owner,true);
		frame.getContentPane().add(panel);
		panel.setOKClickedListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (panel.validateValues()) {
					int interval = 0;
					int intervalMax = 0;
					double backoff = 1.1;

					if (panel.isAllowPolling()) {
						interval = panel.getInterval();
						intervalMax = panel.getIntervalMax();
						backoff = panel.getBackoff();
					}

					SoaplabActivityConfigurationBean bean = getActivity()
							.getConfiguration();
					bean.setPollingBackoff(backoff);
					bean.setPollingInterval(interval);
					bean.setPollingIntervalMax(intervalMax);

					configureActivity(bean);
					
					frame.setVisible(false);
				}
			}

		});

		panel.setCancelClickedListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}

		});

		frame.pack();
		
		frame.setVisible(true);
	}

}
