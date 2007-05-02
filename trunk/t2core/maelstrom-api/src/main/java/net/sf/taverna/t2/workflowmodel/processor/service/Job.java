package net.sf.taverna.t2.workflowmodel.processor.service;

import java.util.Map;

import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.invocation.Event;

/**
 * Contains a (possibly partial) job description. A job is the smallest entity
 * that can be enacted by the invocation layer of the dispatch stack within a
 * processor. Jobs are partial jobs if the set of keys in the data map is not
 * identical to the set of named input ports on the processor within which the
 * job is used. These objects are used internally within the processor to stage
 * data during iteration and within the dispatch stack, they do not appear
 * within the workflow itself.
 * 
 * @author Tom Oinn
 * 
 */
public class Job extends Event {

	private Map<String, EntityIdentifier> dataMap;

	/**
	 * Push the index array onto the owning process name and return the new Job
	 * object. Does not modify this object, the method creates a new Job with
	 * the modified index array and owning process
	 * 
	 * @return
	 */
	public Job pushIndex() {
		return new Job(getPushedOwningProcess(), new int[] {}, dataMap);
	}

	/**
	 * Pull the index array previous pushed to the owning process name and
	 * prepend it to the current index array
	 */
	public Job popIndex() {
		return new Job(owner.substring(0, owner.lastIndexOf(':')),
				getPoppedIndex(), dataMap);
	}

	/**
	 * The actual data carried by this (partial) Job object is in the form of a
	 * map, where the keys of the map are Strings identifying the named input
	 * and the values are Strings containing valid data identifiers within the
	 * context of a visible DataManager object (see CloudOne specification for
	 * further information on the DataManager system)
	 * 
	 * @return Map of name to data reference for this Job
	 */
	public Map<String, EntityIdentifier> getData() {
		return this.dataMap;
	}

	/**
	 * Create a new Job object with the specified owning process (colon
	 * separated 'list' of process identifiers), index array and data map
	 * 
	 * @param owner
	 * @param index
	 * @param data
	 */
	public Job(String owner, int[] index, Map<String, EntityIdentifier> data) {
		this.owner = owner;
		this.index = index;
		this.dataMap = data;
	}

	/**
	 * Show the owner, index array and data map in textual form for debugging
	 * and any other purpose. Jobs appear in the form :
	 * 
	 * <pre>
	 * Job(Process1)[2,0]{Input2=dataID4,Input1=dataID3}
	 * </pre>
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Job(" + owner + ")[");
		for (int i = 0; i < index.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(index[i] + "");
		}
		sb.append("]{");
		boolean first = true;
		for (String key : dataMap.keySet()) {
			if (!first) {
				sb.append(",");
			}
			sb.append(key + "=" + dataMap.get(key));
			first = false;
		}
		sb.append("}");
		return sb.toString();
	}

}
