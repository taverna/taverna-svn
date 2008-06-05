package net.sf.taverna.t2.workbench.models.graph;

/**
 * A message about the selection of a graph element.
 * 
 * @author David Withers
 */
public class GraphSelectionMessage {

	public enum Type {ADDED, REMOVED}
	
	private Type type;
	
	private GraphElement element;
	
	/**
	 * Constructs a new instance of GraphSelectionMessage.
	 *
	 * @param type
	 * @param element
	 */
	public GraphSelectionMessage(Type type, GraphElement element) {
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
	public GraphElement getElement() {
		return element;
	}
	
}
