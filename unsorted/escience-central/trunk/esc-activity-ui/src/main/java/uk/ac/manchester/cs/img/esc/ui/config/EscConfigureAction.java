package uk.ac.manchester.cs.img.esc.ui.config;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

import uk.ac.manchester.cs.img.esc.EscActivity;
import uk.ac.manchester.cs.img.esc.EscActivityConfigurationBean;

@SuppressWarnings("serial")
public class EscConfigureAction
		extends
		ActivityConfigurationAction<EscActivity, EscActivityConfigurationBean> {

	public EscConfigureAction(EscActivity activity, Frame owner) {
		super(activity);
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog<EscActivity, EscActivityConfigurationBean> currentDialog = ActivityConfigurationAction
				.getDialog(getActivity());
		if (currentDialog != null) {
			currentDialog.toFront();
			return;
		}
		EscConfigurationPanel panel = new EscConfigurationPanel(
				getActivity());
		ActivityConfigurationDialog<EscActivity, EscActivityConfigurationBean> dialog = new ActivityConfigurationDialog<EscActivity, EscActivityConfigurationBean>(
				getActivity(), panel);

		ActivityConfigurationAction.setDialog(getActivity(), dialog);

	}

}
