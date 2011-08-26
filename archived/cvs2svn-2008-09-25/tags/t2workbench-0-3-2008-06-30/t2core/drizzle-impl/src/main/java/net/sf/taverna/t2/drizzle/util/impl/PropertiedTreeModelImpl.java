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

import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeModel;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyKeySetting;
import net.sf.taverna.t2.drizzle.util.PropertyValue;
import net.sf.taverna.t2.drizzle.util.TwigConstructor;
import net.sf.taverna.t2.drizzle.util.TwigConstructorRegistry;
import net.sf.taverna.t2.util.beanable.Beanable;
import net.sf.taverna.t2.utility.TypedTreeModelEvent;
import net.sf.taverna.t2.utility.TypedTreeModelListener;

/**
 * @author alanrw
 * 
 * @param <O>
 *            The class of object within the PropertiedObjectSet of which this
 *            is a tree model.
 */
public final class PropertiedTreeModelImpl<O extends Beanable<?>> implements PropertiedTreeModel<O> {

	private List<PropertyKeySetting> keySettings;

	private PropertiedObjectFilter<O> filter;

	private PropertiedTreeRootNode<O> root;

	private HashSet<TypedTreeModelListener<PropertiedTreeNode<O>>> listeners;

	private PropertiedGraphView<O> propertiedGraphView;

	private Comparator<O> objectComparator;

	private HashMap<O, PropertiedTreeNode<O>> nodeMap;

	private PropertiedGraphViewListener<O> posl;

