package net.sf.taverna.t2.workbench.ui;

import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;

/**
 * The current state of the selection of dataflow objects.
 * 
 * @author David Withers
 */
public interface DataflowSelectionModel extends Observable<DataflowSelectionMessage> {

	/**
	 * Adds an element to the current selection.
	 * 
	 * If the element is not in the selection the Observers are notified. If
	 * <code>element</code> is null, this method has no effect.
	 * 
	 * @param element
	 *            the element to add
	 */
	public void addSelection(Object element);

	/**
	 * Removes an element from the current selection.
	 * 
	 * If the element is in the selection the Observers are notified. If
	 * <code>element</code> is null, this method has no effect.
	 * 
	 * @param element
	 *            the element to remove
	 */
	public void removeSelection(Object element);

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
	public void setSelection(Set<Object> elements);

	/**
	 * Returns the current selection.
	 * 
	 * Returns an empty set if nothing is currently selected.
	 * 
	 * @return the current selection
	 */
	public Set<Object> getSelection();

	/**
	 * Clears the current selection.
	 * 
	 * If this changes the selection the Observers are notified.
	 */
	public void clearSelection();

}
