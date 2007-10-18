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

/**
 * PropertiedGraphViewImpl is an implementation of the PropertiedGraphView
 * interface.
 * 
 * @author alanrw
 * 
 */
public final class PropertiedGraphViewImpl<O> implements PropertiedGraphView<O> {

	/**
	 * propertiedObjectSet holds the PropertiesObjectSet of which the
	 * PropertiedGraphView is a view.
	 */
	private PropertiedObjectSet<O> propertiedObjectSet;

	/**
	 * edges holds the Set of PropertiedGraphEdge that connect nodes within the
	 * graph. It is legal for an edge to connect no nodes.
	 */
	private HashSet<PropertiedGraphEdge<O>> edges;

	/**
	 * nodes holds the Set of PropertiedGraphNodes that correspond to Objects
	 * within the PropertiedObjectSet of which the graph is a view.
	 */
	private HashSet<PropertiedGraphNode<O>> nodes;

	/**
	 * nodeMap maps the Objects within the PropertiedObjectSet to their
	 * corresponding PropertiedGraphNode within the graph.
	 * 
	 * The nodes Set may be redundant.
	 */
	private HashMap<O, PropertiedGraphNode<O>> nodeMap;

	/**
	 * edgeMap maps a PropertyKey + PropertyValue to the corresponding
	 * PropertiedGGraphEdge within the graph.
	 */
	private HashMap<PropertyKey, HashMap<PropertyValue, PropertiedGraphEdge<O>>> edgeMap;

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
		propertiedObjectSet = null;
		edges = new HashSet<PropertiedGraphEdge<O>>();
		nodes = new HashSet<PropertiedGraphNode<O>>();

		nodeMap = new HashMap<O, PropertiedGraphNode<O>>();
		edgeMap = new HashMap<PropertyKey, HashMap<PropertyValue, PropertiedGraphEdge<O>>>();

