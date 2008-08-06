package net.sf.taverna.t2.workbench.iterationstrategy.contextview;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;

public class IterationStrategyContextualViewFactory implements
		ContextualViewFactory<ProcessorInputPort> {

	public boolean canHandle(Object selection) {
		return selection instanceof ProcessorInputPort;
	}

	public ContextualView getView(ProcessorInputPort procInpPort) {
		IterationStrategyStack iterationStrategy = procInpPort.getProcessor()
				.getIterationStrategy();
		return new IterationStrategyContextualView(iterationStrategy);
	}

}
