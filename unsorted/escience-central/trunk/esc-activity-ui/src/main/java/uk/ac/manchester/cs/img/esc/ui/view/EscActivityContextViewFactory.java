package uk.ac.manchester.cs.img.esc.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import uk.ac.manchester.cs.img.esc.EscActivity;

public class EscActivityContextViewFactory implements
		ContextualViewFactory<EscActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof EscActivity;
	}

	public List<ContextualView> getViews(EscActivity selection) {
		return Arrays.<ContextualView>asList(new EscContextualView(selection));
	}
	
}
