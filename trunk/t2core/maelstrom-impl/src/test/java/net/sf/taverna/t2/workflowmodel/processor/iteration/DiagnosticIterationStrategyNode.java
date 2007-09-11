package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

/**
 * Iteration strategy node that logs job and completion events for analysis
 * during debugging.
 * 
 * @author Tom
 * 
 */
public class DiagnosticIterationStrategyNode extends
		AbstractIterationStrategyNode {

	private Map<String, List<Event>> ownerToJobList;

	public DiagnosticIterationStrategyNode() {
		this.ownerToJobList = new HashMap<String, List<Event>>();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String owner : ownerToJobList.keySet()) {
			sb.append(owner + "\n");
			List<Event> jobs = ownerToJobList.get(owner);
			for (Event w : jobs) {
				sb.append("  " + w.toString() + "\n");
			}
		}
		return sb.toString();
	}

	public int jobsReceived(String string) {
		if (ownerToJobList.containsKey(string) == false) {
			return 0;
		}
		int number = 0;
		for (Event w : ownerToJobList.get(string)) {
			if (w instanceof Job) {
				number++;
			}
		}
		return number;
	}

	public boolean containsJob(String owningProcess, int[] jobIndex) {
		List<Event> jobs = ownerToJobList.get(owningProcess);
		if (jobs == null) {
			return false;
		}
		for (Event w : jobs) {
			if (w instanceof Job) {
				Job j = (Job)w;
				if (compareArrays(j.getIndex(), jobIndex)
						&& j.getOwningProcess().equals(owningProcess)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean compareArrays(int[] a1, int[] a2) {
		if (a1.length != a2.length) {
			return false;
		}
		for (int i = 0; i < a1.length; i++) {
			if (a1[i] != a2[i]) {
				return false;
			}
		}
		return true;
	}

	public synchronized void receiveCompletion(int inputIndex,
			Completion completion) {
		String owningProcess = completion.getOwningProcess();
		List<Event> jobs = ownerToJobList.get(owningProcess);
		if (jobs == null) {
			jobs = new ArrayList<Event>();
			ownerToJobList.put(owningProcess, jobs);
		}
		jobs.add(completion);
	}

	public synchronized void receiveJob(int inputIndex, Job newJob) {
		List<Event> jobs = ownerToJobList.get(newJob.getOwningProcess());
		if (jobs == null) {
			jobs = new ArrayList<Event>();
			ownerToJobList.put(newJob.getOwningProcess(), jobs);
		}
		jobs.add(newJob);
	}

	public int getIterationDepth(Map<String, Integer> inputDepths) throws IterationTypeMismatchException {
		// TODO Auto-generated method stub
		return 0;
	}
}
