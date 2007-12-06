package net.sf.taverna.t2.invocation;

/**
 * Abstract superclass of all 'event' types within a workflow invocation. These
 * are the Job and Completion events which are used internally within a
 * Processor, in particular by the dispatch stack and iteration system, and the
 * WorkflowDataToken which is the only event class that can exist outside of a
 * Processor boundary (and is therefore the most significant one for users of
 * the API)
 * 
 * @author Tom Oinn
 * 
 */
public abstract class Event<EventType extends Event<?>> {

	protected String owner;

	protected InvocationContext context;

	protected int[] index;

	protected Event(String owner, int[] index, InvocationContext context) {
		this.owner = owner;
		this.index = index;
		this.context = context;
		if (index == null) {
			throw new RuntimeException("Job index cannot be null");
		}
		if (owner == null) {
			throw new RuntimeException("Owning process cannot be null");
		}
		if (context == null) {
			throw new RuntimeException("Invocation context cannot be null");
		}
	}

	/**
	 * An event is final if its index array is zero length
	 * 
	 * @return true if indexarray.length==0
	 */
	public final boolean isFinal() {
		return (index.length == 0);
	}

	/**
	 * The event has an owner, this is represented as a String object but the
	 * ownership is hierarchical in nature. The String is a colon separated list
	 * of alphanumeric process identifiers, with identifiers being pushed onto
	 * this list on entry to a process and popped off on exit.
	 * 
	 * @return String of colon separated process identifiers owning this Job
	 */
	public final String getOwningProcess() {
		return this.owner;
	}

	public final InvocationContext getContext() {
		return this.context;
	}

	/**
	 * Return a copy of the event subclass with the last owning process removed
	 * from the owning process list. For example, if the event had owner
	 * 'foo:bar' this would return a duplicate event with owner 'foo'. If the
	 * owning process is the empty string this is invalid and will throw a
	 * ProcessIdentifierException
	 * 
	 * @return a copy of the event with the parent process identifier
	 */
	public abstract EventType popOwningProcess()
			throws ProcessIdentifierException;

	/**
	 * Return a copy of the event subclass with the specified local process name
	 * appended to the owning process identifier field. If the original owner
	 * was 'foo' and this was called with 'bar' you'd end up with a copy of the
	 * subclass with owner 'foo:bar'
	 * 
	 * @param localProcessName
	 *            name to add
	 * @return the modified event
	 * @throws ProcessIdentifierException
	 *             if the local process name contains the ':' character
	 */
	public abstract EventType pushOwningProcess(String localProcessName)
			throws ProcessIdentifierException;

	/**
	 * Events have an index placing them in a conceptual tree structure. This
	 * index is carried along with the event and used at various points to drive
	 * iteration and ensure that separate jobs are kept that way
	 */
	public final int[] getIndex() {
		return this.index;
	}

	/**
	 * Pop a previously pushed index array off the process name and append the
	 * current index array to create the new index array. This is applied to a
	 * new instance of an Event subclass and does not modify the target.
	 * 
	 * @return new Event subclass with modified owning process and index
	 */
	public abstract EventType popIndex();

	/**
	 * Push the index array onto the owning process name and return the new
	 * Event subclass object. Does not modify this object, the method creates a
	 * new Event subclass with the modified index array and owning process.
	 * 
	 */
	public abstract EventType pushIndex();

	/**
	 * Helper method for the pushIndex operation
	 * 
	 * @return
	 */
	protected final String getPushedOwningProcess() {
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
	 * process but that's relatively easy : <code>
	 * return new <Event subclass>(owner.substring(0, owner.lastIndexOf(':')),getPoppedIndex(), dataMap);
	 * </code>
	 * 
	 * @return
	 */
	protected final int[] getPoppedIndex() {
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

	protected final String popOwner() throws ProcessIdentifierException {
		// Empty string already, can't pop from here, throw exception
		if (owner.equals("")) {
			throw new ProcessIdentifierException(
					"Attempt to pop a null owning process (empty string)");
		}
		// A single ID with no colon in, return the empty string
		if (owner.lastIndexOf(':') < 0) {
			return "";
		}
		return owner.substring(0, owner.lastIndexOf(':'));
	}

	protected final String pushOwner(String newLocalProcess)
			throws ProcessIdentifierException {
		if (newLocalProcess.contains(":")) {
			throw new ProcessIdentifierException("Can't push '"
					+ newLocalProcess + "' as it contains a ':' character");
		}
		return owner + ":" + newLocalProcess;
	}

}
