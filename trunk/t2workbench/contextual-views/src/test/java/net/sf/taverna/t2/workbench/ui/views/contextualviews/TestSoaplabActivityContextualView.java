package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.SoaplabActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Before;
import org.junit.Test;

public class TestSoaplabActivityContextualView {

	Activity<?> a;
		
	@Before
	public void setup() throws Exception {
		a=new SoaplabActivity();
		SoaplabActivityConfigurationBean sb = new SoaplabActivityConfigurationBean();
		sb.setEndpoint("http://www.ebi.ac.uk/soaplab/services/edit.seqret");
		((SoaplabActivity)a).configure(sb);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		ActivityViewFactory factory = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(a);
		assertTrue("Factory should be SoaplabActivityViewFactory",factory instanceof SoaplabActivityViewFactory);
		ActivityView<?> view = factory.getViewType(a);
		assertTrue("The view should be SoaplabActivityContextualView",view instanceof SoaplabActivityContextualView);
	}
	
	@Test
	public void testConfigureAction() throws Exception {
		ContextualView view = new SoaplabActivityContextualView(a);
		assertNotNull("the action should not be null",view.getConfigureAction());
		assertTrue("The action should be a SoaplabAcitivyConfigurationAction",view.getConfigureAction() instanceof SoaplabActivityConfigurationAction);
	}
}
