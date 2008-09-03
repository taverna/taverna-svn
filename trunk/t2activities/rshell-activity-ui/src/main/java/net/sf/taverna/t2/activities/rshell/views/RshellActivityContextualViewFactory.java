package net.sf.taverna.t2.activities.rshell.views;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

/**
 * RshellActivity contextual view factory.
 * 
 * @author Alex Nenadic
 *
 */
public class RshellActivityContextualViewFactory implements ContextualViewFactory<RshellActivity>{

	public boolean canHandle(Object object) {
		return object instanceof RshellActivity;
	}

	 
	public RshellActivityContextualView getView(RshellActivity activity) {
		RshellActivityContextualView view = new RshellActivityContextualView(activity);
		return view;
	}

}
