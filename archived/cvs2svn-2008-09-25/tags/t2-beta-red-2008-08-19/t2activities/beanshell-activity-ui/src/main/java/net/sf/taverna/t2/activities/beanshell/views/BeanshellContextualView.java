package net.sf.taverna.t2.activities.beanshell.views;

import java.awt.Frame;

import javax.help.CSH;
import javax.swing.Action;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.actions.BeanshellActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * A simple non editable HTML table view over a {@link BeanshellActivity}.
 * Clicking on the configure button shows the editable
 * {@link BeanshellConfigView}
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * 
 */
@SuppressWarnings("serial")
public class BeanshellContextualView extends
		HTMLBasedActivityContextualView<BeanshellActivityConfigurationBean> {

	public BeanshellContextualView(Activity<?> activity) {
		super(activity);
		init();
	}

	private void init() {
		CSH
		.setHelpIDString(
				this,
		"net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.BeanshellContextualView");
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
		return "Beanshell contextual view";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new BeanshellActivityConfigurationAction(
				(BeanshellActivity) getActivity(), owner);
	}

}
