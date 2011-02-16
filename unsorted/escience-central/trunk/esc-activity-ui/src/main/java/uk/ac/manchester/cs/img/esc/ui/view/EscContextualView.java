package uk.ac.manchester.cs.img.esc.ui.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import uk.ac.manchester.cs.img.esc.EscActivity;
import uk.ac.manchester.cs.img.esc.EscActivityConfigurationBean;
import uk.ac.manchester.cs.img.esc.ui.config.EscConfigureAction;

@SuppressWarnings("serial")
public class EscContextualView extends HTMLBasedActivityContextualView<EscActivityConfigurationBean> {

	public EscContextualView(Activity<?> activity) {
		super(activity);
 	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		// We want to be on top
		return 100;
	}
	
	@Override
	public Action getConfigureAction(final Frame owner) {
		return new EscConfigureAction((EscActivity) getActivity(), owner);
	}

	@Override
	protected String getRawTableRowsHtml() {
		EscActivityConfigurationBean config = getConfigBean();
		String result = "<tr><td>name</td><td>" + config.getName() + "</td></tr>" +
		"<tr><td>id</td><td>" + config.getId() + "</td></tr>" +
		"<tr><td>url</td><td>" + config.getUrl() + "</td></tr>" +
		"<tr><td>polling interval</td><td>" + config.getPollingInterval() + "</td></tr>" +
		"<tr><td>produce report</td><td>" + config.isProduceReport() + "</td></tr>" +
		"<tr><td>produce workflow</td><td>" + config.isProduceWorkflow() + "</td></tr>" +
		"<tr><td>debug</td><td>" + config.isDebug() + "</td></tr>";
		return result;
	}

	@Override
	public String getViewTitle() {
		return "e-Science Central service`";
	}

}
