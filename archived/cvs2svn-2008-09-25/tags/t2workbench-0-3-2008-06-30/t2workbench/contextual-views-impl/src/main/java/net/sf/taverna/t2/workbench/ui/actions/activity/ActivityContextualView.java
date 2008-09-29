package net.sf.taverna.t2.workbench.ui.actions.activity;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A contextual view specific to an Activity.
 * <p>
 * Through the generic type the activity is associated with a given ConfigBean that is used internally
 * to define the activity. This is the bean that is used to configure the Activity itself.
 * </p>
 * <p>
 * The implementation provides a view based upon the properties set in the ConfigBean
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 *
 * @param <ConfigBean> - the ConfigBean that the Activity for this view is associated with
 * @see Activity
 * @see ContextualView
 */
public abstract class ActivityContextualView<ConfigBean> extends ContextualView {

	private Activity<?> activity;
	
	/**
	 * Constructs an instance of the view, and initialises the view itself.
	 * <p>
	 * The constructor parameter for the implementation of this class should define the specific Activity type itself.
	 * </p>
	 * @param activity
	 */
	public ActivityContextualView(Activity<?> activity) {
		super();
		this.activity = activity;
		initView();
	}

	protected Activity<?> getActivity() {
		return this.activity;
	}

	@SuppressWarnings("unchecked")
	protected ConfigBean getConfigBean() {
		return (ConfigBean)activity.getConfiguration();
	}
}
