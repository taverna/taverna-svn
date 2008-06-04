package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantActivityDropTarget;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTextArea;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTransferHandler;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class DragActivitiesToHereComponent extends JPanel implements
		UIComponentSPI {

	public DragActivitiesToHereComponent() {
		setLayout(new GridBagLayout());
		initialise();
	}

	private void initialise() {
		StringConstantTextArea dropTextArea = new StringConstantTextArea(
				"String Constant 2");
		dropTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		StringConstantActivityDropTarget stringConstantActivityDropTarget2 = new StringConstantActivityDropTarget(
				dropTextArea);
		dropTextArea.setDropTarget(stringConstantActivityDropTarget2);
		
		StringConstantTransferHandler handler2 = new StringConstantTransferHandler();
		dropTextArea.setTransferHandler(handler2);
		
		GridBagConstraints panelConstraint = new GridBagConstraints();
		panelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraint.gridx = 0;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0;
		panelConstraint.weighty = 0;
		panelConstraint.fill = GridBagConstraints.BOTH;
		add(dropTextArea, panelConstraint);
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Drag stuff here";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

}
