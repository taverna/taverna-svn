package net.sf.taverna.t2.activities.interaction.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.activities.interaction.InteractionActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;


public class InteractionActivityContextViewFactory implements
		ContextualViewFactory<InteractionActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof InteractionActivity;
	}

	public List<ContextualView> getViews(InteractionActivity selection) {
		return Arrays.<ContextualView>asList(new InteractionActivityContextualView(selection));
	}
	
}
