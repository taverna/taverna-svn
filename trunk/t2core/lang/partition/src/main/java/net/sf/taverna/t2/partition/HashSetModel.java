package net.sf.taverna.t2.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HashSetModel<ItemType> extends HashSet<ItemType> implements
		SetModel<ItemType> {

	// Listeners for set change events
	private List<SetModelChangeListener<ItemType>> changeListeners;

	/**
	 * Default constructor, creates a set model based on a HashSet
	 */
	public HashSetModel() {
		super();
		changeListeners = new ArrayList<SetModelChangeListener<ItemType>>();
	}

	/**
	 * Implements SetModel
	 */
	public synchronized void addSetModelChangeListener(
			SetModelChangeListener<ItemType> listener) {
		changeListeners.add(listener);
	}

	/**
	 * Implements SetModel
	 */
	public synchronized void removeSetModelChangeListener(
			SetModelChangeListener<ItemType> listener) {
		changeListeners.remove(listener);
	}

	@Override
	public boolean add(ItemType item) {
		if (super.add(item)) {
			notifyAddition(Collections.singleton(item));
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object item) {
		if (super.remove(item)) {
			notifyRemoval(Collections.singleton(item));
			return true;
		}
		return false;
	}

	/**
	 * Push addition notification to listeners
	 * 
	 * @param itemsAdded
	 */
	private synchronized void notifyAddition(Set<ItemType> itemsAdded) {
		for (SetModelChangeListener<ItemType> listener : new ArrayList<SetModelChangeListener<ItemType>>(
				changeListeners)) {
			listener.itemsWereAdded(itemsAdded);
		}
	}

	/**
	 * Push removal notification to listeners
	 * 
	 * @param itemsRemoved
	 */
	private synchronized void notifyRemoval(Set<Object> itemsRemoved) {
		for (SetModelChangeListener<ItemType> listener : new ArrayList<SetModelChangeListener<ItemType>>(
				changeListeners)) {
			listener.itemsWereRemoved(itemsRemoved);
		}
	}
}
