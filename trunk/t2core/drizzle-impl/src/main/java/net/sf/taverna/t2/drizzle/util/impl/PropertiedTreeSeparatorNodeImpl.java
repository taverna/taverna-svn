/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeSeparatorNode;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class PropertiedTreeSeparatorNodeImpl<O> extends PropertiedTreeNodeImpl<O> implements
		PropertiedTreeSeparatorNode<O> {

	@Override
	public int getChildCount() {
		return 0;
	}
	
	public TableModel getTableModel() {
		int rowCount = this.getAllObjects().size();
		int columnCount = -1; // -1 because of object node at leaves of tree
		for (PropertiedTreeNode aNode = this; aNode.getActualChildCount() > 0;
		aNode = aNode.getChild(0)) {
			columnCount++;
		}
		if (columnCount <= 1) {
			columnCount = 1;
		}
		DefaultTableModel result = new DefaultTableModel(rowCount, columnCount);
		fillInDetails(this, result, 0, 0);
		return result;
	}

	private void fillInDetails(PropertiedTreeNode node, DefaultTableModel tableModel,
			int rowOffset, int column) {
		int childCount = node.getActualChildCount();
		int row = rowOffset;
		for (int i = 0; i < childCount; i++) {
			PropertiedTreeNode childNode = node.getChild(i);
			if (childNode instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode childPropertyValueNode =
					(PropertiedTreePropertyValueNode) childNode;
				int numberOfObjectsWithValue = childPropertyValueNode.getAllObjects().size();
				fillInDetails(childPropertyValueNode, tableModel, row, column+1);
				for (int j = 0; j < numberOfObjectsWithValue; j++) {
					PropertyValue value = childPropertyValueNode.getValue();
					if (value != null) {
						tableModel.setValueAt(value.toString(), row++, column);
					}
				}
			}
		}
	}
	
}
