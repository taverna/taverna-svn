package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

public class TestBiomartActivityContextualView {

	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		Activity<?> a = new BiomartActivity();
		BiomartActivityConfigurationBean biomartConfigBean = new BiomartActivityConfigurationBean();
		Element el = getQueryElement("biomart-query.xml");
		biomartConfigBean.setQuery(el);
		((BiomartActivity)a).configure(biomartConfigBean);
		
		ActivityViewFactory factory = ActivityViewFactoryRegistry.getInstance()
				.getViewFactoryForBeanType(a);
		assertTrue("Factory should be BiomartActivityViewFactory",
				factory instanceof BiomartActivityViewFactory);
		ActivityView<?> view = factory.getViewType(a);
		assertTrue("The view should be BiomartActivityContextualView",
				view instanceof BiomartActivityContextualView);
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

}
