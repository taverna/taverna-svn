package net.sf.taverna.t2.partition;

import java.util.Set;

/**
 * Handler for change events on a SetModel instance
 * 
 * @author Tom Oinn
 * 
 */
public interface SetModelChangeListener<ItemType> {

	public void itemsWereAdded(Set<ItemType> newItems);
	
	public void itemsWereRemoved(Set<Object> itemsRemoved);
	
}
