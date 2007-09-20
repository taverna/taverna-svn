package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.NamingException;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.junit.Before;
import org.junit.Test;

public class AddProcessorEditTest {

	private Processor processor;
	
	@Before
	public void createProcessor() {
		processor = new EditsImpl().createProcessor("the_processor");
	}
	
	@Test
	public void testAddingOfProcessor() throws Exception {
		Dataflow f = new DataflowImpl();
		
		AddProcessorEdit edit = new AddProcessorEdit(f,processor);
		edit.doEdit();
		
		assertEquals(1,f.getProcessors().size());
		assertEquals(processor,f.getProcessors().get(0));
	}
	
	@Test(expected=EditException.class)
	public void testCantEditTwice() throws Exception {
		Dataflow f = new DataflowImpl();
		AddProcessorEdit edit = new AddProcessorEdit(f,processor);
		edit.doEdit();
		edit.doEdit();
	}
	
	@Test
	public void testUndo() throws Exception {
		Dataflow f = new DataflowImpl();
		AddProcessorEdit edit = new AddProcessorEdit(f,processor);
		edit.doEdit();
		edit.undo();
		
		assertEquals(0,f.getProcessors().size());
	}
	
	@Test
	public void testUndo2() throws Exception {
		//checks the right one is removed
		Dataflow f = new DataflowImpl();
		AddProcessorEdit edit = new AddProcessorEdit(f,processor);
		edit.doEdit();
		
		ProcessorImpl badProcessor = new ProcessorImpl();
		badProcessor.setName("bad_processor");
		AddProcessorEdit edit2 = new AddProcessorEdit(f,badProcessor);
		edit2.doEdit();
		
		assertEquals(2,f.getProcessors().size());
		edit2.undo();
		assertEquals(processor,f.getProcessors().get(0));
	}
	
	@Test(expected=NamingException.class)
	public void testDuplicateName() throws Exception {
		Dataflow f = new DataflowImpl();
		AddProcessorEdit edit = new AddProcessorEdit(f,processor);
		edit.doEdit();
		
		ProcessorImpl processor2=new ProcessorImpl();
		processor2.setName(processor.getLocalName());
		AddProcessorEdit edit2 = new AddProcessorEdit(f,processor2);
		edit2.doEdit();
	}
	
	@Test
	public void testThroughEditsImpl() throws Exception {
		//Essentially the same as testAddingOfProcessor, but a sanity test that it works correctly through the Edits API
		Dataflow f = new DataflowImpl();
		new EditsImpl().getAddProcessorEdit(f, processor).doEdit();
		
		assertEquals(1,f.getProcessors().size());
		assertEquals(processor,f.getProcessors().get(0));
	}
	
	@Test
	public void testRedo() throws Exception {
		Dataflow f = new DataflowImpl();
		AddProcessorEdit edit = new AddProcessorEdit(f,processor);
		edit.doEdit();
		
		assertEquals(1,f.getProcessors().size());
		assertEquals(processor,f.getProcessors().get(0));
		
		edit.undo();
		
		assertEquals(0,f.getProcessors().size());
		
		edit.doEdit();
		
		assertEquals(1,f.getProcessors().size());
		assertEquals(processor,f.getProcessors().get(0));
	}
}
