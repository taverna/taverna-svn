package net.sf.taverna.t2.activities.localworker.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;

import org.junit.Test;

public class TestContextualViewFactory {

	@SuppressWarnings("unchecked")
	@Test
	public void getLocalworkerFactoryAndConfigure() throws Exception {
		LocalworkerActivity localworkerActivity = new LocalworkerActivity();
		LocalworkerActivityConfigurationBean bean = new LocalworkerActivityConfigurationBean();
		localworkerActivity.configure(bean);

		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
				.getInstance().getViewFactoryForBeanType(localworkerActivity);
		assertNotNull("The localworker view factory should not be null",
				viewFactoryForBeanType);
		assertTrue(
				"Was not a  Localworker view factory",
				viewFactoryForBeanType instanceof LocalworkerActivityViewFactory);
		ContextualView viewType = viewFactoryForBeanType
				.getView(localworkerActivity);
		assertTrue("Was not a Beanshell view", viewType.getClass()
				.getCanonicalName().equals(
						LocalworkerActivityContextualView.class.getName()));

	}

}
