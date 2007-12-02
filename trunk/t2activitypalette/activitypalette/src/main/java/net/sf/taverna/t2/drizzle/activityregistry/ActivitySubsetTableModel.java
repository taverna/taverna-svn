/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public class ActivitySubsetTableModel extends DefaultTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3947839087479059276L;
	private List<ProcessorFactory> rowObjects;
	
	public ActivitySubsetTableModel(final PropertiedTreeNode<ProcessorFactory> node) {
		super();
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
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
		this.rowObjects = new ArrayList<ProcessorFactory>();
		fillInDetails(node, 0, 0);
	}

	private void fillInDetails(PropertiedTreeNode<ProcessorFactory> node,
			int rowOffset, int column) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
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
				this.rowObjects.add(childObjectNode.getObject());
			}
		}
	}
	
	public ProcessorFactory getRowObject(int row) {
		return this.rowObjects.get(row);
	}
	
	public int getObjectIndex(ProcessorFactory pf) {
		if (pf == null) {
			throw new NullPointerException("pf cannot be null"); //$NON-NLS-1$
		}
		return this.rowObjects.indexOf(pf);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
