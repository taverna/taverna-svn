package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI registry for discovering ActivityViewFactories for a given object,
 * like an {@link net.sf.taverna.t2.workflowmodel.processor.activity.Activity}.
 * <p>
 * For {@link ContextualViewFactory factories} to be found, its full qualified
 * name needs to be defined a the resource file
 * <code>/META-INF/services/net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualViewFactory</code>
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 * @see ContextualViewFactory
 * 
 */
@SuppressWarnings("unchecked")
public class ContextualViewFactoryRegistry extends
		SPIRegistry<ContextualViewFactory> {

	/**
	 * Get the singleton instance of this registry
	 * 
	 * @return The ContextualViewFactoryRegistry singleton
	 */
	public static synchronized ContextualViewFactoryRegistry getInstance() {
		return Singleton.instance;
	}

	protected ContextualViewFactoryRegistry() {
		super(ContextualViewFactory.class);
	}

	/**
	 * Discover and return an ContextualViewFactory associated to the provided
	 * activity. This is accomplished by returning the first discovered
	 * {@link ContextualViewFactory#canHandle(Object)} that returns true for
	 * that Activity.
	 * 
	 * @param object
	 * @return
	 * 
	 * @see ContextualViewFactory#canHandle(Object)
	 */
	public ContextualViewFactory<?> getViewFactoryForObject(Object object) {
		for (ContextualViewFactory<?> factory : getInstances()) {
			if (factory.canHandle(object)) {
				return factory;
			}
		}
		throw new IllegalArgumentException(
				"Can't find factory for activity view class " + object);
	}

	private static class Singleton {
		private static final ContextualViewFactoryRegistry instance = new ContextualViewFactoryRegistry();
	}

}
