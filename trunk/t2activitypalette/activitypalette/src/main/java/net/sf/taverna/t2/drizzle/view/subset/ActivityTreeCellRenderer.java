/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

public class ActivityTreeCellRenderer implements TreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8795507848377363512L;

	private DefaultListModel listModel;

	private DefaultTreeCellRenderer defaultRenderer;

	private JLabel resultLabel;

	private JTable resultTable;
	
	private PropertiedObjectSet<ProcessorFactoryAdapter> registry;
	
	Color selectedColor;
	Color nonSelectedColor;

	ActivityTreeCellRenderer(DefaultListModel listModel, PropertiedObjectSet<ProcessorFactoryAdapter> registry) {
		this.listModel = listModel;
		this.registry = registry;
		this.defaultRenderer = new DefaultTreeCellRenderer();
		this.resultLabel = new JLabel(""); //$NON-NLS-1$
		this.resultLabel.setOpaque(true);
		this.resultTable = new JTable();
		this.resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.selectedColor = this.defaultRenderer.getBackgroundSelectionColor();
		this.nonSelectedColor = this.defaultRenderer.getBackgroundNonSelectionColor();
	}

	@SuppressWarnings("unchecked")
	public Component getTreeCellRendererComponent(JTree tree,
			final Object value, @SuppressWarnings("hiding")
			boolean selected, boolean expanded, boolean leaf, int row,
			@SuppressWarnings("hiding")
			boolean hasFocus) {
		if (tree == null) {
			throw new NullPointerException("tree cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		if (value instanceof PropertiedTreeObjectNode) {
			PropertiedTreeObjectNode<ProcessorFactoryAdapter> objectNode = (PropertiedTreeObjectNode<ProcessorFactoryAdapter>) value;
			ProcessorFactoryAdapter adapter = objectNode.getObject();
			if (this.listModel.isEmpty()) {
				ProcessorFactory pf = adapter.getTheFactory();
				this.resultLabel.setText(pf.getName());
				Class<?> processorClass = pf.getProcessorClass();
				String tagName = ProcessorHelper
						.getTagNameForClassName(processorClass.getName());
				ImageIcon icon = ProcessorHelper.getIconForTagName(tagName);
				if (icon != null) {
					this.resultLabel.setIcon(icon);
				}
				if (selected) {
					this.resultLabel.setBackground(this.selectedColor);
				} else {
					this.resultLabel.setBackground(this.nonSelectedColor);
				}
				return this.resultLabel;
			}
			DefaultTableModel tableModel = new DefaultTableModel(1,
					this.listModel.getSize());
			int column = 0;
			for (Enumeration e = this.listModel.elements(); e.hasMoreElements();) {
				PropertyKey pk = (PropertyKey) e.nextElement();
				PropertyValue pv = this.registry.getPropertyValue(adapter, pk);
				if (pv != null) {
					tableModel.setValueAt(pv.toString(), 0, column++);
				}
			}
			this.resultTable.setModel(tableModel);
			TableColumnModel colModel = this.resultTable.getColumnModel();
			for (int i = 0; i < this.resultTable.getColumnCount(); i++) {
			    TableColumn col = colModel.getColumn(i);
			    col.setPreferredWidth(256);
			}
			if (selected) {
				this.resultTable.setBackground(this.selectedColor);
			} else {
				this.resultTable.setBackground(this.nonSelectedColor);
			}
			this.resultTable.doLayout();
			this.resultTable.revalidate();
			return this.resultTable;
		}
		return this.defaultRenderer.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
	}

}