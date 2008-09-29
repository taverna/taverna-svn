package net.sf.taverna.t2.drizzle.util;

/**
 * @author alanrw
 *
 * A PropertiedTreeObjectNode encapsulates an object within a PropertiedTreeModel.
 * 
 * @param <O> The class of object that is encapsulated.
 */
public interface PropertiedTreeObjectNode<O> extends PropertiedTreeNode<O> {
	/**
	 * Set the object to be encapsulated.
	 * 
	 * @param object
	 */
	void setObject(final O object);
	
	/**
	 * Return the object that is encapsulated.
	 * @return
	 */
	O getObject();
}
