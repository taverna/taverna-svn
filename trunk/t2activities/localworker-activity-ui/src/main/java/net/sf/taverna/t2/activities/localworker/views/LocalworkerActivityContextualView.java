package net.sf.taverna.t2.activities.localworker.views;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.activities.localworker.actions.LocalworkerActivityConfigurationAction;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class LocalworkerActivityContextualView extends HTMLBasedActivityContextualView<BeanshellActivityConfigurationBean>{

	public LocalworkerActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><th>Input Port Name</th><th>Depth</th></tr>";
		for (ActivityInputPortDefinitionBean bean : getConfigBean()
				.getInputPortDefinitions()) {
			html = html + "<tr><td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td></tr>";
		}
		html = html
				+ "<tr><th>Output Port Name</th><th>Depth</th><th>Granular Depth</th></tr>";
		for (ActivityOutputPortDefinitionBean bean : getConfigBean()
				.getOutputPortDefinitions()) {
			html = html + "<tr></td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td><td>" + bean.getGranularDepth()
					+ "</td></tr>";
		}
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "Local Worker";
	}
	
	@Override
	public Action getConfigureAction(Frame owner) {
		return new LocalworkerActivityConfigurationAction(
				(LocalworkerActivity)getActivity(), owner);
	}

}
