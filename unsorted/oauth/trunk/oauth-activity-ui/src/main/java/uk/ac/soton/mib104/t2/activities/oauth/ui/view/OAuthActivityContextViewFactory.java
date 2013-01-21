package uk.ac.soton.mib104.t2.activities.oauth.ui.view;

import java.util.Arrays;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import uk.ac.soton.mib104.t2.activities.oauth.OAuthActivity;

public class OAuthActivityContextViewFactory implements ContextualViewFactory<OAuthActivity> {

	@Override
	public boolean canHandle(final Object obj) {
		return (obj != null) && (obj instanceof OAuthActivity);
	}

	@Override
	public List<ContextualView> getViews(final OAuthActivity selection) {
		return Arrays.<ContextualView>asList(new OAuthContextualView(selection));
	}
	
}
