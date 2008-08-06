package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

public class IterationStrategyTree extends JTree implements UIComponentSPI {

	private IterationStrategy strategy = null;

	public IterationStrategyTree() {
		super();
		setCellRenderer(new IterationStrategyCellRenderer());
	}

	public ImageIcon getIcon() {
		return IterationStrategyIcons.leafnodeicon;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		this.strategy = null;
		setModel(null);
	}

	public synchronized void setIterationStrategy(
			IterationStrategyImpl theStrategy) {
		if (theStrategy != this.strategy) {
			this.strategy = theStrategy;
			TreeNode terminal = theStrategy.getTerminal();
			setModel(new DefaultTreeModel(terminal));
			revalidate();
		}
	}

	@Override
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}

	@Override
	public void setModel(TreeModel newModel) {
		if (newModel != null && !(newModel instanceof DefaultTreeModel)) {
			throw new IllegalArgumentException(
					"Model must be a DefaultTreeModel");
		}
		super.setModel(newModel);
	}

}