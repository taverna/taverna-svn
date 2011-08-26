package net.sf.taverna.t2.workflowmodel.processor.iteration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.TreeCache;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.Job;

/**
 * Matches jobs where the index array of the job on index 0 is the prefix of the
 * index array of the job on index 1. This node can only ever have exactly two
 * child nodes!
 * 
 * @author Tom Oinn
 * 
 */
public class PrefixDotProduct extends DotProduct {

	@Override
	protected synchronized final void cleanUp(String owningProcess) {
		ownerToCache.remove(owningProcess);
	}

	@Override
	public void innerReceiveJob(int inputIndex, Job newJob) {
		String owningProcess = newJob.getOwningProcess();
		TreeCache[] caches;
		synchronized (ownerToCache) {
			caches = ownerToCache.get(owningProcess);
			// Create the caches if not already initialized
			if (caches == null) {
				caches = new TreeCache[getChildCount()];
				for (int i = 0; i < getChildCount(); i++) {
					caches[i] = new TreeCache();
				}
				ownerToCache.put(owningProcess, caches);
			}
		}

		// Store the job
		caches[inputIndex].insertJob(newJob);

		// If this job came in on index 0 we have to find all jobs in the cache
		// for index 1 which have the index array as a prefix. Fortunately this
		// is quite easy due to the tree structure of the cache, we can just ask
		// for all nodes in the cache with that index.
		if (inputIndex == 0) {
			int[] prefixIndexArray = newJob.getIndex();
			List<Job> matchingJobs;
			synchronized (caches[1]) {
				// Match all jobs and remove them so other calls can't produce
				// duplicates
				matchingJobs = caches[1].jobsWithPrefix(prefixIndexArray);
				caches[1].cut(prefixIndexArray);
			}
			for (Job job : matchingJobs) {
				Map<String, T2Reference> newDataMap = new HashMap<String, T2Reference>();
				newDataMap.putAll(newJob.getData());
				newDataMap.putAll(job.getData());
				Job mergedJob = new Job(owningProcess, job.getIndex(),
						newDataMap, newJob.getContext());
				pushJob(mergedJob);
			}
		}

		// If the job came in on index 1 we have to find the job on index 0 that
		// matches the first 'n' indices, where 'n' is determined by the depth
		// of jobs on the cache for index 0.
		else if (inputIndex == 1) {
			// Only act if we've received jobs on the cache at index 0
			if (caches[0].getIndexLength() > 0) {
				int[] prefix = new int[caches[0].getIndexLength()];
				for (int i = 0; i < prefix.length; i++) {
					prefix[i] = newJob.getIndex()[i];
				}
				Job j = caches[0].get(prefix);
				if (j != null) {
					Map<String, T2Reference> newDataMap = new HashMap<String, T2Reference>();
					newDataMap.putAll(j.getData());
					newDataMap.putAll(newJob.getData());
					Job mergedJob = new Job(owningProcess, newJob.getIndex(),
							newDataMap, newJob.getContext());
					pushJob(mergedJob);
				}
			}
		}

	}

}
