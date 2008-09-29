package net.sf.taverna.t2.workflowmodel.processor.dispatch;

/**
 * Used by dispatch layers which can contribute property information to their
 * parent processor instance. This is distinct from dispatch layers which modify
 * the process identifier and therefore create their own nodes in the monitor
 * tree, although there's no reason a layer can't perform both functions. For
 * example, the fault tolerance layers create their own state subtrees for each
 * failure recovery but could also contribute aggregate fault information to the
 * parent processor's property set.
 * 
 * @author Tom Oinn
 * 
 * @param <ConfigType>
 *            configuration type for the dispatch layer
 */
public interface PropertyContributingDispatchLayer<ConfigType> extends
		DispatchLayer<ConfigType> {

	/**
	 * Inject properties for the specified owning process into the parent
	 * dispatch stack. At some point prior to this call being made the
	 * setDispatchStack will have been called, implementations of this method
	 * need to use this DispatchStack reference to push properties in with the
	 * specified key.
	 * <p>
	 * Threading - this thread must not fork, do all the work in this method in
	 * the thread you're given by the caller. This is because implementations
	 * may assume that they can collect properties from the dispatch stack
	 * implementation (which will expose them through a private access method to
	 * prevent arbitrary access to layer properties) once this call has
	 * returned.
	 * <p>
	 * There is no guarantee that the layer will have seen an event with the
	 * specified process, and in fact it's unlikely to in the general case as
	 * any layers above it are free to modify the process identifier of tokens
	 * as they go. Remember that this method is for aggregating properties into
	 * the top level (processor) view so you may need to implement the property
	 * getters such that they check prefixes of identifiers rather than
	 * equality.
	 * 
	 * @param owningProcess
	 */
	void injectPropertiesFor(String owningProcess);

}
