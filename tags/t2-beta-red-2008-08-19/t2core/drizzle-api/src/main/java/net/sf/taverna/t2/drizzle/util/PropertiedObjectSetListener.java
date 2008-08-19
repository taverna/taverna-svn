/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

/**
 * The PropertiedObjectSetListener listens for the addition or removal of
 * objects within a PropertiedObjectSet.
 * 
 * @author alanrw
 * 
 */
public interface PropertiedObjectSetListener {
	/**
	 * An object has been added to the specified PropertiedObjectSet.
	 * 
	 * @param pos
	 * @param o
	 */
	void objectAdded(PropertiedObjectSet<?> pos, Object o);

	/**
	 * An object has been removed from the specified PropertiedObjectSet.
	 * 
	 * @param pos
	 * @param o
	 */
	void objectRemoved(PropertiedObjectSet<?> pos, Object o);
}
