package net.sf.taverna.t2.workflowmodel.processor;

import java.util.HashMap;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TreeCache;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import junit.framework.TestCase;

public class TreeCacheTest extends TestCase {

	InvocationContext context = new TestInvocationContext();
	
	public void testStoreLoad() {
		TreeCache tc = new TreeCache();
		Job j = new Job("process", new int[0], new HashMap<String, EntityIdentifier>(), context);
		tc.insertJob(j);
		assertTrue(tc.get(new int[0]).equals(j));
	}
	
	public void testStoreLoadWithDepth() {
		TreeCache tc = new TreeCache();
		Job j = new Job("process", new int[]{0}, new HashMap<String, EntityIdentifier>(), context);
		tc.insertJob(j);
		assertTrue(tc.get(new int[]{0}).equals(j));
	}
	
	public void testStoreLoadWithDepthAndGap() {
		TreeCache tc = new TreeCache();
		Job j = new Job("process", new int[]{1}, new HashMap<String, EntityIdentifier>(), context);
		tc.insertJob(j);
		assertTrue(tc.get(new int[]{1}).equals(j));
		assertTrue(tc.get(new int[]{0}) == null);
	}
	
	public void testStoreWithGap() {
		TreeCache tc = new TreeCache();
		Job j1 = new Job("process", new int[]{0,1}, new HashMap<String, EntityIdentifier>(), context);
		Job j2 = new Job("process", new int[]{0,0}, new HashMap<String, EntityIdentifier>(), context);
		tc.insertJob(j1);
		tc.insertJob(j2);
		System.out.println(tc);
		assertTrue(tc.get(new int[]{0,1}).equals(j1));
		assertTrue(tc.get(new int[]{0,0}).equals(j2));
	}
	
}
