package net.sf.taverna.t2.partition;

import java.util.Set;

/**
 * Extension of the java Set interface with the addition of change listener
 * support. Intended to be plugged into the RootPartition class so the partition
 * is synchronized with the set membership.
 * 
 * @author Tom Oinn
 * 
 * @param <ItemType>
 *            the parameterised type of the set
 */
public interface SetModel<ItemType> extends Set<ItemType> {

	/**
	 * Add a listener to be notified of change events on the set's membership
	 * 
	 * @param listener
	 */
	public void addSetModelChangeListener(
			SetModelChangeListener<ItemType> listener);

	/**
	 * Remove a previously registered change listener
	 * 
	 * @param listener
	 */
	public void removeSetModelChangeListener(
			SetModelChangeListener<ItemType> listener);

}
