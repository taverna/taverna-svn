package net.sf.taverna.t2.workbench.ui.actions.activity;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class SoaplabActivityConfigurationAction extends
		ActivityConfigurationAction<SoaplabActivity> {

	private static final long serialVersionUID = 5076721332542691094L;

	public SoaplabActivityConfigurationAction(SoaplabActivity activity) {
		super(activity);
	}

	public void actionPerformed(ActionEvent action) {

		final SoaplabConfigurationPanel panel = new SoaplabConfigurationPanel(
				getActivity().getConfiguration());
		final JFrame frame = new JFrame();
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

					try {
						getActivity().configure(bean);
					} catch (ActivityConfigurationException ex) {
						JOptionPane.showMessageDialog(null,
								"There was an error configuring the Soaplab activity with the new settings:"
										+ ex.getMessage(),
								"Activity update error",
								JOptionPane.ERROR_MESSAGE);
					}
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
		//frame.setModal(true);
		frame.setVisible(true);
	}

}
