package net.sf.taverna.t2.activities.biomart.views;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class BiomartActivityViewFactory implements ContextualViewFactory<BiomartActivity> {

	
	public boolean canHandle(Object object) {
		return object instanceof BiomartActivity;
	}

	
	public ActivityContextualView<?> getView(BiomartActivity activity) {
		return new BiomartActivityContextualView(activity);
	}

}
