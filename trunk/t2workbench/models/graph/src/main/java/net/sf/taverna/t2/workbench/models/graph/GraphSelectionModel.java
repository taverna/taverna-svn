package net.sf.taverna.t2.workbench.models.graph;

import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;

/**
 * The current state of the selection of graph elements.
 * 
 * @author David Withers
 */
public interface GraphSelectionModel extends Observable<GraphSelectionMessage> {

	/**
	 * Adds an Element to the current selection.
	 * 
	 * If the Element is not in the selection the Observers are notified. If
	 * <code>element</code> is null, this method has no effect.
	 * 
	 * @param element
	 *            the Element to add
	 */
	public void addSelection(GraphElement element);

	/**
	 * Removes an Element from the current selection.
	 * 
	 * If the Element is in the selection the Observers are notified. If
	 * <code>element</code> is null, this method has no effect.
	 * 
	 * @param element
	 *            the Element to remove
	 */
	public void removeSelection(GraphElement element);

	/**
	 * Sets the current selection.
	 * 
	 * If this changes the selection the Observers are notified. If
	 * <code>elements</code> is null, this has the same effect as invoking
	 * <code>clearSelection</code>.
	 * 
	 * @param elements
	 *            the current selection
	 */
	public void setSelection(Set<GraphElement> elements);

	/**
	 * Returns the current selection.
	 * 
	 * Returns an empty set if nothing is currently selected.
	 * 
	 * @return the current selection
	 */
	public Set<GraphElement> getSelection();

	/**
	 * Clears the current selection.
	 * 
	 * If this changes the selection the Observers are notified.
	 */
	public void clearSelection();

}
