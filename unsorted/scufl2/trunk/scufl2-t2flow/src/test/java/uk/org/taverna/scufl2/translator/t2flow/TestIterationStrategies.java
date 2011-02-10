package uk.org.taverna.scufl2.translator.t2flow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static uk.org.taverna.scufl2.translator.t2flow.T2FlowReader.APPLICATION_VND_TAVERNA_T2FLOW_XML;

import org.junit.Before;
import org.junit.Test;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.api.iterationstrategy.CrossProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.DotProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;

public class TestIterationStrategies {
	
	private static final String ITERATIONSTRATEGIES_T2FLOW = "/iterationstrategies.t2flow";
	private WorkflowBundle wfBundle;
	private Workflow wf;
	private Processor coloursLisr;
	private Processor concat;
	private Processor shape;

	@Before
	public void readWorkflow() throws ReaderException, IOException {
		WorkflowBundleIO io = new WorkflowBundleIO();
		InputStream is = getClass().getResourceAsStream(ITERATIONSTRATEGIES_T2FLOW);
		wfBundle = io.readBundle(is, APPLICATION_VND_TAVERNA_T2FLOW_XML);
		wf = wfBundle.getMainWorkflow();
		coloursLisr = wf.getProcessors().getByName("ColoursLisr");
		concat = wf.getProcessors().getByName("Concatenate_two_strings");
		shape = wf.getProcessors().getByName("ShapeAnimals");
	}

	@Test
	public void simpleCrossProduct() throws Exception {
		assertEquals(1, coloursLisr.getIterationStrategyStack().size());
		IterationStrategyTopNode top = coloursLisr.getIterationStrategyStack().get(0);
		assertTrue(top instanceof CrossProduct);
		assertEquals(1, top.size());
		IterationStrategyNode node = top.get(0);
		assertTrue(node instanceof PortNode);
		PortNode portNode = (PortNode) node;
		assertEquals(0, portNode.getDesiredDepth().intValue());
		assertEquals(coloursLisr.getInputPorts().getByName("string"), portNode.getInputProcessorPort());
		
	}

	@Test
	public void simpleDot() throws Exception {
		assertEquals(1, concat.getIterationStrategyStack().size());
		IterationStrategyTopNode top = concat.getIterationStrategyStack().get(0);
		assertTrue(top instanceof DotProduct);
		assertEquals(2, top.size());
		IterationStrategyNode node1 = top.get(0);
		assertTrue(node1 instanceof PortNode);
		PortNode portNode1 = (PortNode) node1;
		assertEquals(0, portNode1.getDesiredDepth().intValue());
		assertEquals(concat.getInputPorts().getByName("string1"), portNode1.getInputProcessorPort());

		IterationStrategyNode node2 = top.get(1);
		assertTrue(node2 instanceof PortNode);
		PortNode portNode2 = (PortNode) node2;
		assertEquals(0, portNode2.getDesiredDepth().intValue());
		assertEquals(concat.getInputPorts().getByName("string2"), portNode2.getInputProcessorPort());

		assertEquals(concat.getInputPorts().getByName("string2"), portNode2.getInputProcessorPort());
		
	}


	@Test
	public void crossAndDot() throws Exception {
		assertEquals(1, shape.getIterationStrategyStack().size());
		IterationStrategyTopNode top = shape.getIterationStrategyStack().get(0);
		assertTrue(top instanceof CrossProduct);
		assertEquals(2, top.size());
		IterationStrategyNode node1 = top.get(0);
		assertTrue(node1 instanceof PortNode);
		PortNode portNode1 = (PortNode) node1;
		assertEquals(0, portNode1.getDesiredDepth().intValue());
		assertEquals(shape.getInputPorts().getByName("string1"), portNode1.getInputProcessorPort());

		IterationStrategyNode node2 = top.get(1);
		assertTrue(node2 instanceof DotProduct);
		DotProduct portNode2 = (DotProduct) node2;
		
		// Note: string3 before string2
		
		IterationStrategyNode node21 = portNode2.get(0);
		assertTrue(node21 instanceof PortNode);
		PortNode portNode21 = (PortNode) node21;
		assertEquals(0, portNode21.getDesiredDepth().intValue());
		assertEquals(shape.getInputPorts().getByName("string3"), portNode21.getInputProcessorPort());

		IterationStrategyNode node22 = portNode2.get(1);
		assertTrue(node22 instanceof PortNode);
		PortNode portNode22 = (PortNode) node22;
		assertEquals(0, portNode22.getDesiredDepth().intValue());
		assertEquals(shape.getInputPorts().getByName("string2"), portNode22.getInputProcessorPort());

	}

}
