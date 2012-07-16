package net.sf.taverna.t2.component.ui.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.ui.config.ComponentConfigureAction;

@SuppressWarnings("serial")
public class ComponentContextualView extends ContextualView {
	private final ComponentActivity activity;
	private JLabel description = new JLabel("ads");

	public ComponentContextualView(ComponentActivity activity) {
		this.activity = activity;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		JPanel jPanel = new JPanel();
		jPanel.add(description);
		refreshView();
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		ComponentActivityConfigurationBean configuration = activity
				.getConfiguration();
		return "Component service " + StringUtils.abbreviate(configuration.getDataflowString(), 40);
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
		ComponentActivityConfigurationBean configuration = activity
				.getConfiguration();
		description.setText("Component service " +
				StringUtils.abbreviate(configuration.getDataflowString(), 40));
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
		return new ComponentConfigureAction(activity, owner);
	}

}
