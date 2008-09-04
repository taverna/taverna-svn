package net.sf.taverna.t2.workbench.ui.views.contextualviews.merge;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.Merge;

/**
 * A factory of contextual views for dataflow's merges.
 * 
 * @author Alex Nenadic
 *
 */
public class MergeContextualViewFactory implements
		ContextualViewFactory<Merge> {

	public boolean canHandle(Object object) {
		return object instanceof Merge;
	}

	public ContextualView getView(Merge merge) {
		return new MergeContextualView(merge);
	}

}
