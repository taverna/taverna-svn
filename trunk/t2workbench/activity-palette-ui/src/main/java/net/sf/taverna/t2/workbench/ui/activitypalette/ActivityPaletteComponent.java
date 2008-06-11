package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ActivityPaletteComponent extends JPanel implements UIComponentSPI {
	
	public ActivityPaletteComponent() {
		setLayout(new GridBagLayout());
		GridBagConstraints panelConstraint = new GridBagConstraints();
		panelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraint.gridx = 0;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0;
		panelConstraint.weighty = 0;
		panelConstraint.fill = GridBagConstraints.BOTH;
		add(initialise(), panelConstraint);
		JPanel fillerPanel = new JPanel();
		panelConstraint.gridx = 1;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0.1;
		panelConstraint.weighty = 0.1;
		add(fillerPanel, panelConstraint);
	}

	private Component initialise() {
		
		//TODO need to get the activities from something and their partitions
//		JTree ActivityTree = new ActivityTree(model);
		return null;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Activity Palette";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

}
