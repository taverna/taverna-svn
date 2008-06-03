package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.WSDLActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.WSDLActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestWSDLActivityContextualView {

	Activity<?> a;
	
	@Before
	public void setUp() throws Exception {
		a=new WSDLActivity();
		WSDLActivityConfigurationBean b=new WSDLActivityConfigurationBean();
		b.setOperation("getReport");
		b.setWsdl("http://www.mygrid.org.uk/taverna-tests/testwsdls/GMService.wsdl");
		((WSDLActivity)a).configure(b);
	}
	
	@SuppressWarnings("unchecked")
	@Ignore("Integration test")
	@Test
	public void testDisovery() throws Exception {
		ActivityViewFactory factory = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(a);
		assertTrue("Factory should be WSDLActivityViewFactory",factory instanceof WSDLActivityViewFactory);
		ActivityContextualView<?> view = factory.getView(a);
		assertTrue("The view should be WSDLActivityContextualView",view instanceof WSDLActivityContextualView);
	}
	
	public void testConfigurationAction() {
		WSDLActivityContextualView view = new WSDLActivityContextualView(a);
		assertNull("WSDL has no configure action, so should be null",view.getConfigureAction());
	}
}
