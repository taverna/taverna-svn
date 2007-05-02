package net.sf.taverna.t2.workflowmodel.processor.service;

import java.util.Map;

import net.sf.taverna.t2.cloudone.EntityIdentifier;

/**
 * Abstract superclass for asynchronous services. Service providers should only
 * have to implement the configuration and invocation methods to have a fully
 * functional service - serialization and deserialization are handled
 * automatically.
 * 
 * @author Tom Oinn
 * 
 * @param <ConfigType>
 *            the configuration type used for this service
 */
public abstract class AbstractAsynchronousService<ConfigType> extends
		AbstractService<ConfigType> implements AsynchronousService<ConfigType> {

	/**
	 * Called immediately after object construction by the deserialization
	 * framework with a configuration bean built from the auto-generated XML.
	 * <p>
	 * This method is responsible for the creation of input and output ports,
	 * something that is currently done in the constructor of the Taverna 1
	 * Processor class.
	 */
	@Override
	public abstract void configure(ConfigType conf)
			throws ServiceConfigurationException;

	/**
	 * Get a configuration bean representing the definition of the service. This
	 * bean should contain enough information to rebuild the input and output
	 * port sets, mappings are explicitly handled by the serialization framework
	 * but the ports are assumed to be generated during the configuration stage
	 * rather than explicitly stored.
	 */
	@Override
	public abstract ConfigType getConfiguration();

	/**
	 * Request an asynchronous invocation of the service on the specified data.
	 * The data items are named relative to the input port names of the service
	 * (as opposed to the parent processor), the invocation layer is responsible
	 * for translating these appropriately before this method is called. The
	 * callback object provides access to a DataManager instance that can be
	 * used to resolve the entity identifiers in the data map, push results up
	 * and signal failure conditions.
	 * <p>
	 * This method must not block! However it happens this method must return
	 * immediately after creating the new service invocation. Do not do any
	 * heavy lifting in the body of this method without creating a new thread
	 * specifically for it.
	 */
	public abstract void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousServiceCallback callback);

}
