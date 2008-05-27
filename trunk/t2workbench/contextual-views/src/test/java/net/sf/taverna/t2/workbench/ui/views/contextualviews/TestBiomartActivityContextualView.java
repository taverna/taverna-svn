package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.BiomartActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

public class TestBiomartActivityContextualView {
	Activity<?> activity;
	@Before
	public void setup() throws Exception {
		activity = new BiomartActivity();
		BiomartActivityConfigurationBean biomartConfigBean = new BiomartActivityConfigurationBean();
		Element el = getQueryElement("biomart-query.xml");
		biomartConfigBean.setQuery(el);
		((BiomartActivity)activity).configure(biomartConfigBean);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		
		
		ActivityViewFactory factory = ActivityViewFactoryRegistry.getInstance()
				.getViewFactoryForBeanType(activity);
		assertTrue("Factory should be BiomartActivityViewFactory",
				factory instanceof BiomartActivityViewFactory);
		ActivityView<?> view = factory.getViewType(activity);
		assertTrue("The view should be BiomartActivityContextualView",
				view instanceof BiomartActivityContextualView);
	}
	
	@Test
	public void testConfigurationAction() throws Exception {
		BiomartActivityContextualView view = new BiomartActivityContextualView(activity);
		assertNotNull("The view should provide a configuration action",view.getConfigureAction());
		assertTrue("The configuration action should be an instance of BiomartActivityConfigurationAction",view.getConfigureAction() instanceof BiomartActivityConfigurationAction);
	}

	private Element getQueryElement(String resourceName) throws Exception {
		InputStream inStream = TestBiomartActivityContextualView.class
				.getResourceAsStream("/"+resourceName);

		if (inStream == null)
			throw new IOException(
					"Unable to find resource for:"
							+ resourceName);
		SAXBuilder builder = new SAXBuilder();
		return builder.build(inStream).detachRootElement();
	}
	
	private void run() throws Exception {
		setup();
		BiomartActivityContextualView view = new BiomartActivityContextualView(activity);
		view.setVisible(true);
	}
	public static void main(String[] args) throws Exception {
		new TestBiomartActivityContextualView().run();
	}

}
