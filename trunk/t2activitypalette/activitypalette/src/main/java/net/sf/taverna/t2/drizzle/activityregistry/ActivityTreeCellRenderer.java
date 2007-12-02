/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

public class ActivityTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8795507848377363512L;

	ActivityTreeCellRenderer() {
		// nothing to do
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getTreeCellRendererComponent(JTree tree,
			final Object value, @SuppressWarnings("hiding")
			boolean selected, boolean expanded,
			boolean leaf, int row, @SuppressWarnings("hiding")
			boolean hasFocus) {
		if (tree == null) {
			throw new NullPointerException("tree cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof PropertiedTreeObjectNode) {
			PropertiedTreeObjectNode<ProcessorFactory> objectNode = (PropertiedTreeObjectNode<ProcessorFactory>) value;
			ProcessorFactory pf = objectNode.getObject();
			Class<?> processorClass = pf.getProcessorClass();
			String tagName = ProcessorHelper
					.getTagNameForClassName(processorClass.getName());
			ImageIcon icon = ProcessorHelper.getIconForTagName(tagName);
			if (icon != null) {
				this.setIcon(icon);
			}
		}
		return this;
	}

}