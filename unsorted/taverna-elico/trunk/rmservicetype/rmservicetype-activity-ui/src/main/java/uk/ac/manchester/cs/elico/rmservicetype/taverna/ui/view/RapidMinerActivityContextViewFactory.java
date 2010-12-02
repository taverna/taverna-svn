package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivity;

public class RapidMinerActivityContextViewFactory implements
		ContextualViewFactory<RapidMinerActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof RapidMinerActivity;
	}

	public List<ContextualView> getViews(RapidMinerActivity selection) {
		return Arrays.<ContextualView>asList(new RapidMinerContextualView(selection));
	}
	
}
