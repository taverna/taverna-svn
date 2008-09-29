package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyContextualView;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;

import org.junit.Test;

public class TestContextualViewFactory {

	@SuppressWarnings("unchecked")
	@Test
	public void getIterationStratContextualView() throws Exception {

		Edits edits = EditsRegistry.getEdits();
		Processor proc = edits.createProcessor("processor");
		ProcessorInputPort in1 = edits.createProcessorInputPort(proc, "in1", 1);
		ProcessorInputPort in2 = edits.createProcessorInputPort(proc, "in2", 1);

		ContextualViewFactory viewFactory = ContextualViewFactoryRegistry
				.getInstance().getViewFactoryForObject(in1);
		assertNotNull("The beanshsell view factory should not be null",
				viewFactory);
		assertTrue("Was not an iteration strategy contextual view factory",
				viewFactory instanceof IterationStrategyContextualViewFactory);
		ContextualView viewType = viewFactory.getView(in1);
		assertTrue("Was not an iteration strategy contextual view", viewType
				.getClass().equals(IterationStrategyContextualView.class));

	}

}
