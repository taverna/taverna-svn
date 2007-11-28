package net.sf.taverna.t2.invocation;

/**
 * Abstract superclass of Completion and Job, no real functionality but exists
 * so we can create a queue of Job|Completion
 * 
 * @author Tom Oinn
 * 
 */
public abstract class Event {

	protected String owner;

	protected InvocationContext context;
	
	protected int[] index;

	/**
	 * The event has an owner, this is represented as a String object but the
	 * ownership is hierarchical in nature. The String is a colon separated list
	 * of alphanumeric process identifiers, with identifiers being pushed onto
	 * this list on entry to a process and popped off on exit.
	 * 
	 * @return String of colon separated process identifiers owning this Job
	 */
	public String getOwningProcess() {
		return this.owner;
	}

	public InvocationContext getContext() {
		return this.context;
	}
	
	/**
	 * Events have an index placing them in a conceptual tree structure. This
	 * index is carried along with the event and used at various points to drive
	 * iteration and ensure that separate jobs are kept that way
	 */
	public int[] getIndex() {
		return this.index;
	}

	/**
	 * Pop a previously pushed index array off the process name and append the
	 * current index array to create the new index array. This is applied to a
	 * new instance of an Event subclass and does not modify the target.
	 * 
	 * @return new Event subclass with modified owning process and index
	 */
	public abstract Event popIndex();

	/**
	 * Push the index array onto the owning process name and return the new Job
	 * object. Does not modify this object, the method creates a new Event
	 * subclass with the modified index array and owning process.
	 * 
	 */
	public abstract Event pushIndex();

	/**
	 * Helper method for the pushIndex operation
	 * 
	 * @return
	 */
	protected String getPushedOwningProcess() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < index.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(index[i]);
		}
		String indexArrayAsString = sb.toString();
		return (owner + ":" + indexArrayAsString);
	}

	/**
	 * Helper method for the popIndex operation, returns the modified index
	 * array. Subclasses must still implement logic to get the modified owning
	 * process but that's relatively easy :
	 * <code>
	 * return new <Event subclass>(owner.substring(0, owner.lastIndexOf(':')),getPoppedIndex(), dataMap);
	 * </code>
	 * 
	 * @return
	 */
	protected int[] getPoppedIndex() {
		int lastLocation = owner.lastIndexOf(':');
		String indexArrayAsString = owner.substring(lastLocation + 1);
		String[] parts = indexArrayAsString.split(",");
		int[] newIndexArray = new int[index.length + parts.length];
		int pos = 0;
		for (String part : parts) {
			newIndexArray[pos++] = Integer.parseInt(part);
		}
		for (int i : index) {
			newIndexArray[pos++] = i;
		}
		return newIndexArray;
	}

}
