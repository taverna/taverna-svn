package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.*;

import java.util.ArrayList;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.serialization.DummyActivity;

import org.junit.Before;
import org.junit.Test;

public class MapProcessorPortsToActivityEditTest {

	ProcessorImpl p;
	EditsImpl edits = new EditsImpl();
	MapProcessorPortsForActivityEdit edit;
	
	@Before
	public void setupProcessorAndEdit() throws Exception {
		p=new ProcessorImpl();
		ProcessorInputPort ip1=edits.createProcessorInputPort(p, "inputPort", 1);
		ProcessorOutputPort op1 = edits.createProcessorOutputPort(p, "outputPort", 1, 1);
		edits.getAddProcessorOutputPortEdit(p, op1).doEdit();
		edits.getAddProcessorInputPortEdit(p, ip1).doEdit();
		
		Activity<?> a = new DummyActivity();
		ActivityInputPort aip1 = edits.createActivityInputPort("inputPort", 1, true, new ArrayList<Class<? extends ExternalReferenceSPI>>(), String.class);
		ActivityInputPort aip2 = edits.createActivityInputPort("newInputPort", 0, true, new ArrayList<Class<? extends ExternalReferenceSPI>>(), String.class);
		edits.getAddActivityInputPortEdit(a, aip1).doEdit();
		edits.getAddActivityInputPortEdit(a, aip2).doEdit();
		
		OutputPort aop1 = edits.createActivityOutputPort("outputPort", 1, 1);
		OutputPort aop2 = edits.createActivityOutputPort("newOutputPort", 0, 0);
		edits.getAddActivityOutputPortEdit(a, aop1).doEdit();
		edits.getAddActivityOutputPortEdit(a, aop2).doEdit();
		
		edits.getAddActivityEdit(p, a).doEdit();
		
		new AddActivityInputPortMapping(a,"inputPort","inputPort").doEdit();
		new AddActivityOutputPortMapping(a,"outputPort","outputPort").doEdit();
		
		edit = new MapProcessorPortsForActivityEdit(p);
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
	public void testDoEdit() throws Exception {
		edit.doEdit();
		assertEquals("there should now be 2 input ports",2,p.getInputPorts().size());
		assertEquals("there should now be 2 output ports",2,p.getOutputPorts().size());
	}
	
	@Test
	public void testUndo() throws Exception {
		edit.doEdit();
		edit.undo();
		assertEquals("there should now be 1 input ports",1,p.getInputPorts().size());
		assertEquals("there should now be 1 output ports",1,p.getOutputPorts().size());
	}
	
	@Test
	public void testMapping() throws Exception {
		Activity<?>a = p.getActivityList().get(0);
		
		assertEquals(1,a.getInputPortMapping().size());
		assertEquals("inputPort",a.getInputPortMapping().get("inputPort"));
		assertEquals(1,a.getOutputPortMapping().size());
		assertEquals("outputPort",a.getOutputPortMapping().get("outputPort"));
		
		edit.doEdit();
		
		assertEquals(2,a.getInputPortMapping().size());
		
		assertEquals("inputPort",a.getInputPortMapping().get("inputPort"));
		assertEquals("newInputPort",a.getInputPortMapping().get("newInputPort"));
		
		assertEquals(2,a.getOutputPortMapping().size());
		assertEquals("outputPort",a.getOutputPortMapping().get("outputPort"));
		assertEquals("newOutputPort",a.getOutputPortMapping().get("newOutputPort"));
		
		edit.undo();
		
		assertEquals(1,a.getInputPortMapping().size());
		assertEquals("inputPort",a.getInputPortMapping().get("inputPort"));
		assertEquals(1,a.getOutputPortMapping().size());
		assertEquals("outputPort",a.getOutputPortMapping().get("outputPort"));
	}
	
	@Test 
	public void testUnchangedPortsRemain() throws Exception {
		ProcessorOutputPort op1 = p.getOutputPortWithName("outputPort");
		ProcessorInputPort ip1 = p.getInputPortWithName("inputPort");
		edit.doEdit();
		assertSame(ip1,p.getInputPortWithName("inputPort"));
		assertSame(op1,p.getOutputPortWithName("outputPort"));
	}
}
