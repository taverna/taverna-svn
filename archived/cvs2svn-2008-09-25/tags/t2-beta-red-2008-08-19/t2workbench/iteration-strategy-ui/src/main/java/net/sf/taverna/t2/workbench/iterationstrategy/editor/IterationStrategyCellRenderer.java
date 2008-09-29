/**
 * 
 */
package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.workbench.iterationstrategy.IterationStrategyIcons;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

import org.apache.log4j.Logger;

final class IterationStrategyCellRenderer extends DefaultTreeCellRenderer {

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(IterationStrategyCellRenderer.class);

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, hasFocus);
		if (value instanceof CrossProduct) {
			setIcon(IterationStrategyIcons.joinIteratorIcon);
			setText("Cross product");
		} else if (value instanceof DotProduct) {
			setIcon(IterationStrategyIcons.lockStepIteratorIcon);
			setText("Dot product");
		} else if (value instanceof NamedInputPortNode) {
			setIcon(IterationStrategyIcons.leafnodeicon);
			NamedInputPortNode namedInput = (NamedInputPortNode) value;
			setText(namedInput.getPortName());
		} else {
			setText("Iteration strategy");
		}
		return this;
	}
}