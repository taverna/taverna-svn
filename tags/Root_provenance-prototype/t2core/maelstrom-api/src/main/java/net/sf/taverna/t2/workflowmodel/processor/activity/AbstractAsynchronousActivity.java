package net.sf.taverna.t2.workflowmodel.processor.activity;

import java.util.Map;

import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Abstract superclass for asynchronous activities. Activity providers should only
 * have to implement the configuration and invocation methods to have a fully
 * functional activity - serialisation and deserialisation are handled
 * automatically.
 * 
 * @author Tom Oinn
 * 
 * @param <ConfigType>
 *            the configuration type used for this activity
 */
public abstract class AbstractAsynchronousActivity<ConfigType> extends
		AbstractActivity<ConfigType> implements AsynchronousActivity<ConfigType> {

	/**
	 * Called immediately after object construction by the deserialisation
	 * framework with a configuration bean built from the auto-generated XML.
	 * <p>
	 * This method is responsible for the creation of input and output ports,
	 * something that is currently done in the constructor of the Taverna 1
	 * Processor class.
	 */
	@Override
	public abstract void configure(ConfigType conf)
			throws ActivityConfigurationException;

	/**
	 * Get a configuration bean representing the definition of the activity. This
	 * bean should contain enough information to rebuild the input and output
	 * port sets, mappings are explicitly handled by the serialisation framework
	 * but the ports are assumed to be generated during the configuration stage
	 * rather than explicitly stored.
	 */
	@Override
	public abstract ConfigType getConfiguration();

	/**
	 * Request an asynchronous invocation of the activity on the specified data.
	 * The data items are named relative to the input port names of the activity
	 * (as opposed to the parent processor), the invocation layer is responsible
	 * for translating these appropriately before this method is called. The
	 * callback object provides access to a DataManager instance that can be
	 * used to resolve the entity identifiers in the data map, push results up
	 * and signal failure conditions.
	 * <p>
	 * This method must not block! However it happens this method must return
	 * immediately after creating the new activity invocation. Do not do any
	 * heavy lifting in the body of this method without creating a new thread
	 * specifically for it.
	 */
	public abstract void executeAsynch(Map<String, EntityIdentifier> data,
			AsynchronousActivityCallback callback);

}
