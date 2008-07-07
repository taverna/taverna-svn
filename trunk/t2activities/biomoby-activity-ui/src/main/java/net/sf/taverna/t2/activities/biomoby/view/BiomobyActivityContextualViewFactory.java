package net.sf.taverna.t2.activities.biomoby.view;

import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class BiomobyActivityContextualViewFactory implements ActivityViewFactory<BiomobyActivity>{

	public boolean canHandle(Activity<?> activity) {
		return activity instanceof BiomobyActivity;
	}

	public ContextualView getView(BiomobyActivity activity) {
		return new BiomobyActivityContextualView(activity);
	}
	
	

}
