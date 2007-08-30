package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.TreeCache;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

/**
 * The dot product matches jobs by index array, when a job is received a job is
 * emited if and only if the index array of the new job is matched exactly by
 * index arrays of one job in each other input index.
 * 
 * @author Tom Oinn
 * 
 */
public class DotProduct extends AbstractIterationStrategyNode {

	Map<String, TreeCache[]> ownerToCache = new HashMap<String, TreeCache[]>();

	public synchronized void receiveJob(int inputIndex, Job newJob) {
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
		Map<String, EntityIdentifier> newDataMap = new HashMap<String, EntityIdentifier>();
		for (TreeCache cache : caches) {

			if (cache.containsLocation(indexArray)) {
				newDataMap.putAll(cache.get(indexArray).getData());
			} else {
				foundMatch = false;
			}
		}
		if (foundMatch) {
			Job j = new Job(owningProcess, indexArray, newDataMap);
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
	public synchronized void receiveCompletion(int inputIndex,
			Completion completion) {
		if (completion.isFinal()) {
			boolean allDone = receiveFinalCompletion(completion
					.getOwningProcess(), inputIndex);
			if (allDone) {
				ownerToCache.remove(completion.getOwningProcess());
				pushCompletion(completion);
			}
		}
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
