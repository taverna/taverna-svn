package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyContextualView;
import net.sf.taverna.t2.workbench.iterationstrategy.contextview.IterationStrategyContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.processor.AsynchEchoActivity;
import net.sf.taverna.t2.workflowmodel.processor.EchoConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Test;

public class TestContextualViewFactory {
	FileManager fileManager = FileManager.getInstance();
	EditManager editManager = EditManager.getInstance();
	private Dataflow dataflow;
	private Processor proc1;
	private ActivityInputPort activityInputPort;

	@SuppressWarnings("unchecked")
	@Test
	public void getIterationStratContextualView() throws Exception {
		ContextualViewFactory viewFactory = ContextualViewFactoryRegistry
				.getInstance().getViewFactoryForObject(activityInputPort);
		assertNotNull("The beanshsell view factory should not be null",
				viewFactory);
		assertTrue("Was not an iteration strategy contextual view factory",
				viewFactory instanceof IterationStrategyContextualViewFactory);
		ContextualView view = viewFactory.getView(activityInputPort);
		assertTrue("Was not an iteration strategy contextual view", view
				.getClass().equals(IterationStrategyContextualView.class));

		IterationStrategyContextualView itView = (IterationStrategyContextualView) view;
		assertEquals(proc1, itView.getProcessor());
	}

	@Before
	public void makeDataflow() throws EditException {
		List<Edit<?>> compoundEdit = new ArrayList<Edit<?>>();

		dataflow = fileManager.newDataflow();
		Edits edits = EditsRegistry.getEdits();
		proc1 = edits.createProcessor("processor1");
		compoundEdit.add(edits.getAddProcessorEdit(dataflow, proc1));

		Processor proc2 = edits.createProcessor("processor2");
		compoundEdit.add(edits.getAddProcessorEdit(dataflow, proc2));

		ProcessorInputPort in1 = edits.createProcessorInputPort(proc1, "in1", 0);
		compoundEdit.add(edits.getAddProcessorInputPortEdit(proc1, in1));

		ProcessorInputPort in2 = edits
				.createProcessorInputPort(proc1, "in2", 0);
		compoundEdit.add(edits.getAddProcessorInputPortEdit(proc1, in2));

		Activity<EchoConfig> activity = new AsynchEchoActivity();
		compoundEdit.add(edits.getConfigureActivityEdit(activity,
				new EchoConfig()));

		compoundEdit.add(edits.getAddActivityEdit(proc1, activity));
		compoundEdit.add(edits.getDefaultDispatchStackEdit(proc1));

		editManager.doDataflowEdit(dataflow, new CompoundEdit(compoundEdit));
		activityInputPort = activity.getInputPorts().iterator().next();
	}
}
