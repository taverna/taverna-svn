package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;

import org.junit.Before;
import org.junit.Test;

public class RemoveProcessorOutputPortEditTest {

	EditsImpl edits = new EditsImpl();
	private Processor processor;
	private ProcessorOutputPort outputPort;
	private RemoveProcessorOutputPortEdit removeProcessorOutputPortEdit;
	
	@Before
	public void setup() throws Exception {
		processor = edits.createProcessor("test");
		outputPort = edits.createProcessorOutputPort(processor, "port", 1,1);
		edits.getAddProcessorOutputPortEdit(processor, outputPort).doEdit();
		removeProcessorOutputPortEdit = new RemoveProcessorOutputPortEdit(processor,outputPort);
	}
	
	@Test
	public void testDoEdit() throws Exception {
		assertFalse(removeProcessorOutputPortEdit.isApplied());
		Processor p = removeProcessorOutputPortEdit.doEdit();
		assertTrue(removeProcessorOutputPortEdit.isApplied());
		assertSame(p,processor);
		assertEquals(0,processor.getOutputPorts().size());
	}
	
	@Test
	public void testUndo() throws Exception {
		assertFalse(removeProcessorOutputPortEdit.isApplied());
		Processor p = removeProcessorOutputPortEdit.doEdit();
		assertTrue(removeProcessorOutputPortEdit.isApplied());
		removeProcessorOutputPortEdit.undo();
		assertFalse(removeProcessorOutputPortEdit.isApplied());
		assertEquals(1,processor.getOutputPorts().size());
		assertSame(outputPort,processor.getOutputPorts().get(0));
	}
	
	@Test
	public void testSubject() throws Exception {
		assertSame(processor,removeProcessorOutputPortEdit.getSubject());
		removeProcessorOutputPortEdit.doEdit();
		assertSame(processor,removeProcessorOutputPortEdit.getSubject());
		removeProcessorOutputPortEdit.undo();
		assertSame(processor,removeProcessorOutputPortEdit.getSubject());
	}
}
