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
	}

	public Component getTreeCellRendererComponent(JTree tree,
			final Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof PropertiedTreeObjectNode) {
			PropertiedTreeObjectNode<ProcessorFactory> objectNode = (PropertiedTreeObjectNode) value;
			ProcessorFactory pf = objectNode.getObject();
			Class processorClass = pf.getProcessorClass();
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