		listeners = new HashSet<PropertiedGraphViewListener<O>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#addListener(net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener)
	 */
	public void addListener(final PropertiedGraphViewListener<O> listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null");
		}
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#getEdge(net.sf.taverna.t2.drizzle.util.PropertyKey, net.sf.taverna.t2.drizzle.util.PropertyValue)
	 */
	public PropertiedGraphEdge<O> getEdge(PropertyKey key, PropertyValue value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		PropertiedGraphEdge<O> result = null;
		if (edgeMap.containsKey(key)) {
			result = edgeMap.get(key).get(value);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#getEdges()
	 */
	public Set<PropertiedGraphEdge<O>> getEdges() {
		// edges cannot just be copied because some edges may no longer connect
		// nodes
		HashSet<PropertiedGraphEdge<O>> result = new HashSet<PropertiedGraphEdge<O>>();
		for (PropertiedGraphEdge<O> edge : edges) {
			if (edge.getNodes().size() > 0) {
				result.add(edge);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#getNode(java.lang.Object)
	 */
	public PropertiedGraphNode<O> getNode(O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null");
		}
		return nodeMap.get(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#getNodes()
	 */
	public Set<PropertiedGraphNode<O>> getNodes() {
		// Copy to be safe
		return new HashSet<PropertiedGraphNode<O>>(nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#getPropertiedObjectSet()
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
	private void notifyListenersEdgeAdded(final PropertiedGraphEdge<O> edge,
			final PropertiedGraphNode<O> node) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null");
		}
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		PropertiedGraphViewListener<O>[] copy = listeners
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
	private void notifyListenersEdgeRemoved(final PropertiedGraphEdge<O> edge,
			final PropertiedGraphNode<O> node) {
		if (edge == null) {
			throw new NullPointerException("edge cannot be null");
		}
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		PropertiedGraphViewListener<O>[] copy = listeners
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
	private void notifyListenersNodeAdded(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		PropertiedGraphViewListener<O>[] copy = listeners
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
	private void notifyListenersNodeRemoved(final PropertiedGraphNode<O> node) {
		if (node == null) {
			throw new NullPointerException("node cannot be null");
		}
		PropertiedGraphViewListener<O>[] copy = listeners
				.toArray(new PropertiedGraphViewListener[0]);
		for (PropertiedGraphViewListener<O> l : copy) {
			l.nodeRemoved(this, node);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#removeListener(net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener)
	 */
	public void removeListener(final PropertiedGraphViewListener<O> listener) {
		if (listener == null) {
			throw new NullPointerException("listener cannot be null");
		}
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#setPropertiedObjectSet(net.sf.taverna.t2.drizzle.util.PropertiedObjectSet)
	 */
	public void setPropertiedObjectSet(
			final PropertiedObjectSet<O> propertiedObjectSet) {
		if (propertiedObjectSet == null) {
			throw new NullPointerException("propertiedObjectSet cannot be null");
		}
		if (this.propertiedObjectSet != null) {
			throw new IllegalStateException(
					"Cannot be initialized more than once");
		}
		this.propertiedObjectSet = propertiedObjectSet;
		
		PropertiedObjectSetListener posListener =
			new PropertiedObjectSetListener() {

			public void objectAdded(PropertiedObjectSet pos, Object o) {
				if (!nodeMap.keySet().contains(o)) {
					PropertiedGraphNode<O> node = new PropertiedGraphNodeImpl<O>();
					node.setObject((O)o);
					nodes.add(node);
					nodeMap.put((O)o, node);
					notifyListenersNodeAdded(node);
				}
			}

			public void objectRemoved(PropertiedObjectSet pos, Object o) {
				if (nodeMap.keySet().contains(o)) {
					PropertiedGraphNode<O> node = nodeMap.get(o);
					Set<PropertiedGraphEdge<O>> edges = node.getEdges();
					for (PropertiedGraphEdge<O> edge : edges) {
						edge.removeNode(node);
						notifyListenersEdgeRemoved(edge, node);
					}
					nodes.remove(node);
					nodeMap.remove(o);
					notifyListenersNodeRemoved(node);
				}
			}
			
		};
		this.propertiedObjectSet.addListener (posListener);

		PropertiedObjectListener poListener =
			new PropertiedObjectListener() {

				public void propertyAdded(Object o, PropertyKey key, PropertyValue value) {
					if (!nodeMap.keySet().contains(o)) {
						throw new IllegalArgumentException(
								"o must be in the propertiedObjectSet");
					}
					PropertiedGraphNode<O> node = nodeMap.get(o);
					PropertiedGraphEdge<O> edge = null;
					HashMap<PropertyValue, PropertiedGraphEdge<O>> valueMap = null;
					if (edgeMap.containsKey(key)) {
						valueMap = edgeMap.get(key);
					} else {
						valueMap = new HashMap<PropertyValue, PropertiedGraphEdge<O>>();
						edgeMap.put(key, valueMap);
					}
					if (valueMap.containsKey(value)) {
						edge = valueMap.get(value);
					} else {
						edge = new PropertiedGraphEdgeImpl<O>();
						edges.add(edge);
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

				public void propertyChanged(Object o, PropertyKey key, PropertyValue oldValue, PropertyValue newValue) {
					propertyRemoved(o, key, oldValue);
					propertyAdded(o, key, newValue);
				}

				public void propertyRemoved(Object o, PropertyKey key, PropertyValue value) {
					if (!nodeMap.keySet().contains(o)) {
						throw new IllegalArgumentException(
								"o must be in the propertiedObjectSet");
					}
					HashMap<PropertyValue, PropertiedGraphEdge<O>> valueMap = edgeMap
							.get(key);
					if (valueMap == null) {
						throw new IllegalStateException("key has no entry in edgeMap");
					}
					PropertiedGraphEdge<O> edge = valueMap.get(value);
					if (edge == null) {
						throw new IllegalStateException("value has no entry in edgeMap");
					}
					PropertiedGraphNode<O> node = nodeMap.get(o);
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedGraphView#replayToListener(net.sf.taverna.t2.drizzle.util.PropertiedGraphViewListener)
	 */
	public void replayToListener(PropertiedGraphViewListener<O> listener) {
		for (PropertiedGraphNode<O> node : nodes) {
			listener.nodeAdded(this, node);
			for (PropertiedGraphEdge<O> edge : node.getEdges()) {
				listener.edgeAdded(this, edge, node);
			}
		}
	}

	public Set<PropertyKey> getKeys() {
		return new HashSet<PropertyKey>(edgeMap.keySet());
	}

	public Set<PropertyValue> getValues(PropertyKey key) {
		if (key == null) {
			throw new NullPointerException ("key cannot be null");
		}
		Set<PropertyValue> result;
		if (edgeMap.containsKey(key)) {
			result = edgeMap.get(key).keySet();
		}
		else {
			result = new HashSet<PropertyValue> ();
		}
		return result;
	}

}
