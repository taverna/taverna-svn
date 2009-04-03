package net.sf.taverna.t2.provenance.connector.configview;

import net.sf.taverna.t2.provenance.connector.MySQLProvenanceConnector;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class MySQLConfigViewFactory implements ContextualViewFactory<MySQLProvenanceConnector>{

	public boolean canHandle(Object selection) {
		// TODO Auto-generated method stub
		return selection instanceof MySQLProvenanceConnector;
	}

	public ContextualView getView(MySQLProvenanceConnector selection) {
		// TODO Auto-generated method stub
		return new MySQLConfigView();
	}

}
