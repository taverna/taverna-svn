/*
 * Created on Feb 9, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.2 $
 */
public abstract class GraphSpanningTree
{
	private static final String TREE_SET = "row tree set";	
	
//	protected class SortedList extends ArrayList
//	{
//		private Comparator comparator;
//		
//		public SortedList(Comparator comparator)
//		{
//			this.comparator = comparator;
//		}
//		
//		public boolean add(Object edge)
//		{
//			for(int index = 0; index < size(); index++)
//			{
//				if(comparator.compare(edge, get(index)) <= 0)
//				{
//					add(index, edge);
//					return true;
//				}
//			}
//			return super.add(edge);
//		}
//	}

	/**
	 * @param edge
	 * @return the source node of the given edge
	 */
	protected abstract Object getSource(Object edge);

	/**
	 * @param edge
	 * @return the target node of the given edge
	 */
	protected abstract Object getTarget(Object edge);

	protected abstract Map getAttributes(Object graphObject);
	
	/**
	 * @param node
	 * @return an iterator over all the edges connected to the given node
	 */
	protected abstract Iterator getEdges(Object node);

	/**
	 * @param edge
	 * @return the weight of the given edge
	 */
	protected abstract int getEdgeWeight(Object edge);

	/**
	 * @param node
	 * @return the rank of the given node
	 */
	protected abstract int getRank(Object node);

	/**
	 * @param node
	 * @return gets the minimum possible rank for the given node.
	 */
	protected abstract int getMinimumRank(Object node);

	/**
	 * @param edge
	 */
	protected abstract void addNonTreeEdge(Object edge);

	/**
	 * @param node
	 * @return gets the maximum rank for the given node.
	 */
	protected abstract int getMaximumRank(Object node);

	/**
	 * @param node
	 * @return the parent in the spanning tree
	 */
	protected abstract Object getTreeParent(Object node);

	/**
	 * @param child
	 * @param parent
	 */
	protected abstract void setTreeParent(Object child, Object parent);

	/**
	 * @param node
	 * @return the set of nodes of the spanning tree
	 */
	protected abstract Set getTreeSet(Object node);

	/**
	 * @param edge
	 * @return the minimum length of the given edge
	 */
	protected abstract int getMinimumEdgeLength(Object edge);

	/**
	 * @param node
	 * @param treeSet
	 */
	protected abstract void setTreeSet(Object node, Set treeSet);

	/**
	 * Removes an edge from the spanning tree.
	 * 
	 * @param edge
	 *            the edge to remove
	 */
	protected abstract void removeTreeEdge(Object edge);

	protected void removeEdge(Object edge)
	{
		if(isTreeEdge(edge))
		{
			replaceTreeEdge(edge);
		}
	}
	
	/**
	 * @param parent
	 */
//	private void treeAddChildren(Object parent)
//	{
//		Iterator edges = getEdges(parent);
//		while (edges.hasNext())
//		{
//			// Follow all tight edges
//			Object edge = edges.next();
//			if (!isTreeEdge(edge))
//			{
//				Object child = getNeighbour(parent, edge);
//				if (getTreeSet(child) == null)
//				{
//					if (getRank(getTarget(edge)) - getRank(getSource(edge)) == getMinimumEdgeLength(edge))
//					{
//						addTreeEdge(parent, child, edge);
//					}
//				}
//			}
//		}
//	}

	/**
	 * @param nodes
	 */
	protected void moveNodes(Set nodes, int change)
	{
		System.err.println("Move nodes " + nodes + " by " + change);
		Iterator iterator = nodes.iterator();
		while(iterator.hasNext())
		{
			shiftRank(iterator.next(), change);			
		}
	}

	protected abstract void setRank(Object node, int rank);

	protected abstract void shiftRank(Object node, int rankChange);
	
