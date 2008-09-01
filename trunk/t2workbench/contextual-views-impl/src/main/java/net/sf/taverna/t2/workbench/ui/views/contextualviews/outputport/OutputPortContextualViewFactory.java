package net.sf.taverna.t2.workbench.ui.views.contextualviews.outputport;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.impl.ActivityOutputPortImpl;

public class OutputPortContextualViewFactory implements ContextualViewFactory<ActivityOutputPortImpl>{

	public boolean canHandle(Object object) {
		return object instanceof ActivityOutputPortImpl;
	}

	public ContextualView getView(ActivityOutputPortImpl outputport) {
		return new OutputPortContextualView(outputport);
	}

}
