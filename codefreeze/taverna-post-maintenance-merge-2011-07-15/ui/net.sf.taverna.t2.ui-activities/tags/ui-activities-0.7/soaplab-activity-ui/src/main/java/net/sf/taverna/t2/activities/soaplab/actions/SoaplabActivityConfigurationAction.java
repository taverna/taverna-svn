/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.soaplab.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

public class SoaplabActivityConfigurationAction extends
		ActivityConfigurationAction<SoaplabActivity, SoaplabActivityConfigurationBean> {

	private static final long serialVersionUID = 5076721332542691094L;
	private final Frame owner;

	public SoaplabActivityConfigurationAction(SoaplabActivity activity,Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent action) {

		final SoaplabConfigurationPanel panel = new SoaplabConfigurationPanel(
				getActivity().getConfiguration());
		final HelpEnabledDialog frame = new HelpEnabledDialog(owner,"Soaplab Activity Configuration", true, null);
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

//					SoaplabActivityConfigurationBean bean = getActivity()
//							.getConfiguration();
					SoaplabActivityConfigurationBean bean = new SoaplabActivityConfigurationBean();
					bean.setPollingBackoff(backoff);
					bean.setPollingInterval(interval);
					bean.setPollingIntervalMax(intervalMax);
					String endpoint = getActivity().getConfiguration().getEndpoint();
					bean.setEndpoint(endpoint);

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
