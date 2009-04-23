package net.sf.taverna.t2.provenance.connector.configview;

import net.sf.taverna.t2.provenance.connector.DerbyProvenanceConnector;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class DerbyProvenanceConfigViewFactory implements ContextualViewFactory<DerbyProvenanceConnector>{

	public boolean canHandle(Object selection) {
		return selection instanceof DerbyProvenanceConnector;
	}

	public ContextualView getView(DerbyProvenanceConnector selection) {
		return new DerbyConfigView();
	}

}
