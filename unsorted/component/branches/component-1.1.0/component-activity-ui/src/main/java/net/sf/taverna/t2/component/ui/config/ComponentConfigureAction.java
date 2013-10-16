package net.sf.taverna.t2.component.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.component.ComponentActivity;
import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

@SuppressWarnings("serial")
public class ComponentConfigureAction
		extends
		ActivityConfigurationAction<ComponentActivity, ComponentActivityConfigurationBean> {

	public ComponentConfigureAction(ComponentActivity activity, Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog<ComponentActivity, ComponentActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		ComponentConfigurationPanel panel = new ComponentConfigurationPanel(
				getActivity());
		ActivityConfigurationDialog<ComponentActivity, ComponentActivityConfigurationBean> dialog = new ActivityConfigurationDialog<ComponentActivity, ComponentActivityConfigurationBean>(
				getActivity(), panel);

		ActivityConfigurationAction.setDialog(getActivity(), dialog);

	}

}
