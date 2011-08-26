package net.sf.taverna.t2.reference;

/**
 * Specialization of ExternalReferenceSPI for reference types which carry a
 * value type internally. Such references can be de-referenced to the specified
 * object type very cheaply. Note that this is not to be used to get an object
 * property of a reference, the returned object must correspond to the value of
 * the referenced data - this means that the HttpUrlReference does not use this
 * to return a java.net.URL, but that the InlineStringReference does use it to
 * return a java.lang.String
 * 
 * @author Tom Oinn
 * 
 */
public interface ValueCarryingExternalReference<T> extends ExternalReferenceSPI {

	/**
	 * Returns the type of the inlined value
	 */
	public Class<T> getValueType();

	/**
	 * Returns the value
	 */
	public T getValue();

}
