package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class ContextualView extends JFrame {
	
	protected void initView() {
		// TODO Auto-generated method stub
		setSize(800, 500);
		setLayout(new BorderLayout());
		add(getMainFrame(), BorderLayout.CENTER);
		setTitle(getViewTitle());
		JPanel buttonFrame = new JPanel();
		add(buttonFrame, BorderLayout.SOUTH);
		buttonFrame.setLayout(new BorderLayout());
		JButton OKButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				setNewValues();
				setVisible(false);
			}
			
		});
		OKButton.setText("OK");
		
		buttonFrame.add(OKButton, BorderLayout.EAST);
		
	}

	protected abstract JComponent getMainFrame();
	
	protected abstract void setNewValues();
	
	protected abstract String getViewTitle();
	

}
