package net.sf.taverna.t2.workflowmodel.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DefaultDispatchStackEditTest {
	
	private ProcessorImpl processor;
	private DefaultDispatchStackEdit defaultDispatchStackEdit;

	@Before
	public void setup() {
		processor = new ProcessorImpl();
		defaultDispatchStackEdit = new DefaultDispatchStackEdit(processor);
	}
	@Test
	public void testEdit() throws Exception {
		assertEquals(0,processor.getDispatchStack().getLayers().size());
		defaultDispatchStackEdit.doEdit();
		assertTrue(processor.getDispatchStack().getLayers().size()>0);
	}
	
	@Test
	public void testUndo() throws Exception {
		defaultDispatchStackEdit.doEdit();
		assertTrue(processor.getDispatchStack().getLayers().size()>0);
		defaultDispatchStackEdit.undo();
		assertEquals(0,processor.getDispatchStack().getLayers().size());
	}
	
	@Test
	public void testSubject() throws Exception {
		assertSame(processor,defaultDispatchStackEdit.getSubject());
		defaultDispatchStackEdit.doEdit();
		assertSame(processor,defaultDispatchStackEdit.getSubject());
		defaultDispatchStackEdit.undo();
		assertSame(processor,defaultDispatchStackEdit.getSubject());
		defaultDispatchStackEdit.doEdit();
		assertSame(processor,defaultDispatchStackEdit.getSubject());
	}
	
	@Test
	public void testApplied() throws Exception {
		assertFalse(defaultDispatchStackEdit.isApplied());
		defaultDispatchStackEdit.doEdit();
		assertTrue(defaultDispatchStackEdit.isApplied());
		defaultDispatchStackEdit.undo();
		assertFalse(defaultDispatchStackEdit.isApplied());
		defaultDispatchStackEdit.doEdit();
		assertTrue(defaultDispatchStackEdit.isApplied());
	}
	
}
