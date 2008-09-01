package net.sf.taverna.t2.workbench.ui.views.contextualviews.dataflowoutputport;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.impl.DataflowOutputPortImpl;

	public class DataflowOutputPortContextualViewFactory implements ContextualViewFactory<DataflowOutputPortImpl>{

		public boolean canHandle(Object object) {
			return object instanceof DataflowOutputPortImpl;
		}

		public ContextualView getView(DataflowOutputPortImpl outputport) {
			return new DataflowOutputPortContextualView(outputport);
		}

	}
