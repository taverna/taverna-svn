package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * An SPI registry for discovering ActivityViewFactories for a given Activity Type
 * <p>
 * For ActivityViewFactory to be found, its full qualified name needs to be defined in the resource file: META-INF/services/net.sf.taverna.t2.workbench.ui.views.contextualviews.ActivityViewFactory
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 * @see ActivityViewFactory
 *
 */
@SuppressWarnings("unchecked")
public class ActivityViewFactoryRegistry extends
		SPIRegistry<ActivityViewFactory> {

	private static ActivityViewFactoryRegistry instance;

	/**
	 * @return a singleton instance of the registry
	 */
	public static synchronized ActivityViewFactoryRegistry getInstance() {
		if (instance == null) {
			instance = new ActivityViewFactoryRegistry();
		}
		return instance;
	}
	
	protected ActivityViewFactoryRegistry() {
		super(ActivityViewFactory.class);
	}

	/**
	 * Discovers and returns an ActivityViewFactory associated to the provided activity.
	 * This is accomplished by returning the first discovered {@link ActivityViewFactory#canHandle(Activity)} that returns true for that Activity.
	 * 
	 * @param activity
	 * @return
	 * 
	 * @see ActivityViewFactory#canHandle(Activity)
	 */
	public ActivityViewFactory<?> getViewFactoryForBeanType(Activity<?> activity) {
		for (ActivityViewFactory<?> factory : getInstances()) {
			if (factory.canHandle(activity)) {
				return factory;
			}
		}
		throw new IllegalArgumentException(
				"Can't find factory for activity view class " + activity);
	}

}
