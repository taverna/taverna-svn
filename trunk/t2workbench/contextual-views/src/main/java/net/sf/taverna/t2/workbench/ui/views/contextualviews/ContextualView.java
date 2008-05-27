package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Action;
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
		
		buttonFrame.add(createButtonPanel(),BorderLayout.EAST);
		
		
		
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		JButton configureButton = new JButton(getConfigureAction());
		configureButton.setEnabled(false);
		
		if (getConfigureAction()!=null) {
			configureButton.setAction(getConfigureAction());
			configureButton.setEnabled(true);
		}
		configureButton.setText("Configure");
		buttonPanel.add(configureButton);
		
		return buttonPanel;
	}

	protected abstract JComponent getMainFrame();
	
	protected abstract String getViewTitle();
	
	protected Action getConfigureAction() {
		return null;
	}
	

}
