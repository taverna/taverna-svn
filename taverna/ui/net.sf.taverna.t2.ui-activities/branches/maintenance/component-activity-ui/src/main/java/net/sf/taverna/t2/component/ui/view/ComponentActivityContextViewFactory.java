package net.sf.taverna.t2.component.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import net.sf.taverna.t2.component.ComponentActivity;

public class ComponentActivityContextViewFactory implements
		ContextualViewFactory<ComponentActivity> {

	@Override
	public boolean canHandle(Object selection) {
		return selection instanceof ComponentActivity;
	}

	@Override
	public List<ContextualView> getViews(ComponentActivity selection) {
		return Arrays.<ContextualView>asList(new ComponentActivityContextualView(selection));
	}

}
