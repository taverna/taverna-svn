package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import org.junit.Test;

public class TestContextualViewFactory {
	
	@SuppressWarnings("unchecked")
	@Test
	public void getBeanshellFactoryAndConfigure() throws Exception {
		BeanshellActivity beanshellActivity = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		beanshellActivity.configure(bean);
		
		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(beanshellActivity);
		assertTrue("Was not a  Beanshell view factory", viewFactoryForBeanType instanceof BeanshellActivityViewFactory);
		ContextualView viewType = viewFactoryForBeanType.getView(beanshellActivity);
		assertTrue("Was not a Beanshell view", viewType.getClass().getCanonicalName().equals(BeanshellContextualView.class.getName()));
		
	}

}
