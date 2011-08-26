/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.testing.CaptureResultsListener;
import net.sf.taverna.t2.testing.InvocationTestHelper;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

import org.junit.Ignore;
import org.junit.Test;

public class RunTaverna2Test extends InvocationTestHelper {

	@Test
	public void textPassedThrough() throws Exception {
		Dataflow dataflow = loadDataflow("in-out.t2flow");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		T2Reference entityId=context.getReferenceService().register("fred", 0, true, context);
		
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			WorkflowDataToken inputToken = new WorkflowDataToken("",new int[]{}, entityId, context);
			facade.pushData(inputToken, port.getName());
		}
		
		waitForCompletion(listener);
		
		assertEquals("fred", listener.getResult("output"));
	}
	
	@Test
	public void testSmallBytesPassThrough() throws Exception {
		Dataflow dataflow = loadDataflow("in-out.t2flow");
		DataflowValidationReport report = validateDataflow(dataflow);
		byte [] bytes = new byte[]{'a','b','c'};
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		T2Reference entityId=context.getReferenceService().register(bytes, 0, true, context);
		
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			WorkflowDataToken inputToken = new WorkflowDataToken("",new int[]{}, entityId, context);
			facade.pushData(inputToken, port.getName());
		}
		
		waitForCompletion(listener);
		
		assertEquals(bytes, listener.getResult("output"));
	}
	
	@Test
	public void test1kBytesPassThrough() throws Exception {
		Dataflow dataflow = loadDataflow("in-out.t2flow");
		DataflowValidationReport report = validateDataflow(dataflow);
		byte [] bytes = readBinaryData("1k.bin");
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		T2Reference entityId=context.getReferenceService().register(bytes, 0, true, context);
		
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			WorkflowDataToken inputToken = new WorkflowDataToken("",new int[]{}, entityId, context);
			facade.pushData(inputToken, port.getName());
		}
		
		waitForCompletion(listener);
		
		assertEquals(bytes, listener.getResult("output"));
	}
	
	@Test
	public void test1mBytesPassThrough() throws Exception {
		Dataflow dataflow = loadDataflow("in-out.t2flow");
		DataflowValidationReport report = validateDataflow(dataflow);
		byte [] bytes = readBinaryData("1m.bin");
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
		
		T2Reference entityId=context.getReferenceService().register(bytes, 0, true, context);
		
		for (DataflowInputPort port : dataflow.getInputPorts()) {
			WorkflowDataToken inputToken = new WorkflowDataToken("",new int[]{}, entityId, context);
			facade.pushData(inputToken, port.getName());
		}
		
		waitForCompletion(listener);
		
		assertEquals(bytes, listener.getResult("output"));
	}

	@Test
	@Ignore("Doesn't finish due to missing completion event")
	public void test2Merges() throws Exception {
		//tests a dataflow that has 2 merges in it, and includes the same named output ports connected to an 
		//input port (all the source ports are called 'value')
		Dataflow dataflow = loadDataflow("2-merges.t2flow");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
				
		waitForCompletion(listener,5);
	}
	
	@Test
	public void testListOut() throws Exception {
		//a very basic test that generates a list and then sounds it to an output.
		//tests whether the correct completion event is received on the output
		Dataflow dataflow = loadDataflow("list-out.t2flow");
		DataflowValidationReport report = validateDataflow(dataflow);
		assertTrue("Unsatisfied processor found during validation",report.getUnsatisfiedEntities().size() == 0);
		assertTrue("Failed processors found during validation",report.getFailedEntities().size() == 0);
		assertTrue("Unresolved outputs found during validation",report.getUnresolvedOutputs().size() == 0);
		assertTrue("Validation failed",report.isValid());
		
		WorkflowInstanceFacade facade;
		facade = new WorkflowInstanceFacadeImpl(dataflow,context,"");
		CaptureResultsListener listener = new CaptureResultsListener(dataflow,context);
		facade.addResultListener(listener);
		
		facade.fire();
				
		waitForCompletion(listener,5);
		Object o = listener.getResult("out");
		assertNotNull("There should be an output 'out'",o);
		assertTrue("the output should be a list",o instanceof List);
		List<?>ol = (List<?>)o;
		assertEquals("There should be 3 items in the list",3, ol.size());
	}
	
	
	
	private byte[] readBinaryData(String resourceName) throws Exception {
		InputStream instr = RunTaverna2Test.class.getResourceAsStream("/binary/"+resourceName);
		int size=instr.available();
		byte[]result = new byte[size];
		instr.read(result);
		return result;
	}
	
	
	
}
