package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.junit.Before;
import org.junit.Test;

public class IterationStrategyTest extends TranslatorTestHelper {

	private ScuflModel iterateImplicit;
	private ScuflModel iterateDot;
	private ScuflModel iterateCross;

	@Before
	public void loadModels() throws ScuflException, IOException {
		iterateImplicit = loadScufl("tripple-iterate-implicit.xml");
		iterateDot = loadScufl("tripple-iterate-dot.xml");
		iterateCross = loadScufl("tripple-iterate-cross.xml");
		assertNotNull(iterateImplicit);
		assertNotNull(iterateDot);
		assertNotNull(iterateCross);
	}

	@Test
	public void implicitStrategy() throws Exception {
		Dataflow translated = WorkflowModelTranslator
				.doTranslation(iterateImplicit);
		Processor proc = null;
		for (Processor candidate : translated.getProcessors()) {
			if (candidate.getLocalName().equals("iterate")) {
				proc = candidate;
				break;
			}
		}
		assertNotNull("Can't find processor", proc);

		IterationStrategy itStrat = proc.getIterationStrategy().getStrategies()
				.get(0);
		assertEquals("Iteration should have 1 child", 1, itStrat
				.getTerminalNode().getChildCount());
		IterationStrategyNode first = itStrat.getTerminalNode().getChildAt(0);
		assertTrue("Implicit strategy should be cross product ",
				first instanceof CrossProduct);
		assertEquals("Should have 3 children", 3, first.getChildCount());

		NamedInputPortNode node0 = (NamedInputPortNode) first.getChildAt(0);
		NamedInputPortNode node1 = (NamedInputPortNode) first.getChildAt(1);
		NamedInputPortNode node2 = (NamedInputPortNode) first.getChildAt(2);

		// Should be reverse order
		assertEquals("in2", node0.getPortName());
		assertEquals("in1", node1.getPortName());
		assertEquals("in0", node2.getPortName());
	}

	@Test
	public void crossProduct() throws Exception {
		Dataflow translated = WorkflowModelTranslator
				.doTranslation(iterateCross);
		Processor proc = null;
		for (Processor candidate : translated.getProcessors()) {
			if (candidate.getLocalName().equals("iterate")) {
				proc = candidate;
				break;
			}
		}
		assertNotNull("Can't find processor", proc);

		IterationStrategy itStrat = proc.getIterationStrategy().getStrategies()
				.get(0);
		assertEquals("Iteration should have 1 child", 1, itStrat
				.getTerminalNode().getChildCount());
		IterationStrategyNode first = itStrat.getTerminalNode().getChildAt(0);
		assertTrue("Implicit strategy should be cross product ",
				first instanceof CrossProduct);
		assertEquals("Should have 3 children", 3, first.getChildCount());

		NamedInputPortNode node0 = (NamedInputPortNode) first.getChildAt(0);
		NamedInputPortNode node1 = (NamedInputPortNode) first.getChildAt(1);
		NamedInputPortNode node2 = (NamedInputPortNode) first.getChildAt(2);

		// Should be reverse order
		assertEquals("in2", node0.getPortName());
		assertEquals("in1", node1.getPortName());
		assertEquals("in0", node2.getPortName());
	}

	@Test
	public void dotProduct() throws Exception {
		Dataflow translated = WorkflowModelTranslator.doTranslation(iterateDot);
		Processor proc = null;
		for (Processor candidate : translated.getProcessors()) {
			if (candidate.getLocalName().equals("iterate")) {
				proc = candidate;
				break;
			}
		}
		assertNotNull("Can't find processor", proc);

		IterationStrategy itStrat = proc.getIterationStrategy().getStrategies()
				.get(0);
		assertEquals("Iteration should have 1 child", 1, itStrat
				.getTerminalNode().getChildCount());
		IterationStrategyNode first = itStrat.getTerminalNode().getChildAt(0);
		assertTrue("Implicit strategy should be dot product ",
				first instanceof DotProduct);
		assertEquals("Should have 3 children", 3, first.getChildCount());

		NamedInputPortNode node0 = (NamedInputPortNode) first.getChildAt(0);
		NamedInputPortNode node1 = (NamedInputPortNode) first.getChildAt(1);
		NamedInputPortNode node2 = (NamedInputPortNode) first.getChildAt(2);

		// Should be reverse order
		assertEquals("in2", node0.getPortName());
		assertEquals("in1", node1.getPortName());
		assertEquals("in0", node2.getPortName());
	}

}
