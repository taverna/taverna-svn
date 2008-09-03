package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflowinputport;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.impl.DataflowInputPortImpl;

/**
 * A factory of contextual views for dataflow's input ports.
 * 
 * @author Alex Nenadic
 *
 */
public class DataflowInputPortContextualViewFactory implements
		ContextualViewFactory<DataflowInputPortImpl> {

	public boolean canHandle(Object object) {
		return object instanceof DataflowInputPortImpl;
	}

	public ContextualView getView(DataflowInputPortImpl inputport) {
		return new DataflowInputPortContextualView(inputport);
	}

}
