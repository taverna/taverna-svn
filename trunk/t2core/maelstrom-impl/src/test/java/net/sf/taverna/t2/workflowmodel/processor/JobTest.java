package net.sf.taverna.t2.workflowmodel.processor;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;
import static net.sf.taverna.t2.workflowmodel.processor.iteration.impl.CrossProductTest.nextID;

import junit.framework.TestCase;

public class JobTest extends TestCase {

	public void testPopPush() {
		Map<String,EntityIdentifier> dataMap = new HashMap<String,EntityIdentifier>();
		dataMap.put("Key1",nextID());
		dataMap.put("Key2",nextID());
		Job j = new Job("Process1",new int[]{1,0}, dataMap);
		// Check that push / pop returns equal to the original Job
		assertTrue(j.toString().equals(j.pushIndex().popIndex().toString()));
		// Index array of pushed job is always zero
		assertTrue(j.pushIndex().getIndex().length == 0);
	}
	
}
