package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.TreeCache;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

/**
 * The dot product matches jobs by index array, when a job is received a job is
 * emited if and only if the index array of the new job is matched exactly by
 * index arrays of one job in each other input index.
 * 
 * @author Tom Oinn
 * 
 */
public class DotProduct extends CompletionHandlingAbstractIterationStrategyNode {

	Map<String, TreeCache[]> ownerToCache = new HashMap<String, TreeCache[]>();

	@Override
	public synchronized void innerReceiveJob(int inputIndex, Job newJob) {
		String owningProcess = newJob.getOwningProcess();
		if (!ownerToCache.containsKey(owningProcess)) {
			TreeCache[] caches = new TreeCache[getChildCount()];
			for (int i = 0; i < getChildCount(); i++) {
				caches[i] = new TreeCache();
			}
			ownerToCache.put(owningProcess, caches);
		}
		// Firstly store the new job in the cache, this isn't optimal but is
		// safe for now - we can make this more efficient by doing the
		// comparison first and only storing the job if required
		TreeCache[] caches = ownerToCache.get(owningProcess);
		caches[inputIndex].insertJob(newJob);
		int[] indexArray = newJob.getIndex();
		boolean foundMatch = true;
		Map<String, T2Reference> newDataMap = new HashMap<String, T2Reference>();
		for (TreeCache cache : caches) {

			if (cache.containsLocation(indexArray)) {
				newDataMap.putAll(cache.get(indexArray).getData());
			} else {
				foundMatch = false;
			}
		}
		if (foundMatch) {
			Job j = new Job(owningProcess, indexArray, newDataMap, newJob
					.getContext());
			// Remove all copies of the job with this index from the cache,
			// we'll never use it
			// again and it pays to be tidy
			for (TreeCache cache : caches) {
				cache.cut(indexArray);
			}
			pushJob(j);
		}
	}

	/**
	 * Delegate to the superclass to propogate completion events if and only if
	 * the completion event is a final one. We can potentially implement finer
	 * grained logic here in the future.
	 */
	@Override
	public synchronized void innerReceiveCompletion(int inputIndex,
			Completion completion) {
		// Do nothing, let the superclass handle final completion events, ignore
		// others for now (although in theory we should be able to do better
		// than this really)
	}

	@Override
	protected synchronized void cleanUp(String owningProcess) {
		ownerToCache.remove(owningProcess);
	}

	public int getIterationDepth(Map<String, Integer> inputDepths)
			throws IterationTypeMismatchException {
		// Check that all input depths are the same
		int depth = getChildren().get(0).getIterationDepth(inputDepths);
		for (IterationStrategyNode childNode : getChildren()) {
			if (childNode.getIterationDepth(inputDepths) != depth) {
				throw new IterationTypeMismatchException(
						"Mismatched input types for dot product node");
			}
		}
		return depth;
	}

}
