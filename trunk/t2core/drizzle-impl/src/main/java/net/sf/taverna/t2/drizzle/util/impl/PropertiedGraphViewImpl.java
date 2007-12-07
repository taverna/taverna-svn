/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedGraphEdge;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphNode;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * PropertiedGraphViewImpl is an implementation of the PropertiedGraphView
 * interface.
 * 
 * @author alanrw
 * 
 * @param <O>
 *            The class of Object within the PropertiedObjectSet of which the
 *            PropertiedGraphView is a view.
 */
public final class PropertiedGraphViewImpl<O extends Beanable<?>> implements PropertiedGraphView<O> {

	/**
	 * propertiedObjectSet holds the PropertiesObjectSet of which the
	 * PropertiedGraphView is a view.
	 */
	private PropertiedObjectSet<O> propertiedObjectSet;

	/**
	 * edges holds the Set of PropertiedGraphEdge that connect nodes within the
	 * graph. It is legal for an edge to connect no nodes.
	 */
	HashSet<PropertiedGraphEdge<O>> edges;

	/**
	 * nodes holds the Set of PropertiedGraphNodes that correspond to Objects
	 * within the PropertiedObjectSet of which the graph is a view.
	 */
	HashSet<PropertiedGraphNode<O>> nodes;

	/**
	 * nodeMap maps the Objects within the PropertiedObjectSet to their
	 * corresponding PropertiedGraphNode within the graph.
	 * 
	 * The nodes Set may be redundant.
	 */
	HashMap<O, PropertiedGraphNode<O>> nodeMap;

	/**
	 * edgeMap maps a PropertyKey + PropertyValue to the corresponding
	 * PropertiedGGraphEdge within the graph.
	 */
	HashMap<PropertyKey, HashMap<PropertyValue, PropertiedGraphEdge<O>>> edgeMap;

	/**
	 * listeners holds the Set of PropertiedGraphViewListeners that listen to
	 * the addition or removal of a PropertiedGraphNode or PropertiedgraphEdge.
	 */
	private HashSet<PropertiedGraphViewListener<O>> listeners;

