/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class ActivitySubsetTableModel extends DefaultTableModel {
	
	private List<ProcessorFactory> rowObjects;
	
	public ActivitySubsetTableModel(final PropertiedTreeNode<ProcessorFactory> node) {
		super();
		int rowCount = node.getAllObjects().size();
		Vector<String> columnNames = new Vector<String>();
		for (PropertiedTreeNode<ProcessorFactory> aNode = node; aNode.getActualChildCount() > 0;
		aNode = aNode.getChild(0)) {
			if (aNode instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode<ProcessorFactory> pvNode =
					(PropertiedTreePropertyValueNode<ProcessorFactory>) aNode;
				columnNames.add(pvNode.getKey().toString());
			}
		}
		this.setColumnCount(columnNames.size());
		this.setColumnIdentifiers(columnNames);
		this.setRowCount(rowCount);
		rowObjects = new ArrayList<ProcessorFactory>();
		fillInDetails(node, 0, 0);
	}

	private void fillInDetails(PropertiedTreeNode<ProcessorFactory> node,
			int rowOffset, int column) {
		int childCount = node.getActualChildCount();
		int row = rowOffset;
		for (int i = 0; i < childCount; i++) {
			PropertiedTreeNode<ProcessorFactory> childNode = node.getChild(i);
			if (childNode instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode<ProcessorFactory> childPropertyValueNode =
					(PropertiedTreePropertyValueNode<ProcessorFactory>) childNode;
				int numberOfObjectsWithValue = childPropertyValueNode.getAllObjects().size();
				fillInDetails(childPropertyValueNode, row, column+1);
				for (int j = 0; j < numberOfObjectsWithValue; j++) {
					PropertyValue value = childPropertyValueNode.getValue();
					if (value != null) {
						this.setValueAt(value.toString(), row++, column);
					}
				}
			} else if (childNode instanceof PropertiedTreeObjectNode) {
				PropertiedTreeObjectNode<ProcessorFactory> childObjectNode =
					(PropertiedTreeObjectNode<ProcessorFactory>) childNode;
				rowObjects.add(childObjectNode.getObject());
			}
		}
	}
	
	public ProcessorFactory getRowObject(int row) {
		return rowObjects.get(row);
	}
	
	public int getObjectIndex(ProcessorFactory pf) {
		return rowObjects.indexOf(pf);
	}
	
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
