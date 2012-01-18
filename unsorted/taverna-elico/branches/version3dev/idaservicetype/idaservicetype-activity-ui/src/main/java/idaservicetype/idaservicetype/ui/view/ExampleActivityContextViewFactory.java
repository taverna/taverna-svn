package idaservicetype.idaservicetype.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import idaservicetype.idaservicetype.IDAActivity;

public class ExampleActivityContextViewFactory implements
		ContextualViewFactory<IDAActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof IDAActivity;
	}

	public List<ContextualView> getViews(IDAActivity selection) {
		return Arrays.<ContextualView>asList(new ExampleContextualView(selection));
	}
	
}
