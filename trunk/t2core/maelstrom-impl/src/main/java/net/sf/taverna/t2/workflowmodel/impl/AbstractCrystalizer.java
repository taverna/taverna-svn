package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.invocation.Completion;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.invocation.TreeCache;
import net.sf.taverna.t2.workflowmodel.processor.service.Job;

/**
 * Recieves Job and Completion events and emits Jobs unaltered. Completion
 * events additionally cause registration of lists for each key in the datamap
 * of the jobs at immediate child locations in the index structure. These list
 * identifiers are sent in place of the Completion events.
 * <p>
 * State for a given process ID is purged when a final completion event is
 * received so there is no need for an explicit cache purge operation in the
 * public API (although for termination of partially complete workflows it may
 * be sensible for subclasses to provide one)
 * <p>
 * 
 * @author Tom Oinn
 */
public abstract class AbstractCrystalizer implements Crystalizer {

	private Map<String, CompletionAwareTreeCache> cacheMap = new HashMap<String, CompletionAwareTreeCache>();

	public abstract Job getEmptyJob(String owningProcess, int[] index);

	/**
	 * Receive a Job or Completion, Jobs are emitted unaltered and cached,
	 * Completion events trigger registration of a corresponding list - this may
	 * be recursive in nature if the completion event's index implies nested
	 * lists which have not been registered.
	 * <p>
	 * If the baseListDepth property is defined then completion events on nodes
	 * which don't already exist create empty jobs instead and emit those, if
	 * undefined the completion event is emited intact.
	 * 
	 * @param e
	 */
	public void receiveEvent(Event e) {
		String owningProcess = e.getOwningProcess();
		CompletionAwareTreeCache cache = null;
		synchronized (cacheMap) {
			if (!cacheMap.containsKey(owningProcess)) {
				cache = new CompletionAwareTreeCache(owningProcess);
				cacheMap.put(owningProcess, cache);
			} else {
				cache = cacheMap.get(owningProcess);
			}
		}
		synchronized (cache) {
			if (e instanceof Job) {
				// Pass through Job after storing it in the cache
				Job j = (Job) e;
				cache.insertJob(j);
				jobCreated(j);
				if (j.getIndex().length == 0) {
					cacheMap.remove(j.getOwningProcess());
				}
				return;
			} else if (e instanceof Completion) {
				Completion c = (Completion) e;
				int[] completionIndex = c.getIndex();
				cache.resolveAt(owningProcess, completionIndex);
				if (c.getIndex().length == 0) {
					cacheMap.remove(c.getOwningProcess());
				}
			}
		}
	}

	protected class CompletionAwareTreeCache extends TreeCache {

		private String owningProcess;

		public CompletionAwareTreeCache(String owningProcess) {
			super();
			this.owningProcess = owningProcess;
		}

		public void resolveAt(String owningProcess, int[] completionIndex) {
			NamedNode n = nodeAt(completionIndex);
			if (n != null) {
				assignNamesTo(n, completionIndex);
			} else {

				// We know what the list depth should be, so we can
				// construct appropriate depth empty lists to fill in the
				// gaps.
				Job j = getEmptyJob(owningProcess, completionIndex);
				// System.out.println("Inserting new empty collection "+j);
				insertJob(j);
				jobCreated(j);

			}
		}

		private void assignNamesTo(NamedNode n, int[] index) {
			// Only act if contents of this node undefined
			// StringBuffer iString = new StringBuffer();
			// for (int foo : index) {
			// iString.append(foo+" ");
			// }
			// System.out.println("assignNamesTo "+iString.toString());
			if (n.contents == null) {
				Map<String, List<EntityIdentifier>> listItems = new HashMap<String, List<EntityIdentifier>>();
				int pos = 0;
				// System.out.println(" Unnamed node :
				// ["+iString.toString()+"]");
				// for (NamedNode child : n.children) {
				// System.out.println(" ++ "+child);
				// }
				for (NamedNode child : n.children) {

					// If child doesn't have a defined name map yet then define
					// it
					Job j;
					if (child == null) {
						// happens if we're completing a partially empty
						// collection structure
						int[] newIndex = new int[index.length + 1];
						for (int i = 0; i < index.length; i++) {
							newIndex[i] = index[i];
						}
						newIndex[index.length] = pos++;
						j = getEmptyJob(owningProcess, newIndex);
						AbstractCrystalizer.this.jobCreated(j);
					} else {

						if (child.contents == null) {
							int[] newIndex = new int[index.length + 1];
							for (int i = 0; i < index.length; i++) {
								newIndex[i] = index[i];
							}
							newIndex[index.length] = pos++;
							assignNamesTo(child, newIndex);
						} else {
							pos++;
						}
						j = child.contents;
					}
					// Now pull the names out of the child job map and push them
					// into lists to be registered

					for (String outputName : j.getData().keySet()) {
						List<EntityIdentifier> items = listItems
								.get(outputName);
						if (items == null) {
							items = new ArrayList<EntityIdentifier>();
							listItems.put(outputName, items);
						}
						items.add(j.getData().get(outputName));
					}
				}
				Map<String, EntityIdentifier> newDataMap = new HashMap<String, EntityIdentifier>();
				for (String outputName : listItems.keySet()) {
					List<EntityIdentifier> idlist = listItems.get(outputName);
					newDataMap.put(outputName, ContextManager.getDataManager(
							owningProcess).registerList(
							idlist.toArray(new EntityIdentifier[0])));
				}
				Job newJob = new Job(owningProcess, index, newDataMap);
				n.contents = newJob;
				// Get rid of the children as we've now named this node
				n.children.clear();
				AbstractCrystalizer.this.jobCreated(n.contents);
			}
		}

	}

}
