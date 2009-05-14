package net.sf.taverna.t2.reference;

/**
 * Factory for T2ReferenceGenerator instances
 * 
 * @author Tom Oinn
 * 
 */
public interface T2ReferenceGeneratorFactory {

	/**
	 * Construct a new reference generator which will use the supplied namespace
	 * for all new references
	 * 
	 * @param namespace
	 *            a namespace to use, must be alphanumeric
	 * @return a configured T2ReferenceGenerator using the supplied namespace
	 */
	public T2ReferenceGenerator getGeneratorWithNamespace(String namespace);

}
