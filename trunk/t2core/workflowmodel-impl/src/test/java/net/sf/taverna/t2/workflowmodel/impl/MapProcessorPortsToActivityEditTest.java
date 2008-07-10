package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.serialization.DummyActivity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MapProcessorPortsToActivityEditTest {

	Processor p;
	EditsImpl edits = new EditsImpl();
	MapProcessorPortForActivityEdit edit;
	
	@Before
	public void setupProcessorAndEdit() throws Exception {
		p=new ProcessorImpl();
		ProcessorInputPort ip1=edits.createProcessorInputPort(p, "inputPort", 1);
		ProcessorOutputPort op1 = edits.createProcessorOutputPort(p, "outputPort", 1, 1);
		edits.getAddProcessorOutputPortEdit(p, op1).doEdit();
		edits.getAddProcessorInputPortEdit(p, ip1).doEdit();
		
		Activity<?> a = new DummyActivity();
		ActivityInputPort aip1 = edits.createActivityInputPort("inputPort", 1, true, new ArrayList<Class<? extends ExternalReferenceSPI>>(), String.class);
		ActivityInputPort aip2 = edits.createActivityInputPort("newInptPort", 0, true, new ArrayList<Class<? extends ExternalReferenceSPI>>(), String.class);
		edits.getAddActivityInputPortEdit(a, aip1).doEdit();
		edits.getAddActivityInputPortEdit(a, aip2).doEdit();
		
		OutputPort aop1 = edits.createActivityOutputPort("outputPort", 1, 1);
		OutputPort aop2 = edits.createActivityOutputPort("newOutputPort", 0, 0);
		edits.getAddActivityOutputPortEdit(a, aop1).doEdit();
		edits.getAddActivityOutputPortEdit(a, aop2).doEdit();
		
		edits.getAddActivityEdit(p, a).doEdit();
		
		edit = new MapProcessorPortForActivityEdit(p);
	}
	
	
	@Test
	public void testIsApplied() throws Exception {
		assertFalse(edit.isApplied());
		edit.doEdit();
		assertTrue(edit.isApplied());
		edit.undo();
		assertFalse(edit.isApplied());
	}
	
	@Test
	@Ignore
	public void testDoEdit() throws Exception {
		edit.doEdit();
		assertEquals("there should now be 2 input ports",2,p.getInputPorts().size());
		assertEquals("there should now be 2 output ports",2,p.getOutputPorts().size());
	}
	
	@Test
	@Ignore
	public void testUndo() throws Exception {
		edit.doEdit();
		edit.undo();
		assertEquals("there should now be 1 input ports",1,p.getInputPorts().size());
		assertEquals("there should now be 1 output ports",1,p.getOutputPorts().size());
	}
}
