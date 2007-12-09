package net.sf.taverna.t2.facade.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicLong;

import net.sf.taverna.t2.facade.FailureListener;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.impl.DummyDataflow;
import net.sf.taverna.t2.workflowmodel.impl.DummyDataflowInputPort;
import net.sf.taverna.t2.workflowmodel.impl.DummyDataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.DummyProcessor;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WorkflowInstanceFacadeImplTest {
	
	private DummyDataflow dataflow;
	private WorkflowInstanceFacadeImpl facade;
	private InvocationContext context = new TestInvocationContext();
	
	@Before
	public void createDataflow() {
		dataflow=new DummyDataflow();
		WorkflowInstanceFacadeImpl.owningProcessId = new AtomicLong(0);
		facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
	}

	@Test
	public void testWorkflowInstanceFacadeImpl() {
		assertSame(dataflow, facade.getDataflow());
	}

	@Test
	public void testAddFailureListener() {
		FailureListener listener = new FailureListener() {
			public void workflowFailed(String message, Throwable t) {
		
			}
		};
		assertEquals(0,facade.failureListeners.size());
		facade.addFailureListener(listener);
		assertEquals(1,facade.failureListeners.size());
		assertSame(listener,facade.failureListeners.get(0));
		facade.addFailureListener(listener);
		assertEquals(2,facade.failureListeners.size());
	}

	@Test
	public void testAddResultListener() {
		ResultListener listener = new ResultListener() {
			public void resultTokenProduced(WorkflowDataToken token, String portName) {	
			}
		};
		assertEquals(0,facade.resultListeners.size());
		facade.addResultListener(listener);
		assertEquals(1,facade.resultListeners.size());
		assertSame(listener,facade.resultListeners.get(0));
		facade.addResultListener(listener);
		assertEquals(2,facade.resultListeners.size());
	}

	@Test
	public void testFire() {
		DummyProcessor processor = new DummyProcessor();
		dataflow.processors.add(processor);
		facade.fire();
		assertNotNull(processor.firedOwningProcess);
		assertEquals("facade0:test_dataflow",processor.firedOwningProcess);
	}

	@Ignore("Not implemented")
	@Test
	public void testGetStateModel() {
		fail("Not yet implemented");
	}

	@Test
	public void testPushData() throws Exception {
		DummyDataflowInputPort port = new DummyDataflowInputPort("test",0,0,dataflow);
		dataflow.inputPorts.add(port);
		facade.pushData(new WorkflowDataToken("", new int[]{}, null, context), "test");
		
		assertNotNull(port.tokenOwningProcess);
		assertEquals("facade0",port.tokenOwningProcess);
	}
	
	@Test
	// test pushData doesn't fire port with non-matching name
	public void testPushData2() throws Exception {
		DummyDataflowInputPort port1 = new DummyDataflowInputPort("test_port1",0,0,dataflow);
		dataflow.inputPorts.add(port1);
		DummyDataflowInputPort port2 = new DummyDataflowInputPort("test_port2",0,0,dataflow);
		dataflow.inputPorts.add(port2);
		facade.pushData(new WorkflowDataToken("", new int[]{}, null, context), "test_port1");
		
		assertNotNull(port1.tokenOwningProcess);
		assertEquals("facade0",port1.tokenOwningProcess);
		
		assertNull(port2.tokenOwningProcess);
	}
	

	
	//test fire doesn't fire input ports
	@Test
	public void testFire2() {
		DummyProcessor processor = new DummyProcessor();
		dataflow.processors.add(processor);
		DummyDataflowInputPort port1 = new DummyDataflowInputPort("test_port1",0,0,dataflow);
		dataflow.inputPorts.add(port1);
		
		facade.fire();
		assertNotNull(processor.firedOwningProcess);
		assertEquals("facade0:test_dataflow",processor.firedOwningProcess);
		assertNull(port1.tokenOwningProcess);
	}
	
	//test for calling fire after pushData has been called throws an Exception
	@Test(expected=IllegalStateException.class)
	public void testFireIllegalStateException() throws Exception{
		DummyProcessor processor = new DummyProcessor();
		dataflow.processors.add(processor);
		DummyDataflowInputPort port1 = new DummyDataflowInputPort("test_port1",0,0,dataflow);
		dataflow.inputPorts.add(port1);
		
		facade.pushData(new WorkflowDataToken("", new int[]{}, null, context), "test_port1");
		
		facade.fire();
	}

	@Test
	public void testFireAllProcessors() throws Exception{
		DummyDataflow dummyDataflow = new DummyDataflow();
		WorkflowInstanceFacadeImpl facade = new WorkflowInstanceFacadeImpl(dummyDataflow, context, "");
		DummyProcessor processor1 = new DummyProcessor();
		dummyDataflow.processors.add(processor1);
		
		DummyProcessor processor2 = new DummyProcessor();
		dummyDataflow.processors.add(processor2);
		
		DummyProcessor processor3 = new DummyProcessor();
		dummyDataflow.processors.add(processor3);
		
		facade.fire();
		
		assertNotNull(processor1.firedOwningProcess);
		assertNotNull(processor2.firedOwningProcess);
		assertNotNull(processor3.firedOwningProcess);
		
		assertEquals("facade1:test_dataflow",processor1.firedOwningProcess);
		assertEquals("facade1:test_dataflow",processor2.firedOwningProcess);
		assertEquals("facade1:test_dataflow",processor3.firedOwningProcess);
	}
	
	@Test
	public void testRemoveFailureListener() {
		
		FailureListener listener = new FailureListener() {
			public void workflowFailed(String message, Throwable t) {
				
			}
		};
		assertEquals(0,facade.failureListeners.size());
		facade.removeFailureListener(listener);
		assertEquals(0,facade.failureListeners.size());
		facade.addFailureListener(listener);
		
		FailureListener listener2 = new FailureListener() {
			public void workflowFailed(String message, Throwable t) {
				
			}
		};
		
		assertEquals(1,facade.failureListeners.size());
		facade.removeFailureListener(listener2);
		assertEquals(1,facade.failureListeners.size());
		facade.removeFailureListener(listener);
		assertEquals(0,facade.failureListeners.size());
	}

	@Test
	public void testRemoveResultListener() {
		WorkflowInstanceFacadeImpl facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
		ResultListener listener = new ResultListener() {
			public void resultTokenProduced(WorkflowDataToken token,String portName) {	
			}
		};
		assertEquals(0,facade.resultListeners.size());
		facade.removeResultListener(listener);
		assertEquals(0,facade.resultListeners.size());
		facade.addResultListener(listener);
		
		ResultListener listener2 = new ResultListener() {
			public void resultTokenProduced(WorkflowDataToken token,String portName) {	
			}
		};
		
		assertEquals(1,facade.resultListeners.size());
		facade.removeResultListener(listener2);
		assertEquals(1,facade.resultListeners.size());
		facade.removeResultListener(listener);
		assertEquals(0,facade.resultListeners.size());
	}
	
	
	@Test
	//tests that the listener is added and removed from the output ports
	public void testResultListenerAndOutputPorts() {
		WorkflowInstanceFacadeImpl facade = new WorkflowInstanceFacadeImpl(dataflow, context, "");
		ResultListener listener = new ResultListener() {
			public void resultTokenProduced(WorkflowDataToken token,String portName) {
			}
		};
		DummyDataflowOutputPort outPort = new DummyDataflowOutputPort("test_output",dataflow);
		dataflow.outputPorts.add(outPort);
		assertEquals("there should be no listeners on the output port yet",0,outPort.getResultListeners().size());
		facade.addResultListener(listener);
		assertEquals("there should be 1 listener on the output port",1,outPort.getResultListeners().size());
		facade.removeResultListener(listener);
		assertEquals("the listener should have been removed on the output port yet",0,outPort.getResultListeners().size());
		
	}

}
