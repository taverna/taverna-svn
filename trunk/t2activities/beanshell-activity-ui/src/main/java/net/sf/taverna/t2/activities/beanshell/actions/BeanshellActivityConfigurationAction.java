package net.sf.taverna.t2.activities.beanshell.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellConfigView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

@SuppressWarnings("serial")
public class BeanshellActivityConfigurationAction extends ActivityConfigurationAction<BeanshellActivity>{

	private final Frame owner;

	public BeanshellActivityConfigurationAction(BeanshellActivity activity, Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		final BeanshellConfigView beanshellConfigView = new BeanshellConfigView((BeanshellActivity)getActivity());
		final JDialog dialog = new JDialog(owner,true);
		dialog.add(beanshellConfigView);
		dialog.setSize(500, 500);
		beanshellConfigView.setButtonClickedListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (beanshellConfigView.isConfigurationChanged()) {
					configureActivity(beanshellConfigView.getConfiguration());
				}
				dialog.setVisible(false);
			}
			
		});
		dialog.setVisible(true);
		
		
	}

}
