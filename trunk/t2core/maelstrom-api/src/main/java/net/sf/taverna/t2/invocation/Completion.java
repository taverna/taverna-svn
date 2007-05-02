package net.sf.taverna.t2.invocation;

/**
 * Contains a (possibly partial) completion event. The completion event is a
 * statement that no further events will occur on this channel with an index
 * prefixed by the completion index. As with Job events completion events have
 * an owning process with the same semantics as that of the Job class
 * 
 * @author Tom Oinn
 * 
 */
public class Completion extends Event {

	/**
	 * Construct a new optionally partial completion event with the specified
	 * owner and completion index
	 * 
	 * @param owningProcess
	 * @param completionIndex
	 */
	public Completion(String owningProcess, int[] completionIndex) {
		this.owner = owningProcess;
		this.index = completionIndex;
	}

	/**
	 * Construct a new final completion event, equivalent to calling new
	 * Completion(owningProcess, new int[0]);
	 * 
	 * @param owningProcess
	 */
	public Completion(String owningProcess) {
		this.owner = owningProcess;
		this.index = new int[0];
	}

	/**
	 * A completion event is final if its index array is zero length
	 * 
	 * @return true if indexarray.length==0
	 */
	public boolean isFinal() {
		return (index.length == 0);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Cmp(" + owner + ")[");
		for (int i = 0; i < index.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(index[i] + "");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Push the index array onto the owning process name and return the new Job
	 * object. Does not modify this object, the method creates a new Job with
	 * the modified index array and owning process
	 * 
	 * @return
	 */
	public Completion pushIndex() {
		return new Completion(getPushedOwningProcess(), new int[] {});
	}

	/**
	 * Pull the index array previous pushed to the owning process name and
	 * prepend it to the current index array
	 */
	public Completion popIndex() {
		return new Completion(owner.substring(0, owner.lastIndexOf(':')),
				getPoppedIndex());
	}

}
