package net.sf.taverna.t2.component.ui.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;

@SuppressWarnings("serial")
public class ComponentActivityContextualView extends HTMLBasedActivityContextualView<ComponentActivityConfigurationBean> {

	public ComponentActivityContextualView(ComponentActivity activity) {
		super(activity);
		init();
	}

	private void init() {
	}

	@Override
	public String getViewTitle() {
		return "Component service";
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
		return new ComponentConfigureAction((ComponentActivity) getActivity(), owner);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "";
		
		html += "<tr><td>Component registry base</td><td>" + getConfigBean().getRegistryBase().toString() + "</td></tr>";
		html += "<tr><td>Component family</td><td>" + getConfigBean().getFamilyName() + "</td></tr>";
		html += "<tr><td>Component name</td><td>" + getConfigBean().getComponentName() + "</td></tr>";
		html += "<tr><td>Component version</td><td>" + getConfigBean().getComponentVersion() + "</td></tr>";
		
		return html;
	}

}
