/**
 * 
 */
package net.sf.taverna.t2.workbench.iterationstrategy.contextview;

import java.util.List;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.iterationstrategy.editor.IterationStrategyTree;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

public class IterationStrategyContextualView extends ContextualView {
	private final IterationStrategyStack iterationStack;

	public IterationStrategyContextualView(IterationStrategyStack iterationStack) {
		if (iterationStack == null) {
			throw new NullPointerException(
					"Iteration strategy stack can't be null");
		}
		this.iterationStack = iterationStack;
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		// TODO: Support all stack layers
		List<? extends IterationStrategy> strategies = iterationStack
				.getStrategies();
		if (strategies.isEmpty()) {
			throw new IllegalStateException("Empty iteration stack");
		}
		IterationStrategy strategy = strategies.get(0);
		if (strategy instanceof IterationStrategyImpl) {
			IterationStrategyTree strategyTree = new IterationStrategyTree();
			strategyTree.setIterationStrategy((IterationStrategyImpl) strategy);
			return strategyTree;
		}
		throw new IllegalStateException(
				"Can't edit unknown iteration strategy implementation "
						+ strategy);
	}

	@Override
	protected String getViewTitle() {
		return "Iteration strategy";
	}

	@Override
	public void refreshView() {

	}
}