	/**
	 * 
	 */
	public PropertiedTreeModelImpl() {
		this.root = new PropertiedTreeRootNodeImpl<O>();
		this.listeners = new HashSet<TypedTreeModelListener<PropertiedTreeNode<O>>>();
		this.nodeMap = new HashMap<O, PropertiedTreeNode<O>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addTreeModelListener(
			final TypedTreeModelListener<PropertiedTreeNode<O>> l) {
		if (l == null) {
			throw new NullPointerException("l cannot be null"); //$NON-NLS-1$
		}
		this.listeners.add(l);
	}

	private void constructSubTree(final PropertiedTreeNode<O> parent,
			final int settingIndex, final Set<O> permittedObjects) {
		if (settingIndex != this.keySettings.size()) {
			PropertyKeySetting setting = this.keySettings.get(settingIndex);
			PropertyKey key = setting.getPropertyKey();
			if (key == null) {
				throw new IllegalStateException("key cannot be null");
			}
			Set<PropertyValue> values = this.propertiedGraphView.getValues(key);

			// Note that a null compaarator is OK
			TreeSet<PropertyValue> sortedValues = new TreeSet<PropertyValue>(
					setting.getComparator());
			sortedValues.addAll(values);

			Set<O> remainingObjects = new HashSet<O>(permittedObjects);
			for (PropertyValue value : sortedValues) {
				PropertiedTreePropertyValueNode<O> subNode = new PropertiedTreePropertyValueNodeImpl<O>();
				subNode.setKey(key);
				subNode.setValue(value);
				Set<PropertiedGraphNode<O>> nodes = this.propertiedGraphView
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
			if (this.objectComparator != null) {
			TreeSet<O> sortedObjects = new TreeSet<O>(this.objectComparator);
			for (O object : permittedObjects) {
				try {
					sortedObjects.add(object);
				} catch (ClassCastException e) {
					throw (e);
					// Here we are
				}
			}
//			sortedObjects.addAll(permittedObjects);
			for (O object : sortedObjects) {
				PropertiedTreeNode<O> subNode = createTwig(object);
				parent.addChild(subNode);
				this.nodeMap.put(object, subNode);
			}
		} else {
			for (O object : permittedObjects) {
				PropertiedTreeNode<O> subNode = createTwig(object);
				parent.addChild(subNode);
				this.nodeMap.put(object, subNode);				
			}
		}
		}
	}
	
	@SuppressWarnings("unchecked")
	private PropertiedTreeNode<O> createTwig(O object) {
		TwigConstructor constructor = TwigConstructorRegistry.getTwigConstructor(object.getClass());
		if (constructor == null) {
			constructor = DefaultTwigConstructor.getInstance();
		}
		PropertiedTreeNode<O> subNode = constructor.createTwig(object);
		return subNode;
	}

	private void generateTree() {
		if (this.root == null) {
			this.root = new PropertiedTreeRootNodeImpl<O>();
		} else {
			this.root.removeAllChildren();
		}
		this.nodeMap = new HashMap<O, PropertiedTreeNode<O>>();
		Set<O> filteredObjects = new HashSet<O>();
		Set<PropertiedGraphNode<O>> graphNodes = this.propertiedGraphView
				.getNodes();
		for (PropertiedGraphNode<O> graphNode : graphNodes) {
			if ((this.filter == null)
					|| (this.filter.acceptObject(graphNode.getObject()))) {
				filteredObjects.add(graphNode.getObject());
			}
		}
		constructSubTree(this.root, 0, filteredObjects);

		notifyListenersTreeStructureChanged(new TypedTreeModelEvent<PropertiedTreeNode<O>>(
				this, this.root.getPath()));
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedTreeNode<O> getChild(final PropertiedTreeNode<O> parent,
			int index) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null"); //$NON-NLS-1$
		}
		return parent.getChild(index);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getChildCount(final PropertiedTreeNode<O> parent) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null"); //$NON-NLS-1$
		}
		return parent.getChildCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getIndexOfChild(PropertiedTreeNode<O> parent,
			PropertiedTreeNode<O> child) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null"); //$NON-NLS-1$
		}
		if (child == null) {
			throw new NullPointerException("child cannot be null"); //$NON-NLS-1$
		}
		return parent.getIndexOfChild(child);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PropertyKeySetting> getPropertyKeySettings() {
		List<PropertyKeySetting> result;
		if (this.keySettings == null) {
			result = null;
		} else {
			result = new ArrayList<PropertyKeySetting>(this.keySettings);
		}
		// Copy just in case
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedTreeNode<O> getRoot() {
		return this.root;
	}

	void internalEdgeChanged(PropertiedGraphEdge<O> edge,
			PropertiedGraphNode<O> node) {
		O object = node.getObject();
		if (this.nodeMap.containsKey(object)) {
			PropertiedTreeNode<O> treeNode = this.nodeMap.get(object);
			PropertyKey key = edge.getKey();
			PropertiedTreeNode<O> ancestor = treeNode.getAncestorWithKey(key);
			if (ancestor != null) {
				PropertiedTreeNode<O> containerNode = ancestor.getParent();
				Set<O> containedObjects = containerNode.getAllObjects();

				// If object is now filtered
				if ((this.filter != null)
						&& !(this.filter.acceptObject(object))) {
					containedObjects.remove(object);
					this.nodeMap.remove(object);
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

	void internalNodeAdded(PropertiedGraphNode<O> node) {
		O object = node.getObject();
		if ((this.filter == null) || (this.filter.acceptObject(object))) {
			generateTree();
		}

	}

	void internalNodeRemoved(PropertiedGraphNode<O> node) {
		if (this.nodeMap.containsKey(node.getObject())) {
			generateTree();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLeaf(final PropertiedTreeNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		return (node.getChildCount() == 0);
	}

	@SuppressWarnings( { "unchecked", "unused" })
	private void notifyListenersTreeNodesChanged(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = this.listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeNodesChanged(e);
		}
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private void notifyListenersTreeNodesInserted(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = this.listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeNodesInserted(e);
		}
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private void notifyListenersTreeNodesRemoved(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = this.listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeNodesRemoved(e);
		}
	}

	@SuppressWarnings("unchecked")
	private void notifyListenersTreeStructureChanged(
			TypedTreeModelEvent<PropertiedTreeNode<O>> e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		TypedTreeModelListener<PropertiedTreeNode<O>>[] copy = this.listeners
				.toArray(new TypedTreeModelListener[0]);
		for (TypedTreeModelListener<PropertiedTreeNode<O>> l : copy) {
			l.treeStructureChanged(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeTreeModelListener(
			final TypedTreeModelListener<PropertiedTreeNode<O>> l) {
		if (l == null) {
			throw new NullPointerException("l cannot be null"); //$NON-NLS-1$
		}
		this.listeners.remove(l);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFilter(final PropertiedObjectFilter<O> filter) {
		if (this.filter != null) {
			throw new IllegalStateException(
					"filter cannot be initialized more than once"); //$NON-NLS-1$
		}
		// OK to be null
		this.filter = filter;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setObjectComparator(Comparator<O> objectComparator) {
		if (objectComparator == null) {
			throw new NullPointerException("objectComparator cannot be null"); //$NON-NLS-1$
		}
		if (this.objectComparator != null) {
			throw new IllegalStateException(
					"objectComparator cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.objectComparator = objectComparator;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setPropertiedGraphView(
			final PropertiedGraphView<O> propertiedGraphView) {
		if (propertiedGraphView == null) {
			throw new NullPointerException("propertiedGraphView cannot be null"); //$NON-NLS-1$
		}
		if (this.keySettings == null) {
			throw new IllegalStateException("keySettings must be set"); //$NON-NLS-1$
		}
		this.propertiedGraphView = propertiedGraphView;
		this.posl = new PropertiedGraphViewListener<O>() {

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
		this.propertiedGraphView.addListener(this.posl);

		generateTree();

	}

	/**
	 * {@inheritDoc}
	 */
	public void setPropertyKeySettings(
			final List<PropertyKeySetting> settingList) {
		if (settingList == null) {
			throw new NullPointerException("settingList cannot be null"); //$NON-NLS-1$
		}
		if (this.keySettings != null) {
			throw new IllegalStateException(
					"keySettings can only be initialized once"); //$NON-NLS-1$
		}
		// Copy just in case
		this.keySettings = new ArrayList<PropertyKeySetting>(settingList);
	}

	/**
	 * {@inheritDoc}
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedObjectFilter<O> getFilter() {
		return this.filter;
	}

	/**
	 * {@inheritDoc}
	 */
	public Comparator<O> getObjectComparator() {
		return this.objectComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public void detachFromGraphView() {
		if (this.posl != null) {
			this.propertiedGraphView.removeListener(this.posl);
		}
	}

}
