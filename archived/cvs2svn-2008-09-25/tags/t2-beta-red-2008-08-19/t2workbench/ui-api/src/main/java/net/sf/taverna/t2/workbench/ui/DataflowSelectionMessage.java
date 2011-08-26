package net.sf.taverna.t2.workbench.ui;

/**
 * A message about the selection of a dataflow object.
 * 
 * @author David Withers
 */
public class DataflowSelectionMessage {

	public enum Type {ADDED, REMOVED}
	
	private Type type;
	
	private Object element;
	
	/**
	 * Constructs a new instance of DataflowSelectionMessage.
	 *
	 * @param type
	 * @param element
	 */
	public DataflowSelectionMessage(Type type, Object element) {
		this.type = type;
		this.element = element;
	}

	/**
	 * Returns the type of the message.
	 *
	 * @return the type of the message
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the subject of the message.
	 *
	 * @return the  of the message
	 */
	public Object getElement() {
		return element;
	}
	
}
