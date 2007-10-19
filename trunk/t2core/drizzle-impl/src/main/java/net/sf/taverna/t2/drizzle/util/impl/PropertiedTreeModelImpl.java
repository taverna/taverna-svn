/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.OperationNotSupportedException;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.utility.TypedTreeModelEvent;
import net.sf.taverna.t2.utility.TypedTreeModelListener;

/**
 * @author alanrw
 * 
 */
public final class PropertiedTreeModelImpl<O> implements PropertiedTreeModel<O> {

	private List<PropertyKeySetting> keySettings;

	private PropertiedObjectFilter<O> filter;

	private PropertiedTreeRootNode<O> root;

	private HashSet<TypedTreeModelListener<PropertiedTreeNode<O>>> listeners;

	private PropertiedGraphView<O> propertiedGraphView;

	private Comparator<O> objectComparator;

	private HashMap<O, PropertiedTreeObjectNode<O>> nodeMap;

	private PropertiedGraphViewListener<O> posl;

	/**
	 * 
	 */
	public PropertiedTreeModelImpl() {
		root = new PropertiedTreeRootNodeImpl<O>();
		listeners = new HashSet<TypedTreeModelListener<PropertiedTreeNode<O>>>();
		nodeMap = new HashMap<O, PropertiedTreeObjectNode<O>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#addTreeModelListener(net.sf.taverna.t2.utility.TypedTreeModelListener)
	 */
	public void addTreeModelListener(
			final TypedTreeModelListener<PropertiedTreeNode<O>> l) {
		if (l == null) {
			throw new NullPointerException("l cannot be null");
		}
		listeners.add(l);
	}

	private void constructSubTree(final PropertiedTreeNode<O> parent,
			final int settingIndex, final Set<O> permittedObjects) {
		if (settingIndex != keySettings.size()) {
			PropertyKeySetting setting = keySettings.get(settingIndex);
			PropertyKey key = setting.getPropertyKey();
			Set<PropertyValue> values = propertiedGraphView.getValues(key);

			// Note that a null compaarator is OK
			TreeSet<PropertyValue> sortedValues = new TreeSet<PropertyValue>(
					setting.getComparator());
			sortedValues.addAll(values);

			Set<O> remainingObjects = new HashSet<O>(permittedObjects);
			for (PropertyValue value : sortedValues) {
				PropertiedTreePropertyValueNode<O> subNode = new PropertiedTreePropertyValueNodeImpl<O>();
				subNode.setKey(key);
				subNode.setValue(value);
				Set<PropertiedGraphNode<O>> nodes = propertiedGraphView
						.getEdge(key, value).getNodes();
				Set<O> objectsWithValue = new HashSet<O>();
				for (PropertiedGraphNode<O> node : nodes) {
					O object = node.getObject();
					if (permittedObjects.contains(object)) {
						objectsWithValue.add(node.getObject());
					}
				}
				remainingObjects.removeAll(objectsWithValue);

				if (objectsWithValue.size() > 0) {
					parent.addChild(subNode);

					constructSubTree(subNode, settingIndex + 1,
							objectsWithValue);
				}
			}

			if (remainingObjects.size() > 0) {
				PropertiedTreePropertyValueNode<O> subNode = new PropertiedTreePropertyValueNodeImpl<O>();
				subNode.setKey(key);
				// value deliberately left as null
				parent.addChild(subNode);
				constructSubTree(subNode, settingIndex + 1, remainingObjects);
			}
		} else {
			// Note that a null comparator is OK
			TreeSet<O> sortedObjects = new TreeSet<O>(objectComparator);
			sortedObjects.addAll(permittedObjects);
			for (O object : sortedObjects) {
				PropertiedTreeObjectNode<O> subNode = new PropertiedTreeObjectNodeImpl<O>();
				subNode.setObject(object);
				parent.addChild(subNode);
				nodeMap.put(object, subNode);
			}
		}
	}

	private void generateTree() {
		if (root == null) {
			root = new PropertiedTreeRootNodeImpl<O> ();
		} else {
			root.removeAllChildren();
		}
		nodeMap = new HashMap<O, PropertiedTreeObjectNode<O>>();
		Set<O> filteredObjects = new HashSet<O>();
		Set<PropertiedGraphNode<O>> graphNodes = this.propertiedGraphView
				.getNodes();
		for (PropertiedGraphNode<O> graphNode : graphNodes) {
			if ((filter == null)
					|| (filter.acceptObject(graphNode.getObject()))) {
				filteredObjects.add(graphNode.getObject());
			}
		}
		constructSubTree(root, 0, filteredObjects);

		notifyListenersTreeStructureChanged(new TypedTreeModelEvent<PropertiedTreeNode<O>>(
				this, root.getPath()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#getChild(java.lang.Object,
	 *      int)
	 */
	public PropertiedTreeNode<O> getChild(final PropertiedTreeNode<O> parent,
			int index) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null");
		}
		return parent.getChild(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(final PropertiedTreeNode<O> parent) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null");
		}
		return parent.getChildCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#getIndexOfChild(java.lang.Object,
	 *      java.lang.Object)
	 */
	public int getIndexOfChild(PropertiedTreeNode<O> parent,
			PropertiedTreeNode<O> child) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null");
		}
		if (child == null) {
			throw new NullPointerException("child cannot be null");
		}
		return parent.getIndexOfChild(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedTreeModel#getPropertyKeySettings()
	 */
	public List<PropertyKeySetting> getPropertyKeySettings() {
		List<PropertyKeySetting> result;
		if (keySettings == null) {
			result = null;
		} else {
			result = new ArrayList<PropertyKeySetting>(keySettings);
		}
		// Copy just in case
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#getRoot()
	 */
	public PropertiedTreeNode<O> getRoot() {
		return this.root;
	}

	private void internalEdgeChanged(PropertiedGraphEdge<O> edge,
			PropertiedGraphNode<O> node) {
		O object = node.getObject();
		if (nodeMap.containsKey(object)) {
			PropertiedTreeObjectNode<O> treeNode = nodeMap.get(object);
			PropertyKey key = edge.getKey();
			PropertiedTreeNode<O> ancestor = treeNode.getAncestorWithKey(key);
			if (ancestor != null) {
				PropertiedTreeNode<O> containerNode = ancestor.getParent();
				Set<O> containedObjects = containerNode.getAllObjects();

				// If object is now filtered
				if ((filter != null) && !(filter.acceptObject(object))) {
					containedObjects.remove(object);
					nodeMap.remove(object);
				}
				containerNode.removeAllChildren();
				// rely on constructSubTree to overwrite the entries in the
				// nodeMap
				constructSubTree(containerNode, containerNode.getDepth(),
						containedObjects);
				notifyListenersTreeStructureChanged(new TypedTreeModelEvent<PropertiedTreeNode<O>>(
						this, containerNode.getPath()));
			}
		}
	}

	private void internalNodeAdded(PropertiedGraphNode<O> node) {
		O object = node.getObject();
		if ((filter == null) || (filter.acceptObject(object))) {
			generateTree();
		}

	}

	private void internalNodeRemoved(PropertiedGraphNode<O> node) {
		if (nodeMap.containsKey(node.getObject())) {
			generateTree();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(final PropertiedTreeNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		return (node.getChildCount() == 0);
	}

	private void notifyListenersTreeNodesChanged(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null");
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeNodesChanged(e);
		}
	}

	private void notifyListenersTreeNodesInserted(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null");
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeNodesInserted(e);
		}
	}

	private void notifyListenersTreeNodesRemoved(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null");
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeNodesRemoved(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void notifyListenersTreeStructureChanged(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null");
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeStructureChanged(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#removeTreeModelListener(net.sf.taverna.t2.utility.TypedTreeModelListener)
	 */
	public void removeTreeModelListener(
			final TypedTreeModelListener<PropertiedTreeNode<O>> l) {
		if (l == null) {
			throw new NullPointerException("l cannot be null");
		}
		listeners.remove(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedTreeModel#setFilter(net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter)
	 */
	public void setFilter(final PropertiedObjectFilter<O> filter) {
		if (this.filter != null) {
			throw new IllegalStateException(
					"filter cannot be initialized more than once");
		}
		// OK to be null
		this.filter = filter;
	}

	public void setObjectComparator(Comparator<O> objectComparator) {
		if (objectComparator == null) {
			throw new NullPointerException("objectComparator cannot be null");
		}
		if (this.objectComparator != null) {
			throw new IllegalStateException(
					"objectComparator cannot be initialized more than once");
		}
		this.objectComparator = objectComparator;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedTreeModel#setPropertiedGraphView(net.sf.taverna.t2.drizzle.util.PropertiedGraphView)
	 */
	public void setPropertiedGraphView(
			final PropertiedGraphView<O> propertiedGraphView) {
		if (propertiedGraphView == null) {
			throw new NullPointerException("propertiedGraphView cannot be null");
		}
		if (keySettings == null) {
			throw new IllegalStateException("keySettings must be set");
		}
		this.propertiedGraphView = propertiedGraphView;
		posl = new PropertiedGraphViewListener<O>() {

			public void edgeAdded(PropertiedGraphView<O> view,
					PropertiedGraphEdge<O> edge, PropertiedGraphNode<O> node) {
				internalEdgeChanged(edge, node);
			}

			public void edgeRemoved(PropertiedGraphView<O> view,
					PropertiedGraphEdge<O> edge, PropertiedGraphNode<O> node) {
				internalEdgeChanged(edge, node);
			}

			public void nodeAdded(PropertiedGraphView<O> view,
					PropertiedGraphNode<O> node) {
				internalNodeAdded(node);
			}

			public void nodeRemoved(PropertiedGraphView<O> view,
					PropertiedGraphNode<O> node) {
				internalNodeRemoved(node);
			}

		};
		this.propertiedGraphView.addListener(posl);

		generateTree();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedTreeModel#setPropertyKeySettings(java.util.List)
	 */
	public void setPropertyKeySettings(
			final List<PropertyKeySetting> settingList) {
		if (settingList == null) {
			throw new NullPointerException("settingList cannot be null");
		}
		if (this.keySettings != null) {
			throw new IllegalStateException(
					"keySettings can only be initialized once");
		}
		// Copy just in case
		keySettings = new ArrayList<PropertyKeySetting>(settingList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.utility.TypedTreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 *      java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		// Do nothing
	}

	public PropertiedObjectFilter<O> getFilter() {
		return this.filter;
	}

	public Comparator<O> getObjectComparator() {
		return this.objectComparator;
	}

	public void detachFromGraphView() {
		if (posl != null) {
			propertiedGraphView.removeListener(posl);
		}
	}

}
