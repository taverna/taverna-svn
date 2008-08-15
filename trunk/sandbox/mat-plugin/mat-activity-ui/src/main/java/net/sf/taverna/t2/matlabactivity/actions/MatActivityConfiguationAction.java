package net.sf.taverna.t2.matlabactivity.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JDialog;
import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.matlabactivity.views.MatActivityConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

/**
 *
 * @author petarj
 */
public class MatActivityConfiguationAction extends ActivityConfigurationAction<MatActivity> {

    private static final long serialVersionUID = -7319085372935246376L;
    private Frame owner;

    public MatActivityConfiguationAction(MatActivity activity, Frame owner) {
        super(activity);
        this.owner = owner;
    }

    public void actionPerformed(ActionEvent e) {
        final MatActivityConfigView matActivityConfigView = new MatActivityConfigView(
                getActivity());
        final JDialog dialog = new JDialog(owner, true);
        dialog.add(matActivityConfigView);
        dialog.setSize(500, 500);
        matActivityConfigView.setButtonClickedListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (matActivityConfigView.isConfigurationChanged()) {
                    configureActivity(matActivityConfigView.getConfiguration());
                }
                dialog.setVisible(false);
            }
        });
        dialog.setVisible(true);
    }
}
