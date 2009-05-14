package net.sf.taverna.t2.platform.util.reflect;

/**
 * Provides methods to instantiate and configure objects from classes managed by
 * the plug-in management system
 * 
 * @author Tom Oinn
 * 
 */
public interface ReflectionHelper {

	/**
	 * Set a property on the target object by calling the set<em>PropertyName</em>
	 * method through reflection.
	 * 
	 * @param target
	 *            the object on which the property is to be set
	 * @param propertyName
	 *            the name of a property, the method name will be derived from
	 *            this using the java bean convention of a set<em>PropertyName</em>
	 *            method with a single argument
	 * @param value
	 *            the value of the property to set
	 * @throws ReflectionException
	 *             if a reflection checked exception is thrown, this is
	 *             generally because the particular property can't be found or
	 *             isn't of the supplied type but can also be due to security or
	 *             other access constraint violations
	 */
	public void setProperty(Object target, String propertyName, Object value)
			throws ReflectionException;

	/**
	 * Get a property value from the target object by calling the get<em>PropertyName</em>
	 * through reflection
	 * 
	 * @param target
	 *            the object from which to retrieve the property value
	 * @param propertyName
	 *            the property to fetch
	 * @return the property value
	 * @throws ReflectionException
	 *             if the property doesn't exist or if any reflection based
	 *             exception is thrown when attempting to look up and call the
	 *             appropriate method
	 */
	public Object getProperty(Object target, String propertyName)
			throws ReflectionException;

	/**
	 * Construct a new instance of an object from a class located by name out of
	 * the active plug-in set in the associated plug-in manager. Iterates
	 * through the currently active plug-ins until it manages to find a
	 * corresponding class definition and invokes the constructor corresponding
	 * to the specified arguments.
	 * 
	 * @param className
	 *            the class name to construct
	 * @param args
	 *            arguments corresponding to arguments to a constructor of the
	 *            located class
	 * @return the newly constructed object
	 * @throws ReflectionException
	 *             if any reflection or plug-in based exception was thrown
	 *             during lookup and object instantiation
	 */
	public Object construct(String className, Object... args)
			throws ReflectionException;

	/**
	 * Construct a new instance of an object as in the {@link construct} method
	 * but also configure it by calling setter methods for any properties in the
	 * propertyStrings argument. These property strings are interpreted as
	 * 'propertyName=propertyValue' with whitespace being included. If the
	 * property type discovered on the object (the first if multiple such exist)
	 * is an object type the corresponding object will be constructed with the
	 * string value of the property as its sole constructor argument. If no such
	 * constructor exists this will throw an exception. Because of the way
	 * reflection works in Java this will also work for primitive types - the
	 * reflection system presents these internally as their wrapper types all of
	 * which have single string constructors.
	 * 
	 * @param className
	 *            the class name to construct
	 * @param propertyStrings
	 *            an array of 'PropertyName=value' strings defining properties
	 *            to set, in order, on the newly constructed object
	 * @param constructorArgs
	 *            arguments to the constructor
	 * @return the newly instantiated and configured object
	 * @throws ReflectionException
	 *             if any reflection or plug-in based exception was thrown
	 *             during lookup, instantiation or configuration
	 */
	public Object createAndConfigure(String className,
			String[] propertyStrings, Object... constructorArgs)
			throws ReflectionException;

}