	/**
	 * Create a new PropertiedGraphView.
	 */
	public PropertiedGraphViewImpl() {
		super();
		this.propertiedObjectSet = null;
		this.edges = new HashSet<PropertiedGraphEdge<O>>();
		this.nodes = new HashSet<PropertiedGraphNode<O>>();

		this.nodeMap = new HashMap<O, PropertiedGraphNode<O>>();
		this.edgeMap = new HashMap<PropertyKey, HashMap<PropertyValue, PropertiedGraphEdge<O>>>();

		this.listeners = new HashSet<PropertiedGraphViewListener<O>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(final PropertiedGraphViewListener<O> listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null"); //$NON-NLS-1$
		}
		this.listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedGraphEdge<O> getEdge(PropertyKey key, PropertyValue value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		PropertiedGraphEdge<O> result = null;
		if (this.edgeMap.containsKey(key)) {
			result = this.edgeMap.get(key).get(value);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertiedGraphEdge<O>> getEdges() {
		// edges cannot just be copied because some edges may no longer connect
		// nodes
		HashSet<PropertiedGraphEdge<O>> result = new HashSet<PropertiedGraphEdge<O>>();
		for (PropertiedGraphEdge<O> edge : this.edges) {
			if (edge.getNodes().size() > 0) {
				result.add(edge);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedGraphNode<O> getNode(O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		return this.nodeMap.get(object);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertiedGraphNode<O>> getNodes() {
		// Copy to be safe
		return new HashSet<PropertiedGraphNode<O>>(this.nodes);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedObjectSet<O> getPropertiedObjectSet() {
		return this.propertiedObjectSet;
	}

	/**
	 * Notify listeners of the addition of the specified node to the Set of
	 * PropertiedGraphNode connected by the specified edge. The edge itself may
	 * be new to the graph.
	 * 
	 * @param edge
	 * @param node
	 */
	@SuppressWarnings("unchecked") void notifyListenersEdgeAdded(final PropertiedGraphEdge<O> edge,
			final PropertiedGraphNode<O> node) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null"); //$NON-NLS-1$
		}
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		PropertiedGraphViewListener<O>[] copy = this.listeners
				.toArray(new PropertiedGraphViewListener[0]);
		for (PropertiedGraphViewListener<O> l : copy) {
			l.edgeAdded(this, edge, node);
		}

	}

	/**
	 * Notify listeners of the removal of the specified node from the Set of
	 * PropertiedGraphNode connected by the specified edge. The edge may as a
	 * result connect zero nodes.
	 * 
	 * @param edge
	 * @param node
	 */
	@SuppressWarnings({"unchecked" }) void notifyListenersEdgeRemoved(final PropertiedGraphEdge<O> edge,
			final PropertiedGraphNode<O> node) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null"); //$NON-NLS-1$
		}
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		PropertiedGraphViewListener<O>[] copy = this.listeners
				.toArray(new PropertiedGraphViewListener[0]);
		for (PropertiedGraphViewListener<O> l : copy) {
			l.edgeRemoved(this, edge, node);
		}
	}

	/**
	 * Notify listeners of the addition of the specified node to the
	 * PropertiedGraphView.
	 * 
	 * @param node
	 */
	@SuppressWarnings("unchecked") void notifyListenersNodeAdded(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		PropertiedGraphViewListener<O>[] copy = this.listeners
				.toArray(new PropertiedGraphViewListener[0]);
		for (PropertiedGraphViewListener<O> l : copy) {
			l.nodeAdded(this, node);
		}
	}

	/**
	 * Notify listeners of the removal of the specified node from the
	 * PropertiedGraphView. The node should have already been disconnected from
	 * its edges.
	 * 
	 * @param node
	 */
	@SuppressWarnings("unchecked") void notifyListenersNodeRemoved(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null"); //$NON-NLS-1$
		}
		PropertiedGraphViewListener<O>[] copy = this.listeners
				.toArray(new PropertiedGraphViewListener[0]);
		for (PropertiedGraphViewListener<O> l : copy) {
			l.nodeRemoved(this, node);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(final PropertiedGraphViewListener<O> listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null"); //$NON-NLS-1$
		}
		this.listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPropertiedObjectSet(
			final PropertiedObjectSet<O> propertiedObjectSet) {
		if (propertiedObjectSet == null) {
			throw new NullPointerException("propertiedObjectSet cannot be null"); //$NON-NLS-1$
		}
		if (this.propertiedObjectSet != null) {
			throw new IllegalStateException(
					"Cannot be initialized more than once"); //$NON-NLS-1$
		}
		this.propertiedObjectSet = propertiedObjectSet;

		PropertiedObjectSetListener posListener = new PropertiedObjectSetListener() {

			@SuppressWarnings("unchecked")
			public void objectAdded(PropertiedObjectSet<?> pos, Object o) {
				if (!PropertiedGraphViewImpl.this.nodeMap.keySet().contains(o)) {
					PropertiedGraphNode<O> node = new PropertiedGraphNodeImpl<O>();
					node.setObject((O) o);
					PropertiedGraphViewImpl.this.nodes.add(node);
					PropertiedGraphViewImpl.this.nodeMap.put((O) o, node);
					notifyListenersNodeAdded(node);
				}
			}

			public void objectRemoved(PropertiedObjectSet<?> pos, Object o) {
				if (PropertiedGraphViewImpl.this.nodeMap.keySet().contains(o)) {
					PropertiedGraphNode<O> node = PropertiedGraphViewImpl.this.nodeMap.get(o);
					Set<PropertiedGraphEdge<O>> nodeEdges = node.getEdges();
					for (PropertiedGraphEdge<O> edge : nodeEdges) {
						edge.removeNode(node);
						notifyListenersEdgeRemoved(edge, node);
					}
					PropertiedGraphViewImpl.this.nodes.remove(node);
					PropertiedGraphViewImpl.this.nodeMap.remove(o);
					notifyListenersNodeRemoved(node);
				}
			}

		};
		this.propertiedObjectSet.addListener(posListener);

		PropertiedObjectListener poListener = new PropertiedObjectListener() {

			public void propertyAdded(Object o, PropertyKey key,
					PropertyValue value) {
				if (!PropertiedGraphViewImpl.this.nodeMap.keySet().contains(o)) {
					throw new IllegalArgumentException(
							"o must be in the propertiedObjectSet"); //$NON-NLS-1$
				}
				PropertiedGraphNode<O> node = PropertiedGraphViewImpl.this.nodeMap.get(o);
				PropertiedGraphEdge<O> edge = null;
				HashMap<PropertyValue, PropertiedGraphEdge<O>> valueMap = null;
				if (PropertiedGraphViewImpl.this.edgeMap.containsKey(key)) {
					valueMap = PropertiedGraphViewImpl.this.edgeMap.get(key);
				} else {
					valueMap = new HashMap<PropertyValue, PropertiedGraphEdge<O>>();
					PropertiedGraphViewImpl.this.edgeMap.put(key, valueMap);
				}
				if (valueMap.containsKey(value)) {
					edge = valueMap.get(value);
				} else {
					edge = new PropertiedGraphEdgeImpl<O>();
					PropertiedGraphViewImpl.this.edges.add(edge);
					edge.setKey(key);
					edge.setValue(value);
					valueMap.put(value, edge);
				}
				if (!edge.getNodes().contains(edge)) {
					edge.addNode(node);
					node.addEdge(edge);
					notifyListenersEdgeAdded(edge, node);
				}
			}

			public void propertyChanged(Object o, PropertyKey key,
					PropertyValue oldValue, PropertyValue newValue) {
				propertyRemoved(o, key, oldValue);
				propertyAdded(o, key, newValue);
			}

			public void propertyRemoved(Object o, PropertyKey key,
					PropertyValue value) {
				if (!PropertiedGraphViewImpl.this.nodeMap.keySet().contains(o)) {
					throw new IllegalArgumentException(
							"o must be in the propertiedObjectSet"); //$NON-NLS-1$
				}
				HashMap<PropertyValue, PropertiedGraphEdge<O>> valueMap = PropertiedGraphViewImpl.this.edgeMap
						.get(key);
				if (valueMap == null) {
					throw new IllegalStateException(
							"key has no entry in edgeMap"); //$NON-NLS-1$
				}
				PropertiedGraphEdge<O> edge = valueMap.get(value);
				if (edge == null) {
					throw new IllegalStateException(
							"value has no entry in edgeMap"); //$NON-NLS-1$
				}
				PropertiedGraphNode<O> node = PropertiedGraphViewImpl.this.nodeMap.get(o);
				if (edge.getNodes().contains(node)) {
					edge.removeNode(node);
					node.removeEdge(edge);
					notifyListenersEdgeRemoved(edge, node);
				}
			}

		};
		this.propertiedObjectSet.addAllObjectsListener(poListener);

		propertiedObjectSet.replayToListener(posListener);
		propertiedObjectSet.replayToAllObjectsListener(poListener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void replayToListener(PropertiedGraphViewListener<O> listener) {
		for (PropertiedGraphNode<O> node : this.nodes) {
			listener.nodeAdded(this, node);
			for (PropertiedGraphEdge<O> edge : node.getEdges()) {
				listener.edgeAdded(this, edge, node);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertyKey> getKeys() {
		return new HashSet<PropertyKey>(this.edgeMap.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertyValue> getValues(PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		Set<PropertyValue> result;
		if (this.edgeMap.containsKey(key)) {
			result = this.edgeMap.get(key).keySet();
		} else {
			result = new HashSet<PropertyValue>();
		}
		return result;
	}

}
