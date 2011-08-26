package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RemoveProcessorInputPortEditTest {

	EditsImpl edits = new EditsImpl();
	private Processor processor;
	private ProcessorInputPort inputPort;
	private RemoveProcessorInputPortEdit removeProcessorInputPortEdit;
	
	@Before
	public void setup() throws Exception {
		processor = edits.createProcessor("test");
		inputPort = edits.createProcessorInputPort(processor, "port", 1);
		edits.getAddProcessorInputPortEdit(processor, inputPort).doEdit();
		removeProcessorInputPortEdit = new RemoveProcessorInputPortEdit(processor,inputPort);
	}
	
	@Test
	public void testDoEdit() throws Exception {
		assertFalse(removeProcessorInputPortEdit.isApplied());
		Processor p = removeProcessorInputPortEdit.doEdit();
		assertTrue(removeProcessorInputPortEdit.isApplied());
		assertSame(p,processor);
		assertEquals(0,processor.getInputPorts().size());
	}
	
	@Test
	public void testUndo() throws Exception {
		assertFalse(removeProcessorInputPortEdit.isApplied());
		removeProcessorInputPortEdit.doEdit();
		assertTrue(removeProcessorInputPortEdit.isApplied());
		removeProcessorInputPortEdit.undo();
		assertFalse(removeProcessorInputPortEdit.isApplied());
		assertEquals(1,processor.getInputPorts().size());
		assertSame(inputPort,processor.getInputPorts().get(0));
	}
	
	@Test
	public void testSubject() throws Exception {
		assertSame(processor,removeProcessorInputPortEdit.getSubject());
		removeProcessorInputPortEdit.doEdit();
		assertSame(processor,removeProcessorInputPortEdit.getSubject());
		removeProcessorInputPortEdit.undo();
		assertSame(processor,removeProcessorInputPortEdit.getSubject());
	}
}
