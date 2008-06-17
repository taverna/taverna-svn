package net.sf.taverna.t2.partition;

import java.awt.datatransfer.Transferable;

/**
 * An interface that defines an "item" that is the result of performing a query
 * for an Activity type.
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 * @see ActivityQuery
 * @see Query
 * 
 */
public interface ActivityItem {
	/**
	 * Used during drag and drop operations. The activity palette (ie the Tree
	 * which has the {@link RootPartition} stuff) is made up of
	 * {@link ActivityItem}s each of which will return a {@link Transferable}.
	 * Inside this will be a wrapper containing the activity and a bean
	 */
	public Transferable getActivityTransferable();

}
