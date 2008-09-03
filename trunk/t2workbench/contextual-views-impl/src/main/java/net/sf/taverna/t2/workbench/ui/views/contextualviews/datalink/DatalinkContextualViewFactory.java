package net.sf.taverna.t2.workbench.ui.views.contextualviews.datalink;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Datalink;

/**
 * A factory of contextual views for dataflow's datalinks.
 * 
 * @author Alex Nenadic
 *
 */
public class DatalinkContextualViewFactory implements
		ContextualViewFactory<Datalink> {

	public boolean canHandle(Object object) {
		return object instanceof Datalink;
	}

	public ContextualView getView(Datalink datalink) {
		return new DatalinkContextualView(datalink);
	}

}
