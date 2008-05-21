package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Test;

public class TestWSDLActivityContextualView {

	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		Activity<?> a=new WSDLActivity();
		WSDLActivityConfigurationBean b=new WSDLActivityConfigurationBean();
		b.setOperation("getReport");
		b.setWsdl("http://discover.nci.nih.gov/gominer/xfire/GMService?wsdl");
		((WSDLActivity)a).configure(b);
		
		
		ActivityViewFactory factory = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(a);
		assertTrue("Factory should be WSDLActivityViewFactory",factory instanceof WSDLActivityViewFactory);
		ActivityView<?> view = factory.getViewType(a);
		assertTrue("The view should be WSDLActivityContextualView",view instanceof WSDLActivityContextualView);
	}
}
