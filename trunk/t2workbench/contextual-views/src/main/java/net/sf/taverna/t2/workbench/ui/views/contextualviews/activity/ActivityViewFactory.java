package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A factory class that when associated with a given Activity creates a {@link ActivityContextualView} for Activity
 * <p>
 * This factory acts as an SPI to find a ActivityContextualView for a given Activity.
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 *
 * @param <ActivityType> - the activity type this factory is associated with
 * 
 * @see ActivityContextualView
 * @see ContextualView
 * @see ActivityViewFactoryRegistry
 */
public abstract class ActivityViewFactory<ActivityType> {
	
	/**
	 * @param activity - the Activity for which a ContextualView needs to be generated
	 * @return an instance of an ActivityContext|ualView
	 */
	public abstract ActivityContextualView<?> getView(ActivityType activity);

	/**
	 * Used by the SPI system to find the correct factory that can handle the given Activity type.
	 * 
	 * @param activity
	 * @return true if this factory relates to the given Activity type
	 * @see ActivityViewFactoryRegistry
	 */
	public abstract boolean canHandle(Activity<?> activity);

}