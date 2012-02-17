package net.sf.taverna.t2.activities.interaction.view;

import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.config.InteractionActivityConfigureAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;


@SuppressWarnings("serial")
public class InteractionActivityContextualView extends ContextualView {
	private final InteractionActivity activity;
	private JLabel description = new JLabel("ads");

	public InteractionActivityContextualView(InteractionActivity activity) {
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
		InteractionActivityConfigurationBean configuration = activity
				.getConfiguration();
		return "Example service " + configuration.getTemplateName();
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
		InteractionActivityConfigurationBean configuration = activity
				.getConfiguration();
		description.setText("Template " + configuration.getTemplateName());
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
		return null;
	}

}