	/**
	 * @param node
	 * @param tailSet
	 */
	private void getTailSet(Object node, Set tailSet)
	{
		tailSet.add(node);
		Object parent = getTreeParent(node);
		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (edge != parent && isTreeEdge(edge))
			{
				getTailSet(getNeighbour(node, edge), tailSet);
			}
		}
	}

	/**
	 * @param node
	 * @param edge
	 * @return get the neighbouring node of an edge
	 */
	private Object getNeighbour(Object node, Object edge)
	{
		Object source = getSource(edge);
		if (node == source)
		{
			return getTarget(edge);
		}
		return source;
	}

	protected abstract Comparator getComparator();
	
	/**
	 * Get all non-tree edges connecting a node in the head set to a node in the
	 * tail set
	 * 
	 * @param tree1
	 * @param tree2
	 * @return list of edges connecting the head and tail sets
	 */
	private Collection getConnectingEdges(Set tree1, Set tree2)
	{
		Collection joiningEdges = new TreeSet(getComparator());
		Iterator nodes;
		if(tree1.size() < tree2.size())
		{
			nodes = tree1.iterator();
		}
		else
		{
			nodes = tree2.iterator();
		}
		while (nodes.hasNext())
		{
			Iterator edges = getEdges(nodes.next());
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (!isTreeEdge(edge))
				{
					Object source = getSource(edge);
					Object target = getTarget(edge);
					if (tree1.contains(source))
					{
						if (tree2.contains(target))
						{
							//System.err.println("Connecting edge " + edge);
							joiningEdges.add(edge);
						}
					}
					else if (tree2.contains(source))
					{
						if (tree1.contains(target))
						{
							//System.err.println("Connecting edge " + edge);
							joiningEdges.add(edge);
						}
					}
				}
			}
		}
		System.err.println("Edges between " + tree1 + " and " +tree2 + ": " +joiningEdges);
		return joiningEdges;
	}

	/**
	 * @param parent
	 * @param child
	 * @param edge
	 */
	protected void addTreeEdge(Object parent, Object child, Object edge)
	{
		System.err.println("Add tree edge " + edge);
		Set tree1 = getTreeSet(parent);
		Set tree2 = getTreeSet(child);
		if (tree1 == null)
		{
			tree1 = new HashSet();
			tree1.add(parent);
			setTreeSet(parent, tree1);
		}
		if (tree2 == null)
		{
			tree2 = new HashSet();
			tree2.add(child);
		}

		setTreeParent(edge, parent);
		setTreeParent(child, edge);		
		
		if (tree1 != tree2)
		{
			if(parent == getSource(edge))
			{
				tightenEdge(edge, tree1, tree2);
			}
			else
			{
				tightenEdge(edge, tree2, tree1);			
			}
			
			if (tree2.size() > tree1.size())
			{
				Set temp = tree1;
				tree1 = tree2;
				tree2 = temp;
			}
			tree1.addAll(tree2);
			Iterator nodes = tree2.iterator();
			while (nodes.hasNext())
			{
				setTreeSet(nodes.next(), tree1);
			}
		}
		else
		{
			Set tailSet = new HashSet();
			getTailSet(child, tailSet);
			Set headSet = new HashSet();
			headSet.addAll(getTreeSet(parent));
			headSet.removeAll(tailSet);
			
			if(parent == getSource(edge))
			{
				tightenEdge(edge, headSet, tailSet);
			}
			else
			{
				tightenEdge(edge, tailSet, headSet);			
			}
		}
		//System.err.println(tree1);
		//treeAddChildren(child);
		assert getTreeParent(edge) != null: edge;
	}

	/**
	 * Minimizes the length of a given edge.
	 * 
	 * @param edge
	 */
	protected void tightenEdge(Object edge, Set sourceSet, Set targetSet)
	{
		// TODO Rewrite more generically?
		assert(sourceSet != targetSet);
		assert(sourceSet != null);
		assert(targetSet != null);
		Object source = getSource(edge);
		Object target = getTarget(edge);

		int sourceRow = getRank(source);
		int sourceMinRow = getMinimumRank(source);
		int targetRow = getRank(target);
		int targetMinRow = getMinimumRank(target);
		int minimumEdgeLength = getMinimumEdgeLength(edge);

		if (targetRow - sourceRow != minimumEdgeLength)
		{
			if (sourceRow < sourceMinRow)
			{
				int sourceMaxRow = getMaximumRank(source);
				if (targetMinRow - sourceMaxRow > minimumEdgeLength)
				{
					// System.err.println("Rearrange & remove tree edge " +
					// edge);
					// TODO!
					System.err.println("Would be removing tree edge " + edge + " here!");					
					//removeTreeEdge(edge);
				}
				else
				{
					moveNodes(sourceSet, sourceRow - (targetRow - getMinimumEdgeLength(edge)));
				}
			}
			// TODO Is final clause even possible?
			// TODO Change to move to target min row?
			else if (targetRow < targetMinRow || targetMinRow <= sourceRow)
			{
				moveNodes(targetSet, (sourceRow + minimumEdgeLength) - targetRow);
			}
			else
			{
				int sourceMaxRow = getMaximumRank(source);
				if (targetMinRow - sourceMaxRow > minimumEdgeLength)
				{
					// TODO!
					System.err.println("Would be removing tree edge " + edge + " here!");
					//removeTreeEdge(edge);
				}
				else
				{
					if (sourceRow < sourceMaxRow)
					{
						moveNodes(sourceSet, (targetRow - minimumEdgeLength) - sourceRow);
					}
					else
					{
						moveNodes(targetSet, (sourceRow + minimumEdgeLength) - targetRow);
					}
				}
			}
		}
	}

	/**
	 * @param treeEdge
	 * @param timeStamp
	 * @return the cut value for the given tree edge
	 */
	protected int getCutValue(Object treeEdge, String timeStamp)
	{
		Object parent = getTreeParent(treeEdge);
		assert parent != null : treeEdge;
		Object child = getSource(treeEdge);
		int direction = 1;
		if (child == parent)
		{
			child = getTarget(treeEdge);
			direction = -1;
		}

		//String text = "";

		Iterator edges = getEdges(child);
		int cutValue = 0;
		while (edges.hasNext())
		{
			Object edge = edges.next();
			int value = getEdgeWeight(edge);
			if (edge != treeEdge)
			{
				if (child == getTarget(edge))
				{
					value *= -direction;
				}
				else
				{
					value *= direction;
				}

				if (isTreeEdge(edge))
				{
					if(child == getTarget(edge))
					{
						value += getCutValue(edge, timeStamp) * direction;
					}
					else
					{
						value += getCutValue(edge, timeStamp) * -direction;
					}
					//text += "[" + edge + "]";					
				}
			}
			cutValue += value;
			//text += value + " ";
		}
		//System.err.println("Cut for " + treeEdge + " = " + cutValue + " = " + text
		//		+ ", direction = " + direction + ", child = " + child);
		return cutValue;
	}

	/**
	 * @param treeEdges
	 */
	protected void optimiseTree(Collection treeEdges)
	{
		while(true)
		{
			Object cutEdge = null;
			int lowestCut = Integer.MAX_VALUE;
			String timeStamp = "" + System.currentTimeMillis() + Math.random();
			Iterator iterator = treeEdges.iterator();
			while (iterator.hasNext())
			{
				Object edge = iterator.next();
				int cut = getCutValue(edge, timeStamp);
				if (cut < lowestCut)
				{
					lowestCut = cut;
					cutEdge = edge;
				}
			}
			if (lowestCut < 0)
			{
				treeEdges.remove(cutEdge);
				Object replacedEdge = replaceTreeEdge(cutEdge);
				if (replacedEdge != null)
				{
					treeEdges.add(replacedEdge);
				}
			}
			else
			{
				break;
			}
		}
	}

	/**
	 * Attempts to replace a edge in the spanning tree with a
	 * 
	 * @param edge
	 *            the tree edge to replace
	 */
	protected Object replaceTreeEdge(Object edge)
	{
		Object parent = getTreeParent(edge);
		assert parent != null: edge;
		Object tailParent = getNeighbour(parent, edge);

		Set tailSet = new HashSet();
		getTailSet(tailParent, tailSet);
		Set headSet = new HashSet();
		headSet.addAll(getTreeSet(parent));
		headSet.removeAll(tailSet);
		// Get all non-tree edges connecting the head and tail of this edge
		Collection joiningEdges = getConnectingEdges(headSet, tailSet);
		removeTreeEdge(edge);
		removeTreeEdge(tailParent);
		if (joiningEdges.isEmpty())
		{
			// If there are no edges connecting the head and tail, then
			// split into two trees
			treeNormalize(headSet);
			treeNormalize(tailSet);
			System.err.println("Remove tree edge " + edge);
			return null;
		}

		Object replacement = joiningEdges.iterator().next();
		Object source = getSource(replacement);
		Object joinParent = source;
		Object target = getTarget(replacement);
		Object child = target;
		if (!headSet.contains(source))
		{
			joinParent = child;
			child = source;
		}
		System.err.println("Replace tree edge " + edge + " with " + replacement);
		addTreeEdge(joinParent, child, replacement);
		return replacement;
	}

	private void treeNormalize(Set tree)
	{
		Iterator nodes = tree.iterator();
		int lowestRank = Integer.MAX_VALUE;
		Object lowestNode = null;
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			setTreeSet(node, tree);
			int rank = getRank(node);
			if (rank < lowestRank)
			{
				lowestRank = rank;
				lowestNode = node;
			}
		}
		if (lowestRank > getMinimumRank(lowestNode) && lowestRank != Integer.MAX_VALUE)
		{
			moveNodes(tree, lowestRank - getMinimumRank(lowestNode));
		}
	}

	/**
	 * @param edge
	 * @return <code>true</code> if the edge now forms part of the spanning tree
	 */
	protected boolean addEdge(Object edge)
	{
		if (!isTreeEdge(edge))
		{
			// System.err.println(edge);
			Object source = getSource(edge);
			Object target = getTarget(edge);
			Set sourceTreeSet = getTreeSet(source);
			Set targetTreeSet = getTreeSet(target);
			if (sourceTreeSet != targetTreeSet)
			{
				if (sourceTreeSet == null)
				{
					addTreeEdge(target, source, edge);
				}
				else if (targetTreeSet == null)
				{
					addTreeEdge(source, target, edge);
				}
				else
				{
					if (sourceTreeSet.size() > targetTreeSet.size())
					{
						addTreeEdge(source, target, edge);
					}
					else
					{
						addTreeEdge(target, source, edge);
					}
				}
			}
			else
			{
				if (sourceTreeSet == null)
				{
					addTreeEdge(source, target, edge);
					// Add any tight edges on source as well as target
					//treeAddChildren(source);
				}
				else
				{
					int edgeLength = getRank(target) - getRank(source);
					if (edgeLength < getMinimumEdgeLength(edge))
					{
						// TODO Need to break some other edge
						// Get path? Choose edge to remove?
						System.err.println("Edge " + edge + " less than min length!");
						//addTreeEdge(source, target, edge);
					}
					//else
					{
						addNonTreeEdge(edge);
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Creates an initial spanning tree, given a list of graph edges
	 * 
	 * @param edges
	 * @return the edges which weren't used to create the tree
	 */
	protected Collection createInitialTree(Iterator edges)
	{
		Collection treeEdges = new ArrayList();
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (addEdge(edge))
			{
				treeEdges.add(edge);
			}
		}
		return treeEdges;
	}

	/**
	 * @param edge
	 * @return <code>true</code> if the edge forms part of the graph spanning
	 *         tree
	 */
	protected boolean isTreeEdge(Object edge)
	{
		return getTreeParent(edge) != null;
	}
}