package net.sf.taverna.t2.activities.rshell.views;

import java.awt.Frame;

import javax.help.CSH;
import javax.swing.Action;

import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A simple non-editable HTML table view over a {@link RshellActivity}.

 * @author Alex Nenadic
 *
 */
public class RshellActivityContextualView extends
		HTMLBasedActivityContextualView<RshellActivityConfigurationBean> {

	private static final long serialVersionUID = -2423232268033935502L;

	public RshellActivityContextualView(Activity<?> activity) {
		super(activity);
		init();
	}

	private void init() {
		CSH
		.setHelpIDString(
				this,
		"net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.RshellContextualView");
	}
	
	@Override
	protected String getRawTableRowsHtml() {
		// FIXME: fill in the table rows.
		String html = "";
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "Rshell activity";
	}
	
	@Override
	public Action getConfigureAction(Frame owner) {
		//return new RshellActivityConfigurationAction(
			//	(RshellActivity) getActivity(), owner);
		return null;
	}

}
