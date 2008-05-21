package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Test;

public class TestStringConstantContextualView {

	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		Activity<?> a=new StringConstantActivity();
		StringConstantConfigurationBean b=new StringConstantConfigurationBean();
		b.setValue("elvis");
		((StringConstantActivity)a).configure(b);
		
		
		ActivityViewFactory factory = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(a);
		assertTrue("Factory should be StringConstantActivityViewFactory",factory instanceof StringConstantActivityViewFactory);
		ActivityView<?> view = factory.getViewType(a);
		assertTrue("The view should be StringConstantActivityContextualView",view instanceof StringConstantActivityContextualView);
	}
}
