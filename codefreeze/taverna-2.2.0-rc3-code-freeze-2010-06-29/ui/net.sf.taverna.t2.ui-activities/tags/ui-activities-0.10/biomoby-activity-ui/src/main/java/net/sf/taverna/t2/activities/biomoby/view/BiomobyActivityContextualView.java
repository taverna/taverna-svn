/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivityConfigurationBean;
import net.sf.taverna.t2.activities.biomoby.actions.BiomobyActivityConfigurationAction;
import net.sf.taverna.t2.activities.biomoby.actions.MobyParserAction;
import net.sf.taverna.t2.activities.biomoby.actions.MobyServiceDetailsAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

@SuppressWarnings("serial")
public class BiomobyActivityContextualView extends HTMLBasedActivityContextualView<BiomobyActivityConfigurationBean> {

	@Override
	public Action getConfigureAction(Frame owner) {
		BiomobyActivity activity = (BiomobyActivity)getActivity();
		if (activity.getMobyService() != null && activity.containsSecondaries()) {
			return new BiomobyActivityConfigurationAction((BiomobyActivity)getActivity(),owner);
		}
		else {
			return null;
		}	
	}

	public BiomobyActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><td>Endpoint</td><td>"+getConfigBean().getMobyEndpoint()+"</td></tr>";
		html += "<tr><td>Authority</td><td>"+getConfigBean().getAuthorityName()+"</td></tr>";
		html += "<tr><td>Service</td><td>"+getConfigBean().getServiceName()+"</td></tr>";
		if (getConfigBean().getSecondaries().size()>0) {
			html+="<tr><th colspan='2' align='left'>Secondaries</th></tr>";
			for (Entry<String, String> entry : getConfigBean().getSecondaries().entrySet()) {
				html+="<tr><td>"+entry.getKey()+"</td><td>"+entry.getValue()+"</td></tr>";
			}
		}
		return html;
	}

	@Override
	public String getViewTitle() {
		return "Biomoby activity";
	}
	
	/**
	 * Gets the component from the {@link HTMLBasedActivityContextualView} and
	 * adds buttons to it allowing Moby service details
	 */
	@Override
	public JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();
		JPanel flowPanel = new JPanel(new FlowLayout());
		
		BiomobyActivity activity = (BiomobyActivity)getActivity();

		JButton button = new JButton(new MobyServiceDetailsAction(activity,null));
		flowPanel.add(button);
		if (activity.getMobyService()!=null) {
			JButton button2 = new JButton(new MobyParserAction(activity,null));
			flowPanel.add(button2);
		}
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

}
