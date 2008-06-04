package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class DummyView {
	
	public static void main(String[] args) throws Exception {
		
		DummyView view = new DummyView();
		
		Activity<?> a = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		bean.setScript("hello this is a script");
		
		//Outputs
		List<ActivityOutputPortDefinitionBean> outputPortDefinitions = new ArrayList<ActivityOutputPortDefinitionBean>();
		ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
		outputPortBean.setDepth(1);
		outputPortBean.setGranularDepth(0);
		List<String> mimeTypes = new ArrayList<String>();
		mimeTypes.add("text/plain");
		outputPortBean.setMimeTypes(mimeTypes );
		outputPortBean.setName("output1");
		outputPortDefinitions.add(outputPortBean);
		
		//Inputs
		List<ActivityInputPortDefinitionBean> iPB = new ArrayList<ActivityInputPortDefinitionBean>();
		ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();
		activityInputPortDefinitionBean.setAllowsLiteralValues(true);
		activityInputPortDefinitionBean.setDepth(1);
		List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes = new ArrayList<Class<? extends ReferenceScheme<?>>>();
		handledReferenceSchemes.add(FileReferenceScheme.class);
		activityInputPortDefinitionBean.setHandledReferenceSchemes(handledReferenceSchemes);
		List<String> mimeTypes2 = new ArrayList<String>();
		mimeTypes2.add("text/html");
		activityInputPortDefinitionBean.setMimeTypes(mimeTypes2);
		activityInputPortDefinitionBean.setName("input1");
		activityInputPortDefinitionBean.setTranslatedElementType(String.class);
		iPB.add(activityInputPortDefinitionBean);
		
		ActivityInputPortDefinitionBean activityInputPortDefinitionBean2 = new ActivityInputPortDefinitionBean();
		activityInputPortDefinitionBean2.setAllowsLiteralValues(false);
		activityInputPortDefinitionBean2.setDepth(3);
		List<Class<? extends ReferenceScheme<?>>> handledReferenceSchemes2 = new ArrayList<Class<? extends ReferenceScheme<?>>>();
		handledReferenceSchemes2.add(HttpReferenceScheme.class);
		activityInputPortDefinitionBean2.setHandledReferenceSchemes(handledReferenceSchemes);
		List<String> mimeTypes3 = new ArrayList<String>();
		mimeTypes3.add("text/plain");
		activityInputPortDefinitionBean2.setMimeTypes(mimeTypes3);
		activityInputPortDefinitionBean2.setName("inputstuff");
		activityInputPortDefinitionBean2.setTranslatedElementType(Integer.class);
		iPB.add(activityInputPortDefinitionBean2);
		
		
		bean.setInputPortDefinitions(iPB);
		bean.setOutputPortDefinitions(outputPortDefinitions);
		List<String> dependencies = new ArrayList<String>();
		dependencies.add("group1:artifact1:1.1.1");
		bean.setDependencies(dependencies );
		
		
		
		
		
		try {
			((BeanshellActivity)a).configure(bean);
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Dataflow dataflow = EditsRegistry.getEdits().createDataflow();
		Processor processor = EditsRegistry.getEdits().createProcessor("processor1");
		EditsRegistry.getEdits().getAddActivityEdit(processor, a).doEdit();
		
//		a=new WSDLActivity();
//		WSDLActivityConfigurationBean b=new WSDLActivityConfigurationBean();
//		b.setOperation("getReport");
//		b.setWsdl("http://discover.nci.nih.gov/gominer/xfire/GMService?wsdl");
//		((WSDLActivity)a).configure(b);
//		
//		a=new SoaplabActivity();
//		SoaplabActivityConfigurationBean sb = new SoaplabActivityConfigurationBean();
//		sb.setEndpoint("http://www.ebi.ac.uk/soaplab/services/edit.seqret");
//		((SoaplabActivity)a).configure(sb);
		
//		StringConstantActivity s=new StringConstantActivity();
//		StringConstantConfigurationBean ss=new StringConstantConfigurationBean();
//		ss.setValue("monkey");
//		((StringConstantActivity)s).configure(ss);
//		EditsRegistry.getEdits().getAddActivityEdit(processor, s).doEdit();
//		
//		ProcessorContextualView procContextView = new ProcessorContextualView(processor);
		
		
		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(a);

		ActivityContextualView viewType = viewFactoryForBeanType.getView(a);
		
//		System.out.println(viewType.getClass().getCanonicalName());
		
//		view.setSize(new Dimension(500, 400));
//		procContextView.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		procContextView.setVisible(true);
		((JFrame) viewType).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		((Component) viewType).setVisible(true);
		
	}
	
	

}
