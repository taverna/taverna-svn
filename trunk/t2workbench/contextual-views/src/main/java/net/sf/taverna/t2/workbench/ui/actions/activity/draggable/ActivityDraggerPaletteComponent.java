package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

//import com.apple.audio.hardware.ADevicePropertyListener;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.localworker.translator.LocalworkerTranslator;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.compatibility.activity.ActivityTranslationException;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellActivityDropTarget;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellActivityTransferHandler;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell.BeanshellTextArea;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantActivityDropTarget;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTextArea;
import net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant.StringConstantTransferHandler;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class ActivityDraggerPaletteComponent extends JPanel implements
		UIComponentSPI {

	private JPanel activitiesPanel;
	private int activityPanelGridy = 0;

	public ActivityDraggerPaletteComponent() {
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

	private JPanel initialise() {
		
		activitiesPanel = new JPanel();
		activitiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, "Activity Palette",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		activitiesPanel.setLayout(new GridBagLayout());
		GridBagConstraints activityPanelConstraint = new GridBagConstraints();
		activityPanelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		activityPanelConstraint.gridx = 0;
		activityPanelConstraint.gridy = activityPanelGridy;
		activityPanelConstraint.weightx = 0;
		activityPanelConstraint.weighty = 0;
		activityPanelConstraint.fill = GridBagConstraints.BOTH;
		
		StringConstantTextArea dragTextArea = new StringConstantTextArea(
				"String Constant");
		dragTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		StringConstantConfigurationBean bean1 = new StringConstantConfigurationBean();
//		bean1.setValue("hello there");
		StringConstantActivity activity1 = new StringConstantActivity();
		try {
			activity1.configure(bean1);
		} catch (ActivityConfigurationException e2) {
			e2.printStackTrace();
		}
		StringConstantTransferHandler handler1 = new StringConstantTransferHandler(
				bean1, activity1);
		dragTextArea.setTransferHandler(handler1);

//		ActivityMouseListener draggableActivityMouseListener = new ActivityMouseListener(
//				dragTextArea);
//
//		dragTextArea.addMouseListener(draggableActivityMouseListener);
//		StringConstantActivityDropTarget stringConstantActivityDropTarget = new StringConstantActivityDropTarget(
//				dragTextArea);
//		dragTextArea.setDropTarget(stringConstantActivityDropTarget);
		dragTextArea.setDragEnabled(true);
		activitiesPanel.add(dragTextArea, activityPanelConstraint);
		activityPanelGridy++;
		
		BeanshellTextArea beanTextArea1 = new BeanshellTextArea("Beanshell");
		beanTextArea1.setBorder(javax.swing.BorderFactory.createTitledBorder(
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
		
//		ActivityMouseListener beanshellMouseListener = new ActivityMouseListener(beanTextArea1);
//		beanTextArea1.addMouseListener(beanshellMouseListener);
		
//		BeanshellActivityDropTarget beanshellDropTarget = new BeanshellActivityDropTarget(beanTextArea1);
//		beanTextArea1.setDropTarget(beanshellDropTarget);
		activityPanelConstraint.gridy = activityPanelGridy;
		activitiesPanel.add(beanTextArea1, activityPanelConstraint);
		activityPanelGridy++;
		getAllLocalWorkers();
		return activitiesPanel;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Activity Pallette";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}
	
	private void getAllLocalWorkers() {
		LocalworkerTranslator localworkerTranslator = new LocalworkerTranslator();
		for (Entry<String,String> entry:localworkerTranslator.getLocalWorkerToScript().entrySet()){
			try {
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println("Local worker: " + key);
				localworkerTranslator.getScript(key);
				BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
				bean.setScript(localworkerTranslator.getScript(key));
				BeanshellActivity activity = new BeanshellActivity();
				activity.configure(bean);
				BeanshellTextArea beanTextArea = new BeanshellTextArea(value);
				beanTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
						null, null,
						javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
						javax.swing.border.TitledBorder.DEFAULT_POSITION,
						new java.awt.Font("Lucida Grande", 1, 12)));
				BeanshellActivityTransferHandler beanshelTransferHandler = new BeanshellActivityTransferHandler(bean, (BeanshellActivity) activity);
				beanTextArea.setTransferHandler(beanshelTransferHandler);
				
//				ActivityMouseListener beanshellMouseListener = new ActivityMouseListener(beanTextArea);
//				beanTextArea.addMouseListener(beanshellMouseListener);
				
//				BeanshellActivityDropTarget beanshellDropTarget = new BeanshellActivityDropTarget(beanTextArea);
//				beanTextArea.setDropTarget(beanshellDropTarget);
				GridBagConstraints activityPanelConstraint = new GridBagConstraints();
				activityPanelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
				activityPanelConstraint.gridx = 0;
				activityPanelConstraint.gridy = activityPanelGridy;
				activityPanelConstraint.weightx = 0;
				activityPanelConstraint.weighty = 0;
				activityPanelConstraint.fill = GridBagConstraints.BOTH;
				activitiesPanel.add(beanTextArea, activityPanelConstraint);
				activityPanelGridy++;
			} catch (ActivityTranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ActivityConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private BeanshellActivityConfigurationBean createBeanshellBean() {
		BeanshellActivityConfigurationBean beanshellBean1 = new BeanshellActivityConfigurationBean();
//		beanshellBean1.setScript("hello this is a script");
		
//		//Outputs
//		List<ActivityOutputPortDefinitionBean> outputPortDefinitions1 = new ArrayList<ActivityOutputPortDefinitionBean>();
//		ActivityOutputPortDefinitionBean outputPortBean1 = new ActivityOutputPortDefinitionBean();
//		outputPortBean1.setDepth(1);
//		outputPortBean1.setGranularDepth(0);
//		List<String> mimeTypes1 = new ArrayList<String>();
//		mimeTypes1.add("text/plain");
//		outputPortBean1.setMimeTypes(mimeTypes1 );
//		outputPortBean1.setName("output1");
//		outputPortDefinitions1.add(outputPortBean1);
//		
		//Inputs
//		List<ActivityInputPortDefinitionBean> iPB1 = new ArrayList<ActivityInputPortDefinitionBean>();
//		ActivityInputPortDefinitionBean activityInputPortDefinitionBean1 = new ActivityInputPortDefinitionBean();
//		activityInputPortDefinitionBean1.setAllowsLiteralValues(true);
//		activityInputPortDefinitionBean1.setDepth(1);
//		List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes1 = new ArrayList<Class<? extends ReferenceScheme<?>>>();
//		handledReferenceSchemes1.add(FileReferenceScheme.class);
//		activityInputPortDefinitionBean1.setHandledReferenceSchemes(handledReferenceSchemes1);
//		List<String> mimeTypes2 = new ArrayList<String>();
//		mimeTypes2.add("text/html");
//		activityInputPortDefinitionBean1.setMimeTypes(mimeTypes2);
//		activityInputPortDefinitionBean1.setName("input1");
//		activityInputPortDefinitionBean1.setTranslatedElementType(String.class);
//		iPB1.add(activityInputPortDefinitionBean1);
//		
//		ActivityInputPortDefinitionBean activityInputPortDefinitionBean2 = new ActivityInputPortDefinitionBean();
//		activityInputPortDefinitionBean2.setAllowsLiteralValues(false);
//		activityInputPortDefinitionBean2.setDepth(3);
//		List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes2 = new ArrayList<Class<? extends ReferenceScheme<?>>>();
//		handledReferenceSchemes2.add(HttpReferenceScheme.class);
//		activityInputPortDefinitionBean2.setHandledReferenceSchemes(handledReferenceSchemes1);
//		List<String> mimeTypes3 = new ArrayList<String>();
//		mimeTypes3.add("text/plain");
//		activityInputPortDefinitionBean2.setMimeTypes(mimeTypes3);
//		activityInputPortDefinitionBean2.setName("inputstuff");
//		activityInputPortDefinitionBean2.setTranslatedElementType(Integer.class);
//		iPB1.add(activityInputPortDefinitionBean2);
		
		
//		beanshellBean1.setInputPortDefinitions(iPB1);
//		beanshellBean1.setOutputPortDefinitions(outputPortDefinitions1);
//		List<String> dependencies = new ArrayList<String>();
//		dependencies.add("group1:artifact1:1.1.1");
//		beanshellBean1.setDependencies(dependencies );
		return beanshellBean1;
	}

}
