package net.sf.taverna.t2.workflowmodel.processor.dispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.AbstractCrystalizer;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;

public class DispatchStackTestWithParallelizeTest extends TestCase {

	public InvocationContext context = new TestInvocationContext();
	
	private class BasicDispatchStackImpl extends DispatchStackImpl {
		
		private List<Activity<?>> activities;
		
		public BasicDispatchStackImpl(List<Activity<?>> activityList) {
			this.activities = new ArrayList<Activity<?>>();
			for (Activity<?> s : activityList) {
				activities.add(s);
			}		
		}

		@Override
		protected boolean conditionsSatisfied(String owningProcess) {
			return true;
		}

		@Override
		protected List<? extends Activity<?>> getActivities() {
			return this.activities;
		}

		@Override
		protected void pushEvent(IterationInternalEvent<? extends IterationInternalEvent<?>> e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void finishedWith(String owningProcess) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected String getProcessName() {
			return "process";
		}

		public void receiveMonitorableProperty(MonitorableProperty<?> prop,
				String processID) {
			// TODO Auto-generated method stub
			
		}

		public Processor getProcessor() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public void testStackConstructionAndSimpleInvocation() {
		System.out.println("Multiple jobs, single process identifier");
		DispatchStackImpl d = new BasicDispatchStackImpl(new ArrayList<Activity<?>>());
		d.addLayer(new DiagnosticLayer());
		d.addLayer(new Parallelize());
		d.addLayer(new DummyInvokerLayer());
		for (IterationInternalEvent<?> e : generateLotsOfEvents("Process1", 10)) {
			d.receiveEvent(e);
		}
		try {
			Thread.sleep(3000);
			System.out.println("--------------------------------------------------\n");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void testMultipleProcessIDs() {
		System.out.println("Multiple jobs, multiple process identifiers");
		DispatchStackImpl d = new BasicDispatchStackImpl(new ArrayList<Activity<?>>());
		d.addLayer(new DiagnosticLayer());
		d.addLayer(new Parallelize());
		d.addLayer(new DummyInvokerLayer());
		for (IterationInternalEvent<?> e : generateLotsOfEvents("Process1", 10)) {
			d.receiveEvent(e);
		}
		for (IterationInternalEvent<?> e : generateLotsOfEvents("Process2", 6)) {
			d.receiveEvent(e);
		}
		try {
			Thread.sleep(3000);
			System.out.println("--------------------------------------------------\n");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Test that we still get a cache purge when there are no explicit
	 * completion events, in this case because a job is submitted with an empty
	 * index array (i.e. no iteration)
	 * 
	 */
	public void testSingleJob() {
		System.out.println("Single job");
		DispatchStackImpl d = new BasicDispatchStackImpl(new ArrayList<Activity<?>>());
		d.addLayer(new DiagnosticLayer());
		d.addLayer(new Parallelize());
		d.addLayer(new DummyInvokerLayer());
		Map<String, T2Reference> dataMap = new HashMap<String, T2Reference>();
		dataMap.put("SingleJobInput", TestInvocationContext.nextReference());
		d.receiveEvent(new Job("Process1:processorName", new int[] {}, dataMap, context));
		try {
			Thread.sleep(1000);
			System.out.println("--------------------------------------------------\n");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Test single job with streaming
	 */
	public void testSingleJobWithStream() {
		System.out.println("Single job with streaming");
		DispatchStackImpl d = new BasicDispatchStackImpl(new ArrayList<Activity<?>>());
		d.addLayer(new DiagnosticLayer());
		d.addLayer(new Parallelize());
		d.addLayer(new DummyStreamingInvokerLayer());
		Map<String, T2Reference> dataMap = new HashMap<String, T2Reference>();
		dataMap.put("SingleJobInput", TestInvocationContext.nextReference());
		d.receiveEvent(new Job("Process1:processorName", new int[] {}, dataMap, context));
		try {
			Thread.sleep(2000);
			System.out.println("--------------------------------------------------\n");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Test streaming job with crystalizer added
	 */
	public void testSingleJobWithStreamAndCrystalize() {
		System.out.println("Single job with streaming");
		final AbstractCrystalizer c = new AbstractCrystalizer() {
		
			public void completionCreated(Completion completion) {
				System.out.println(" ** "+completion.toString());
			}
		
			public void jobCreated(Job outputJob) {
				System.out.println(" ** "+outputJob.toString());
			}

			@Override
			public Job getEmptyJob(String owningProcess, int[] index, InvocationContext context) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		DispatchStackImpl d = new BasicDispatchStackImpl(new ArrayList<Activity<?>>()) {
			@Override
			protected void pushEvent(IterationInternalEvent<? extends IterationInternalEvent<?>> e) {
				c.receiveEvent(e);
			}
		};
		d.addLayer(new DiagnosticLayer());
		d.addLayer(new Parallelize());
		d.addLayer(new DummyStreamingInvokerLayer());
		Map<String, T2Reference> dataMap = new HashMap<String, T2Reference>();
		dataMap.put("SingleJobInput", TestInvocationContext.nextReference());
		d.receiveEvent(new Job("Process1:processorName", new int[] {}, dataMap, context));
		try {
			Thread.sleep(2000);
			System.out.println("--------------------------------------------------\n");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Test parallel jobs with streaming
	 */
	public void testParallelJobsWithStream() {
		System.out.println("Parallel jobs with streaming");
		DispatchStackImpl d = new BasicDispatchStackImpl(new ArrayList<Activity<?>>());
		d.addLayer(new DiagnosticLayer());
		d.addLayer(new Parallelize());
		d.addLayer(new DummyStreamingInvokerLayer());
		for (IterationInternalEvent<?> e : generateLotsOfEvents("Process1", 4)) {
			d.receiveEvent(e);
		}
		try {
			Thread.sleep(3000);
			System.out.println("--------------------------------------------------\n");
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Convenience method to generate a list of Event objects to be used as
	 * input to the dispatch stack. Each list consists of a number of Job
	 * objects followed by an appropriate Completion and is reasonably
	 * representative of what you might see in a real workflow
	 * 
	 * @param processID
	 * @param jobs
	 * @return
	 */
	private List<IterationInternalEvent<?>> generateLotsOfEvents(String processID, int jobs) {
		List<IterationInternalEvent<?>> events = new ArrayList<IterationInternalEvent<?>>();
		for (int i = 0; i < jobs; i++) {
			Map<String, T2Reference> dataMap = new HashMap<String, T2Reference>();
			dataMap.put("Input1", TestInvocationContext.nextReference());
			events.add(new Job(processID+":processorName", new int[] { i }, dataMap, context));
		}
		events.add(new Completion(processID+":processorName", context));
		return events;
	}

}
