package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import javax.swing.Action;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.BeanshellActvityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;

/**
 * A simple non editable HTML table view over a {@link BeanshellActivity}.
 * Clicking on the configure button shows the editable
 * {@link BeanshellConfigView}
 * 
 * @author Ian Dunlop
 * 
 */
public class BeanshellContextualView extends
		HTMLBasedActivityContextualView<BeanshellActivityConfigurationBean> {

	public BeanshellContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		// TODO Auto-generated method stub
		String html = "<tr><th>Input Port Name</th><th>Depth</th></tr>";
		for (ActivityInputPortDefinitionBean bean : getConfigBean()
				.getInputPortDefinitions()) {
			html = html + "<tr><td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td></tr>";
		}
		return html;
	}

	@Override
	protected String getViewTitle() {
		// TODO Auto-generated method stub
		return "Beanshell Contextual View";
	}

	@Override
	protected Action getConfigureAction() {
		// TODO Auto-generated method stub
		return new BeanshellActvityConfigurationAction(
				(BeanshellActivity) getActivity());

	}

}
