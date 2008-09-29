package net.sf.taverna.t2.workflowmodel.processor;

import static net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext.nextReference;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

public class JobTest extends TestCase {

	InvocationContext context = new DummyInvocationContext();
	
	public void testPopPush() {
		Map<String,T2Reference> dataMap = new HashMap<String,T2Reference>();
		dataMap.put("Key1",nextReference());
		dataMap.put("Key2",nextReference());
		Job j = new Job("Process1",new int[]{1,0}, dataMap, context);
		// Check that push / pop returns equal to the original Job
		assertTrue(j.toString().equals(j.pushIndex().popIndex().toString()));
		// Index array of pushed job is always zero
		assertTrue(j.pushIndex().getIndex().length == 0);
	}
	
}
