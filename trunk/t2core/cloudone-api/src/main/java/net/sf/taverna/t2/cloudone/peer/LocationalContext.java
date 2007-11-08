package net.sf.taverna.t2.cloudone.peer;

/**
 * Defines locational context information mandated by a single type of reference
 * scheme.
 *
 * @author Tom Oinn
 * @author Matthew Pocock
 *
 */
public interface LocationalContext {

	/**
	 * Get the context type.
	 *
	 * @return The context type
	 */
	public String getContextType();

	/**
	 * Get a value associated with a key.
	 * <p>
	 * For a key specified as "some.key.thing", use
	 * <code>getValue("some", "key", "thing");</code>
	 *
	 * @param keyPath
	 *            One or more key paths
	 * @return The associated value
	 */
	public String getValue(String... keyPath);

}
