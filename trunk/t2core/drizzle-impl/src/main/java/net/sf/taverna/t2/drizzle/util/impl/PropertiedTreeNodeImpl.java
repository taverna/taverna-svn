/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 * 
 * @param <O> The class of object that is encapsulated by leaf object nodes.
 */
public abstract class PropertiedTreeNodeImpl<O> implements
		PropertiedTreeNode<O> {

	private List<PropertiedTreeNode<O>> children;

	private PropertiedTreeNode<O> parent;

	/**
	 * 
	 */
	public PropertiedTreeNodeImpl() {
		this.children = new ArrayList<PropertiedTreeNode<O>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addChild(PropertiedTreeNode<O> child) {
		if (child == null) {
			throw new NullPointerException("child cannot be null"); //$NON-NLS-1$
		}
		if (child.getParent() != null) {
			throw new IllegalArgumentException ("child already owned"); //$NON-NLS-1$
		}
		this.children.add(child);
		if (child instanceof PropertiedTreeNodeImpl) {
			((PropertiedTreeNodeImpl<O>) child).setParent(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<O> getAllObjects() {
		Set<O> result = new HashSet<O> ();
		for (PropertiedTreeNode<O> child : this.children) {
			result.addAll (child.getAllObjects());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedTreePropertyValueNode<O> getAncestorWithKey(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException ("key cannot be null"); //$NON-NLS-1$
		}
		PropertiedTreePropertyValueNode<O> result = null;
		for (PropertiedTreeNode<O> ancestor = this.getParent();
			(ancestor != null) && (result == null);
			ancestor = ancestor.getParent()) {
			if (ancestor instanceof PropertiedTreePropertyValueNode) {
				PropertiedTreePropertyValueNode<O> propertyParent =
					(PropertiedTreePropertyValueNode<O>) ancestor;
				PropertyKey k = propertyParent.getKey();
				if (k.equals(key)) {
					result = propertyParent;
				}
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final PropertiedTreeNode<O> getChild(final int index) {
		PropertiedTreeNode<O> result = null;
		try {
			result = this.children.get(index);
		} catch (IndexOutOfBoundsException e) {
			// reset result
			result = null;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getChildCount() {
		return this.children.size();
	}
	
	public int getActualChildCount() {
		return this.children.size();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getDepth() {
		int result = 0;
		for (PropertiedTreeNode<O> ancestor = this.getParent();
			ancestor != null; ancestor = ancestor.getParent(), result++) {
			// nothing in loop
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getIndexOfChild(final PropertiedTreeNode<O> child) {
		if (child == null) {
			throw new NullPointerException("child cannot be null"); //$NON-NLS-1$
		}
		return this.children.indexOf(child);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedTreeNode<O> getParent() {
		return this.parent;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public PropertiedTreeNode<O>[] getPath() {
		return getPathList().toArray(new PropertiedTreeNode[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PropertiedTreeNode<O>> getPathList() {
		List<PropertiedTreeNode<O>> result;
		if (getParent() != null) {
			List<PropertiedTreeNode<O>> parentList =
				getParent().getPathList();
			parentList.add(this);
			result = parentList;
		}
		else {
			result = new ArrayList<PropertiedTreeNode<O>> ();
			result.add(this);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAllChildren() {
		for (PropertiedTreeNode<O> child : this.children) {
			if (child instanceof PropertiedTreeNodeImpl) {
				((PropertiedTreeNodeImpl<O>) child).setParent(null);
			}
		}
		this.children = new ArrayList<PropertiedTreeNode<O>> ();
	}

	/**
	 * @param parent
	 *            the parent to set - can be null
	 */
	protected final void setParent(PropertiedTreeNode<O> parent) {
		this.parent = parent;
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