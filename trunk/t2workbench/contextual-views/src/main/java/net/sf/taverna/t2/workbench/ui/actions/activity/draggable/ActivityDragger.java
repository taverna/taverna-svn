package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellActivityDropTarget;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellActivityMouseListener;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellActivityTransferHandler;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellActivityTransferable;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellTextArea;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantActivityDropTarget;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantActivityMouseListener;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTextArea;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTransferHandler;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

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
		
		
		BeanshellTextArea beanTextArea1 = new BeanshellTextArea("Beanshell here");
		beanTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		
		BeanshellTextArea beanTextArea2 = new BeanshellTextArea("Another Beanshell Over Here");
		beanTextArea2.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		
		
		Activity<?> beanshellActivity1 = new BeanshellActivity();
		BeanshellActivityConfigurationBean beanshellBean1= createBeanshellBean();
		
		Activity<?> beanshellActivity2 = new BeanshellActivity();
		BeanshellActivityConfigurationBean beanshellBean2= createBeanshellBean();
		
		
		try {
			((BeanshellActivity)beanshellActivity1).configure(beanshellBean1);
			((BeanshellActivity)beanshellActivity2).configure(beanshellBean2);
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BeanshellActivityTransferHandler beanshelTransferHandler = new BeanshellActivityTransferHandler(beanshellBean1, (BeanshellActivity) beanshellActivity1);
		beanTextArea1.setTransferHandler(beanshelTransferHandler);
		
		BeanshellActivityMouseListener beanshellMouseListener = new BeanshellActivityMouseListener(beanTextArea1);
		beanTextArea1.addMouseListener(beanshellMouseListener);
		
		BeanshellActivityDropTarget beanshellDropTarget = new BeanshellActivityDropTarget(beanTextArea1);
		beanTextArea1.setDropTarget(beanshellDropTarget);
		
		
		BeanshellActivityTransferHandler beanshelTransferHandler2 = new BeanshellActivityTransferHandler(beanshellBean2, (BeanshellActivity) beanshellActivity2);
		beanTextArea2.setTransferHandler(beanshelTransferHandler2);
		
		BeanshellActivityMouseListener beanshellMouseListener2 = new BeanshellActivityMouseListener(beanTextArea1);
		beanTextArea2.addMouseListener(beanshellMouseListener2);
		
		BeanshellActivityDropTarget beanshellDropTarget2 = new BeanshellActivityDropTarget(beanTextArea2);
		beanTextArea2.setDropTarget(beanshellDropTarget2);
		
		
		
		
		

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
		outerConstraint.gridy = 2;
		add(beanTextArea1, outerConstraint);
		outerConstraint.gridy = 3;
		add(beanTextArea2, outerConstraint);

	}
	
	
	public BeanshellActivityConfigurationBean createBeanshellBean() {
		BeanshellActivityConfigurationBean beanshellBean1 = new BeanshellActivityConfigurationBean();
		beanshellBean1.setScript("hello this is a script");
		
		//Outputs
		List<ActivityOutputPortDefinitionBean> outputPortDefinitions1 = new ArrayList<ActivityOutputPortDefinitionBean>();
		ActivityOutputPortDefinitionBean outputPortBean1 = new ActivityOutputPortDefinitionBean();
		outputPortBean1.setDepth(1);
		outputPortBean1.setGranularDepth(0);
		List<String> mimeTypes1 = new ArrayList<String>();
		mimeTypes1.add("text/plain");
		outputPortBean1.setMimeTypes(mimeTypes1 );
		outputPortBean1.setName("output1");
		outputPortDefinitions1.add(outputPortBean1);
		
		//Inputs
		List<ActivityInputPortDefinitionBean> iPB1 = new ArrayList<ActivityInputPortDefinitionBean>();
		ActivityInputPortDefinitionBean activityInputPortDefinitionBean1 = new ActivityInputPortDefinitionBean();
		activityInputPortDefinitionBean1.setAllowsLiteralValues(true);
		activityInputPortDefinitionBean1.setDepth(1);
		List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes1 = new ArrayList<Class<? extends ReferenceScheme<?>>>();
		handledReferenceSchemes1.add(FileReferenceScheme.class);
		activityInputPortDefinitionBean1.setHandledReferenceSchemes(handledReferenceSchemes1);
		List<String> mimeTypes2 = new ArrayList<String>();
		mimeTypes2.add("text/html");
		activityInputPortDefinitionBean1.setMimeTypes(mimeTypes2);
		activityInputPortDefinitionBean1.setName("input1");
		activityInputPortDefinitionBean1.setTranslatedElementType(String.class);
		iPB1.add(activityInputPortDefinitionBean1);
		
		ActivityInputPortDefinitionBean activityInputPortDefinitionBean2 = new ActivityInputPortDefinitionBean();
		activityInputPortDefinitionBean2.setAllowsLiteralValues(false);
		activityInputPortDefinitionBean2.setDepth(3);
		List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes2 = new ArrayList<Class<? extends ReferenceScheme<?>>>();
		handledReferenceSchemes2.add(HttpReferenceScheme.class);
		activityInputPortDefinitionBean2.setHandledReferenceSchemes(handledReferenceSchemes1);
		List<String> mimeTypes3 = new ArrayList<String>();
		mimeTypes3.add("text/plain");
		activityInputPortDefinitionBean2.setMimeTypes(mimeTypes3);
		activityInputPortDefinitionBean2.setName("inputstuff");
		activityInputPortDefinitionBean2.setTranslatedElementType(Integer.class);
		iPB1.add(activityInputPortDefinitionBean2);
		
		
		beanshellBean1.setInputPortDefinitions(iPB1);
		beanshellBean1.setOutputPortDefinitions(outputPortDefinitions1);
		List<String> dependencies = new ArrayList<String>();
		dependencies.add("group1:artifact1:1.1.1");
		beanshellBean1.setDependencies(dependencies );
		return beanshellBean1;
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
