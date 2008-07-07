package net.sf.taverna.t2.activities.stringconstant.views;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class StringConstantActivityContextualView extends HTMLBasedActivityContextualView<StringConstantConfigurationBean> {

	private static final long serialVersionUID = -553974544001808511L;

	public StringConstantActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getViewTitle() {
		return "String constant activity";
	}


	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><td>Value</td><td>"+getConfigBean().getValue()+"</td></tr>";
		return html;
	}

	@SuppressWarnings("serial")
	@Override
	public Action getConfigureAction(Frame owner) {
		
		return new StringConstantActivityConfigurationAction((StringConstantActivity)getActivity(),owner) {

			@Override
			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				refreshView();
			}
			
		};
	}

}


