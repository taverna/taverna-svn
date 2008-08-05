package net.sf.taverna.t2.activities.stringconstant.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Before;
import org.junit.Test;

public class TestStringConstantContextualView {
	Activity<?> activity;
	
	@Before
	public void setup() throws ActivityConfigurationException {
		activity=new StringConstantActivity();
		StringConstantConfigurationBean b=new StringConstantConfigurationBean();
		b.setValue("elvis");
		((StringConstantActivity)activity).configure(b);
	}
	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {ContextualViewFactory factory = ContextualViewFactoryRegistry.getInstance().getViewFactoryForObject(activity);
		assertTrue("Factory should be StringConstantActivityViewFactory",factory instanceof StringConstantActivityViewFactory);
		ContextualView view = factory.getView(activity);
		assertTrue("The view should be StringConstantActivityContextualView",view instanceof StringConstantActivityContextualView);
	}
	
	@Test
	public void testGetConfigureAction() throws Exception {
		ContextualView view = new StringConstantActivityContextualView(activity);
		assertNotNull("The action should not be null",view.getConfigureAction(null));
		assertTrue("Should be a StringConstantActivityConfigurationAction",view.getConfigureAction(null) instanceof StringConstantActivityConfigurationAction);
	}
	
}
