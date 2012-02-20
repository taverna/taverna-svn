/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;
import net.sf.taverna.t2.activities.interaction.view.InteractionActivityConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityConfigurationDialog;

/**
 * @author alanrw
 *
 */
public class InteractionActivityConfigurationAction extends
		ActivityConfigurationAction<InteractionActivity, InteractionActivityConfigurationBean> {
	
    public static final String EDIT_INTERACTION = "Edit interaction";

    public InteractionActivityConfigurationAction(InteractionActivity activity, Frame owner) {
            super(activity);
            putValue(NAME, EDIT_INTERACTION );
    }


	@Override
	public void actionPerformed(ActionEvent e) {
		ActivityConfigurationDialog currentDialog = ActivityConfigurationAction.getDialog(getActivity());
        if (currentDialog != null) {
                currentDialog.toFront();
                return;
        }
        final InteractionActivityConfigView interactionConfigView = new InteractionActivityConfigView((InteractionActivity)getActivity());
        final ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean> dialog =
                new ActivityConfigurationDialog<InteractionActivity, InteractionActivityConfigurationBean>(getActivity(), interactionConfigView);

        ActivityConfigurationAction.setDialog(getActivity(), dialog);   
	}

}
