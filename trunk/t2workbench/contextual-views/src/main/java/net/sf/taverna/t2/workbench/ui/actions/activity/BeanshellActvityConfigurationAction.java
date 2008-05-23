package net.sf.taverna.t2.workbench.ui.actions.activity;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.BeanshellConfigView;

public class BeanshellActvityConfigurationAction extends ActivityConfigurationAction<BeanshellActivity>{

	private static final long serialVersionUID = 8462133163666495672L;

	public BeanshellActvityConfigurationAction(BeanshellActivity activity) {
		super(activity);
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
			}
			
		});
	}

}
