package net.sf.taverna.t2.provenance.connector.configview;

import net.sf.taverna.t2.provenance.connector.MySQLProvenanceConnector;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class MySQLConfigViewFactory implements ContextualViewFactory<MySQLProvenanceConnector>{

	public boolean canHandle(Object selection) {
		return selection instanceof MySQLProvenanceConnector;
	}

	public ContextualView getView(MySQLProvenanceConnector selection) {
		return new MySQLConfigView();
	}

}
