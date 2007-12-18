/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class ActivitySubsetTableModel extends DefaultTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3947839087479059276L;
	private List<ProcessorFactoryAdapter> rowObjects;
	
	/**
	 * @param node
	 */
	public ActivitySubsetTableModel(final PropertiedTreeNode<ProcessorFactoryAdapter> node) {
		super();
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		int rowCount = node.getAllObjects().size();
		Vector<String> columnNames = new Vector<String>();
		for (PropertiedTreeNode<ProcessorFactoryAdapter> aNode = node; aNode.getActualChildCount() > 0;
		aNode = aNode.getChild(0)) {
			if (aNode instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode<ProcessorFactoryAdapter> pvNode =
					(PropertiedTreePropertyValueNode<ProcessorFactoryAdapter>) aNode;
				columnNames.add(pvNode.getKey().toString());
			}
		}
		this.setColumnCount(columnNames.size());
		this.setColumnIdentifiers(columnNames);
		this.setRowCount(rowCount);
		this.rowObjects = new ArrayList<ProcessorFactoryAdapter>();
		fillInDetails(node, 0, 0);
	}

	private void fillInDetails(PropertiedTreeNode<ProcessorFactoryAdapter> node,
			int rowOffset, int column) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		int childCount = node.getActualChildCount();
		int row = rowOffset;
		for (int i = 0; i < childCount; i++) {
			PropertiedTreeNode<ProcessorFactoryAdapter> childNode = node.getChild(i);
			if (childNode instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode<ProcessorFactoryAdapter> childPropertyValueNode =
					(PropertiedTreePropertyValueNode<ProcessorFactoryAdapter>) childNode;
				int numberOfObjectsWithValue = childPropertyValueNode.getAllObjects().size();
				fillInDetails(childPropertyValueNode, row, column+1);
				for (int j = 0; j < numberOfObjectsWithValue; j++) {
					PropertyValue value = childPropertyValueNode.getValue();
					if (value != null) {
						this.setValueAt(value.toString(), row++, column);
					}
				}
			} else if (childNode instanceof PropertiedTreeObjectNode) {
				PropertiedTreeObjectNode<ProcessorFactoryAdapter> childObjectNode =
					(PropertiedTreeObjectNode<ProcessorFactoryAdapter>) childNode;
				this.rowObjects.add(childObjectNode.getObject());
			}
		}
	}
	
	/**
	 * @param row
	 * @return
	 */
	public ProcessorFactoryAdapter getRowObject(int row) {
		return this.rowObjects.get(row);
	}
	
	/**
	 * @param adapter
	 * @return
	 */
	public int getObjectIndex(ProcessorFactoryAdapter adapter) {
		if (adapter == null) {
			throw new NullPointerException("adapter cannot be null"); //$NON-NLS-1$
		}
		return this.rowObjects.indexOf(adapter);
	}
	
	/**
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
