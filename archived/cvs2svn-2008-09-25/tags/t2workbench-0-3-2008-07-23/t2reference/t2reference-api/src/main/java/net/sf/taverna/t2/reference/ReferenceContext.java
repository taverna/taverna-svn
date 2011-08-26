package net.sf.taverna.t2.reference;

import java.util.List;

/**
 * Many operations over the reference manager require access to an appropriate
 * context. The context contains hooks out to platform level facilities such as
 * the security agent framework (when used in conjunction with the enactor).
 * <p>
 * This interface is also used to pass in resources required by the external
 * reference translation and construction SPIs. An example might be a translator
 * from File to URL could work by copying the source file to a web share of some
 * kind, but obviously this can't happen unless properties such as the location
 * of the web share folder are known. These properties tend to be properties of
 * the installation rather than of the code, referring as they do to resources
 * on the machine hosting the reference manager (and elsewhere).
 * <p>
 * Where entities in the context represent properties of the platform rather
 * than the 'session' they are likely to be configured in a central location
 * such as a Spring context definition, this interface is neutral to those
 * concerns.
 * 
 * @author Tom Oinn
 * 
 */
public interface ReferenceContext {

	/**
	 * Return a list of all entities in the resolution context which match the
	 * supplied entity type argument.
	 * 
	 * @param <T>
	 *            The generic type of the returned entity list. In general the
	 *            compiler is smart enough that you don't need to specify this,
	 *            it can pick it up from the entityType parameter.
	 * @param entityType
	 *            Class of entity to return. Use Object.class to return all
	 *            entities within the resolution context
	 * @return a list of entities from the resolution context which can be cast
	 *         to the specified type.
	 */
	public <T extends Object> List<? extends T> getEntities(Class<T> entityType);

}
