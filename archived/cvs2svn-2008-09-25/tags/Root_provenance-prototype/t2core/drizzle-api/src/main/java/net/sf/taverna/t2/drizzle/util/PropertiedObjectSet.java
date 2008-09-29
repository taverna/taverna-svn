package net.sf.taverna.t2.drizzle.util;

import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * A PropertiedObjectSet is a context within which objects of the specified
 * parameter class can be assocaited with PropertyKey + PropertyValue pairs.
 * 
 * The decision to expose PropertiedObject will be reviewed.
 * 
 * At the moment, for a given Object only a single PropertyValue can be
 * associated with a PropertyKey. It is not clear if this limit is reasonable
 * for future purposes.
 * 
 * @author alanrw
 * 
 * @param <O>
 */
@SuppressWarnings("unchecked")
public interface PropertiedObjectSet<O extends Beanable> extends
		Beanable<PropertiedObjectSetBean> {

	/**
	 * Add the specified PropertiedObjectListener to all PropertiedObjects
	 * within the PropertiedObjectSet. Note that if additional objects are added
	 * to the PropertiedObjectSet then their corresponding PropertiedObject will
	 * also have the PropertiedObjectListener added.
	 * 
	 * @param pol
	 */
	void addAllObjectsListener(final PropertiedObjectListener pol);

	/**
	 * Add a listener to the PropertiedObjectSet. The
	 * PropertiedObjectSetListener listens for the addition or removal of an
	 * Object (and its associated PropertiedObject).
	 * 
	 * @param posl
	 */
	void addListener(final PropertiedObjectSetListener posl);

	/**
	 * Add an object to the PropertiedObjectSet.
	 * 
	 */
	void addObject(final O object);

	/**
	 * Check if the PropertiedObjectSet contains the specified object and a
	 * corresponding PropertiedObject.
	 * 
	 * @param object
	 * @return
	 */
	boolean containsObject(final O object);

	/**
	 * Return all the PropertyKeys for which there is at least one object within
	 * the PropertiedObjectSet that associate a PropertyValue with the key.
	 * 
	 * @return
	 */
	Set<PropertyKey> getAllPropertyKeys();

	/**
	 * Return all the PropertyValues that for at least one object within the
	 * PropertiedObjectSet are associated with the specified key.
	 * 
	 * @param key
	 * @return
	 */
	Set<PropertyValue> getAllPropertyValues(PropertyKey key);

	/**
	 * Return all the objects within the PropertiedObjectSet.
	 * 
	 * @return
	 */
	Set<O> getObjects();

	Set<PropertyKey> getPropertyKeys(final O object);
	
	PropertyValue getPropertyValue(final O object, final PropertyKey key);
	
	boolean hasProperty(final O object, final PropertyKey key);

	/**
	 * Remove the PropertiedObjectListener from listening to all the
	 * PropertiedObjects corresponding to objects within the
	 * PropertiedObjectSet.
	 * 
	 * @param pol
	 */
	void removeAllObjectsListener(final PropertiedObjectListener pol);

	/**
	 * Remove the PropertiedObjectSetListener from listening to the
	 * PropertiedObjectSet.
	 * 
	 * @param posl
	 */
	void removeListener(final PropertiedObjectSetListener posl);

	/**
	 * Remove an object from the PropertiedObjectSet.
	 * 
	 * If the object is within the PropertiedObjectSet then it and its
	 * corresponding PropertiedObject are removed. Listeners to the
	 * PropertiedObjectSet are notified of the removal of the object.
	 * 
	 * @param key
	 */
	void removeObject(O key);

	/**
	 * Remove a PropertyKey and its associated PropertyValue from the
	 * PropertiedObject corresponding to the specified object.
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObject#removeProperty(net.sf.taverna.t2.service.util.PropertyKey)
	 * 
	 * @param o
	 * @param key
	 */
	void removeProperty(final O o, final PropertyKey key);

	/**
	 * Replay all the calls necessary to acquaint a listener with the current
	 * state of the objects within the PropertiedObjectSet.
	 * 
	 * Note that the calls are not necessarily the same as created the state.
	 */
	void replayToAllObjectsListener(final PropertiedObjectListener pol);

	/**
	 * Replay all the calls necessary to acquant a listener with the current
	 * state of the PropertiedObjectSet.
	 * 
	 * Note that the calls are not necessarily the same as created the state.
	 */
	void replayToListener(final PropertiedObjectSetListener posl);

	/**
	 * Set the PropertyValue associated with the PropertyKey for the
	 * PropertiedObject corresponding to the object.
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObject#setProperty(net.sf.taverna.t2.service.util.PropertyKey,
	 *      net.sf.taverna.t2.service.util.PropertyValue)
	 * 
	 * @param object
	 * @param key
	 * @param value
	 */
	void setProperty(final O object, final PropertyKey key,
			final PropertyValue value);

}