package net.sf.taverna.activities.localworker.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.activities.localworker.views.LocalworkerActivityConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

/**
 * The {@link LocalworkerActivity}s have pre-defined scripts, ports etc in a
 * serialised form on disk. So if the user wants to change them they have to do
 * so at own risk.
 * 
 * @author Ian Dunlop
 * 
 */
@SuppressWarnings("serial")
public class LocalworkerActivityConfigurationAction extends
		ActivityConfigurationAction<LocalworkerActivity> {

	private final Frame owner;

	public LocalworkerActivityConfigurationAction(LocalworkerActivity activity,
			Frame owner) {
		super(activity);
		this.owner = owner;
	}

	/**
	 * Pops up a {@link JOptionPane} warning the user that they change things at
	 * their own risk
	 */
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		// tell the use that this is a local worker and if they change it then
		// to be careful
		final LocalworkerActivityConfigView localworkerConfigView = new LocalworkerActivityConfigView(
				(LocalworkerActivity) getActivity());
		final JDialog frame = new JDialog(owner,true);
		frame.add(localworkerConfigView);
		frame.setSize(500, 500);
		
		localworkerConfigView.setButtonClickedListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// FIXME when the user clicks config on a local worker it should
				// add it as a user defined one to the activity palette
				frame.setVisible(false);
				if (localworkerConfigView.isConfigurationChanged()) {
					configureActivity(localworkerConfigView.getConfiguration());
				}
			}

		});
		
		Object[] options = { "Continue", "Cancel" };
		int n = JOptionPane
				.showOptionDialog(
						frame,
						"Changing the properties of a Local Worker may affect the behaviour.  Do you want to contine",
						"WARNING", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
						// custom Icon
						options, options[0]);
		

		if (n == 0) {
			// continue was clicked so prepare for config
			frame.setVisible(true);
		} else {
			// do nothing
		}
	}

}
