package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantActivityDropTarget;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantActivityMouseListener;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTextArea;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTransferHandler;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class ActivityDragger extends JFrame {

	private DataFlavor dataFlavor;

	public ActivityDragger() {

		try {
			dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		setLayout(new GridBagLayout());
		final StringConstantTextArea dragTextArea = new StringConstantTextArea(
				"String Constant 1");
		dragTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		StringConstantConfigurationBean bean1 = new StringConstantConfigurationBean();
		bean1.setValue("hello there");
		StringConstantActivity activity1 = new StringConstantActivity();
		try {
			activity1.configure(bean1);
		} catch (ActivityConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		StringConstantTransferHandler handler1 = new StringConstantTransferHandler(
				bean1, activity1);
		dragTextArea.setTransferHandler(handler1);

		StringConstantActivityMouseListener draggableActivityMouseListener = new StringConstantActivityMouseListener(
				dragTextArea);

		dragTextArea.addMouseListener(draggableActivityMouseListener);
		StringConstantActivityDropTarget stringConstantActivityDropTarget = new StringConstantActivityDropTarget(
				dragTextArea);
		dragTextArea.setDropTarget(stringConstantActivityDropTarget);
		dragTextArea.setDragEnabled(true);

		final StringConstantTextArea dropTextArea = new StringConstantTextArea(
				"String Constant 2");
		dropTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		StringConstantConfigurationBean bean2 = new StringConstantConfigurationBean();
		bean2.setValue("how are you doing");
		StringConstantActivity activity2 = new StringConstantActivity();
		try {
			activity2.configure(bean2);
		} catch (ActivityConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringConstantTransferHandler handler2 = new StringConstantTransferHandler(
				bean2, activity2);
		dropTextArea.setTransferHandler(handler2);

		StringConstantActivityMouseListener draggableActivityMouseListener2 = new StringConstantActivityMouseListener(
				dropTextArea);

		dropTextArea.addMouseListener(draggableActivityMouseListener2);
		dropTextArea.setDragEnabled(true);
		StringConstantActivityDropTarget stringConstantActivityDropTarget2 = new StringConstantActivityDropTarget(
				dropTextArea);
		dropTextArea.setDropTarget(stringConstantActivityDropTarget2);

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weightx = 0;
		outerConstraint.weighty = 0;
		outerConstraint.fill = GridBagConstraints.BOTH;

		add(dragTextArea, outerConstraint);
		outerConstraint.gridy = 1;
		add(dropTextArea, outerConstraint);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ActivityDragger activityDragger = new ActivityDragger();
		activityDragger.setSize(500, 500);
		activityDragger.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		activityDragger.setVisible(true);
	}

}
