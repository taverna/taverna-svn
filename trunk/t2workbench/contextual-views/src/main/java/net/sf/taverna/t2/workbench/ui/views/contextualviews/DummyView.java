package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class DummyView {
	
	public static void main(String[] args) throws Exception {
		
		DummyView view = new DummyView();
		
		BeanshellActivity beanshellActivity = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		bean.setScript("hello this is a script");
		try {
			beanshellActivity.configure(bean);
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(beanshellActivity);

		ActivityView viewType = viewFactoryForBeanType.getViewType(beanshellActivity);
		
		System.out.println(viewType.getClass().getCanonicalName());
		
//		view.setSize(new Dimension(500, 400));
		((JFrame) viewType).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		((Component) viewType).setVisible(true);
	}
	
	

}
