package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class ContextualView extends JFrame {
	
	protected void initView() {
		setLayout(new BorderLayout());
		add(getMainFrame(), BorderLayout.CENTER);
		setTitle(getViewTitle());
		JPanel buttonFrame = new JPanel();
		add(buttonFrame, BorderLayout.SOUTH);
		buttonFrame.setLayout(new BorderLayout());
		
		buttonFrame.add(new JButton("OK"), BorderLayout.EAST);
		
	}

	protected abstract JComponent getMainFrame();
	
	protected abstract String getViewTitle();

}
