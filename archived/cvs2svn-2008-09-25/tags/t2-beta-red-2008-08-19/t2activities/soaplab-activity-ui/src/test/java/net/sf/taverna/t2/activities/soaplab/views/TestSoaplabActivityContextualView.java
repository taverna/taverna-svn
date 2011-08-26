package net.sf.taverna.t2.activities.soaplab.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivityConfigurationBean;
import net.sf.taverna.t2.activities.soaplab.actions.SoaplabActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Before;
import org.junit.Ignore;
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
	@Ignore("Integration test")
	public void testDisovery() throws Exception {
		ContextualViewFactory factory = ContextualViewFactoryRegistry.getInstance().getViewFactoryForObject(a);
		assertTrue("Factory should be SoaplabActivityViewFactory",factory instanceof SoaplabActivityViewFactory);
		ContextualView view = factory.getView(a);
		assertTrue("The view should be SoaplabActivityContextualView",view instanceof SoaplabActivityContextualView);
	}
	
	@Test
	@Ignore("Integration test")
	public void testConfigureAction() throws Exception {
		ContextualView view = new SoaplabActivityContextualView(a);
		assertNotNull("the action should not be null",view.getConfigureAction(null));
		assertTrue("The action should be a SoaplabAcitivyConfigurationAction",view.getConfigureAction(null) instanceof SoaplabActivityConfigurationAction);
	}
	
	private void run() throws Exception
	{
		setup();
		ContextualView view = new SoaplabActivityContextualView(a);
		view.setVisible(true);
	}
	
	public static void main(String[] args) throws Exception {
		new TestSoaplabActivityContextualView().run();
	}
}
