package net.sf.taverna.workbench.ui.beanshell.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellActivityViewFactory;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

import org.junit.Test;

public class TestContextualViewFactory {
	
	@SuppressWarnings("unchecked")
	@Test
	public void getBeanshellFactoryAndConfigure() throws Exception {
		BeanshellActivity beanshellActivity = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		beanshellActivity.configure(bean);
		
		ContextualViewFactory viewFactoryForBeanType = ContextualViewFactoryRegistry.getInstance().getViewFactoryForObject(beanshellActivity);
		assertNotNull("The beanshsell view factory should not be null",viewFactoryForBeanType);
		assertTrue("Was not a  Beanshell view factory", viewFactoryForBeanType instanceof BeanshellActivityViewFactory);
		ContextualView viewType = viewFactoryForBeanType.getView(beanshellActivity);
		assertTrue("Was not a Beanshell view", viewType.getClass().getCanonicalName().equals(BeanshellContextualView.class.getName()));
		
	}

}
