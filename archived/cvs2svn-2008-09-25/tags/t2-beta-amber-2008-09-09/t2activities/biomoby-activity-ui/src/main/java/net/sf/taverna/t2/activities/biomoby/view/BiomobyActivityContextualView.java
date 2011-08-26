/*******************************************************************************
 * This file is a component of the Taverna project, and is licensed  under the
 *  GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 ******************************************************************************/
package net.sf.taverna.t2.activities.biomoby.view;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivityConfigurationBean;
import net.sf.taverna.t2.activities.biomoby.actions.BiomobyActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

@SuppressWarnings("serial")
public class BiomobyActivityContextualView extends HTMLBasedActivityContextualView<BiomobyActivityConfigurationBean> {

	@Override
	public Action getConfigureAction(Frame owner) {
		return new BiomobyActivityConfigurationAction((BiomobyActivity)getActivity(),owner);
	}

	public BiomobyActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><td>Endpoint</td><td>"+getConfigBean().getMobyEndpoint()+"</td></tr>";
		html += "<tr><td>Authority</td><td>"+getConfigBean().getAuthorityName()+"</td></tr>";
		html += "<tr><td>Service</td><td>"+getConfigBean().getServiceName()+"</td></tr>";
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "Biomoby activity";
	}

}
