package net.sf.taverna.t2.activities.dataflow.views;

import java.awt.Frame;

import javax.help.CSH;
import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.dataflow.actions.DataflowActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class DataflowActivityContextualView extends
		HTMLBasedActivityContextualView<Dataflow> {

	public DataflowActivityContextualView(Activity<?> activity) {
		super(activity);
		CSH
				.setHelpIDString(this,
						"net.sf.taverna.t2.activities.dataflow.views.DataflowActivityContextualView");
	}

	@Override
	protected String getRawTableRowsHtml() {

		return ((DataflowActivity) getActivity()).getConfiguration()
				.getLocalName();
	}

	@Override
	protected String getViewTitle() {
		return "Dataflow Contextual View";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new DataflowActivityConfigurationAction(
				(DataflowActivity) getActivity(), owner);
	}

}
