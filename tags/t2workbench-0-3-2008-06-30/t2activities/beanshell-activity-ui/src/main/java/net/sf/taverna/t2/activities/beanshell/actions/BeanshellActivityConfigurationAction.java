package net.sf.taverna.t2.activities.beanshell.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellConfigView;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellContextualView;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

@SuppressWarnings("serial")
public class BeanshellActivityConfigurationAction extends ActivityConfigurationAction<BeanshellActivity>{

	private final BeanshellContextualView view;

	public BeanshellActivityConfigurationAction(BeanshellActivity activity, BeanshellContextualView view) {
		super(activity);
		this.view = view;
	}

	public void actionPerformed(ActionEvent e) {
		BeanshellConfigView beanshellConfigView = new BeanshellConfigView((BeanshellActivity)getActivity());
		final JFrame frame = new JFrame();
		frame.add(beanshellConfigView);
		frame.setSize(500, 500);
		frame.setVisible(true);
		
		beanshellConfigView.setButtonClickedListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				view.refreshView();
			}
			
		});
	}

}
