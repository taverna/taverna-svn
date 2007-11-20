/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeSeparatorNode;

public class TableTreeCellRenderer extends JPanel implements
		TreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8795507848377363512L;

	private JTable table;

	private TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

	TableTreeCellRenderer() {
		this.setLayout(new BorderLayout());
		this.table = new JTable();
		this.add(this.table, BorderLayout.CENTER);
	}

	public Component getTreeCellRendererComponent(JTree tree,
			final Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Component result = null;
		if (value instanceof PropertiedTreeSeparatorNode) {
			this.table.setModel(((PropertiedTreeSeparatorNode) value)
					.getTableModel());

			this.table.doLayout();
			result = this;
		} else {
			result = this.defaultRenderer.getTreeCellRendererComponent(
					tree, value, selected, expanded, leaf, row, hasFocus);
		}
		return result;
	}
}