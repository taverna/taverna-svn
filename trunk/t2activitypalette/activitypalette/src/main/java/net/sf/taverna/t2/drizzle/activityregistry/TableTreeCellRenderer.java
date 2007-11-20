/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeSeparatorNode;

public class TableTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8795507848377363512L;

	private TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	TableTreeCellRenderer() {
	}

	public Component getTreeCellRendererComponent(JTree tree,
			final Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Component result = null;
		if (value instanceof PropertiedTreeSeparatorNode) {
			JTable table = new JTable(((PropertiedTreeSeparatorNode) value)
					.getTableModel());
			result = table;
		} else {
			result = this.defaultRenderer.getTreeCellRendererComponent(
					tree, value, selected, expanded, leaf, row, hasFocus);
		}
		return result;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(50,50);
	}
}