/*
 * Created on Feb 9, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.8 $
 */
public abstract class GraphSpanningTree
{
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
		if (isTreeEdge(edge))
		{
			replaceTreeEdge(edge);
		}
	}

	/**
	 * @param nodes
	 */
	protected void moveNodes(Set nodes, int change)
	{
		if (change == 0)
		{
			return;
		}
		System.err.println(this + ": Move nodes " + nodes + " by " + change);
		Iterator iterator = nodes.iterator();
		while (iterator.hasNext())
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
	protected void getTailSet(Object node, Set tailSet, Object lastEdge)
	{
		tailSet.add(node);
		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (edge != lastEdge && isTreeEdge(edge))
			{
				getTailSet(getNeighbour(node, edge), tailSet, edge);
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

	protected Comparator getComparator(final Object edge)
	{
		return new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				if (o1 == o2)
				{
					return 0;
				}

				int sourceRow1 = getRank(getSource(o1));
				int targetRow1 = getRank(getTarget(o1));
				int length1 = Math.abs(targetRow1 - sourceRow1);

				int sourceRow2 = getRank(getSource(o2));
				int targetRow2 = getRank(getTarget(o2));
				int length2 = Math.abs(targetRow2 - sourceRow2);

				if (length1 == length2)
				{
					if (targetRow1 == targetRow2)
					{
						return o1.toString().compareTo(o2.toString());
					}
					return targetRow1 - targetRow2;
				}
				return length1 - length2;
			}
		};
	}

	private Collection getReplacementEdges(Object edge)
	{
		Set tailSet = new HashSet();
		Set headSet = new HashSet();
		Object tail = getTarget(edge);
		getTailSet(tail, headSet, edge);
		tailSet.addAll(getTreeSet(tail));
		tailSet.removeAll(headSet);

		// Get all non-tree edges connecting the head and tail of this edge
		return getReplacementEdges(edge, headSet, tailSet);
	}

	/**
	 * Get all non-tree edges connecting a node in the head set to a node in the tail set
	 * 
	 * @param headSet
	 * @param tailSet
	 * @return list of edges connecting the head and tail sets
	 */
	private Collection getReplacementEdges(Object replaceEdge, Set headSet, Set tailSet)
	{
		Collection joiningEdges = new TreeSet(getComparator(replaceEdge));
		Iterator nodes;
		if (headSet.size() < tailSet.size())
		{
			nodes = headSet.iterator();
		}
		else
		{
			nodes = tailSet.iterator();
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
					if (headSet.contains(source))
					{
						if (tailSet.contains(target))
						{
							joiningEdges.add(edge);
						}
					}
					// else if (tailSet.contains(source))
					// {
					// if (headSet.contains(target))
					// {
					// joiningEdges.add(edge);
					// }
					// }
				}
			}
		}
		return joiningEdges;
	}

	/**
	 * @param parent
	 * @param child
	 * @param edge
	 */
	protected void addTreeEdge(Object parent, Object child, Object edge)
	{
		System.err.println(this + ": Add tree edge " + edge);
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
			if (parent == getSource(edge))
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
			getTailSet(child, tailSet, edge);
			Set headSet = new HashSet();
			headSet.addAll(getTreeSet(parent));
			headSet.removeAll(tailSet);

			if (parent == getSource(edge))
			{
				tightenEdge(edge, headSet, tailSet);
			}
			else
			{
				tightenEdge(edge, tailSet, headSet);
			}
		}
		// System.err.println(tree1);
		// treeAddChildren(child);
		assert getTreeParent(edge) != null : edge;
	}

	/**
	 * Minimizes the length of a given edge.
	 * 
	 * @param edge
	 */
	protected void tightenEdge(Object edge, Set sourceSet, Set targetSet)
	{
		// TODO Tidy up? Improve?
		assert (sourceSet != targetSet);
		assert (sourceSet != null);
		assert (targetSet != null);
		Object source = getSource(edge);
		Object target = getTarget(edge);

		int sourceRow = getRank(source);
		int sourceMinRow = getMinimumRank(source);
		int targetRow = getRank(target);
		int targetMinRow = getMinimumRank(target);
		int sourceMin = getMinimumTreeRank(sourceSet);
		int targetMin = getMinimumTreeRank(targetSet);
		int minimumEdgeLength = getMinimumEdgeLength(edge);
	
		if (targetRow - sourceRow != minimumEdgeLength)
		{
			if (sourceRow < sourceMinRow)
			{
				int sourceMaxRow = getMaximumRank(source);
				if (targetMinRow - sourceMaxRow > minimumEdgeLength)
				{
					System.err.println(this + ": Would remove tree edge " + edge + " here!");
					// treeNormalize(sourceSet);
					// TODO removeTreeEdge(edge);
				}
				// else
				{
					moveNodes(sourceSet, (targetRow - minimumEdgeLength) - sourceRow);
				}
			}
			else if (targetRow < targetMinRow || targetMinRow <= sourceRow)
			{
				moveNodes(targetSet, (sourceRow + minimumEdgeLength) - targetRow);
			}
			else
			{
				int sourceMaxRow = getMaximumRank(source);
				if (targetMinRow - sourceMaxRow > minimumEdgeLength)
				{
					System.err.println(this + ": Would remove tree edge " + edge + " here!");
					// TODO removeTreeEdge(edge);
				}
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
		// assert getRank(target) - getRank(source) ==
		// getMinimumEdgeLength(edge): this + ": " + edge;
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

		// String text = "";

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
					if (child == getTarget(edge))
					{
						value += getCutValue(edge, timeStamp) * direction;
					}
					else
					{
						value += getCutValue(edge, timeStamp) * -direction;
					}
					// text += "[" + edge + "]";
				}
			}
			cutValue += value;
			// text += value + " ";
		}
		//if (cutValue <= 0)
		{
			System.err.println(this + ": Cut " + cutValue + " for " + treeEdge);
		}
		// System.err.println(this + ": Cut " + cutValue + " for " + treeEdge +
		// " = " + text
		// + ", direction = " + direction + ", child = " + child);
		return cutValue;
	}

	/**
	 * @param treeEdges
	 */
	protected void optimiseTree(Collection treeEdges)
	{
		// TODO Search all relevant tree edges!
		boolean hasCutEdges = true;
		while (hasCutEdges)
		{
			Collection newEdges = new ArrayList();
			hasCutEdges = false;
			Iterator iterator = treeEdges.iterator();
			String timeStamp = "" + System.currentTimeMillis() + Math.random();
			while (iterator.hasNext())
			{
				Object edge = iterator.next();
				int cut = getCutValue(edge, timeStamp);
				if (cut < 0)
				{
					Collection replacements = getReplacementEdges(edge);
					if (!replacements.isEmpty())
					{
						replaceTreeEdge(edge, replacements.iterator().next());
						newEdges.add(replacements.iterator().next());
						hasCutEdges = true;
						iterator.remove();
						timeStamp = "" + System.currentTimeMillis() + Math.random();
					}
				}
			}
			treeEdges.addAll(newEdges);
		}
	}

	protected void replaceTreeEdge(Object edge, Object replacementEdge)
	{
		System.err.println(this + ": Replace tree edge " + edge + " with " + replacementEdge);
		Object parent = getTreeParent(edge);
		assert parent != null : edge;
		Object child = getNeighbour(parent, edge);

		removeTreeEdge(edge);
		removeTreeEdge(child);

		Object source = getSource(replacementEdge);
		Object joinParent = source;
		Object target = getTarget(replacementEdge);
		Object joinChild = target;

		addTreeEdge(joinParent, joinChild, replacementEdge);
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
		assert parent != null : edge;
		Object child = getNeighbour(parent, edge);

		Object tail = getTarget(edge);
		
		Set tailSet = new HashSet();
		Set headSet = new HashSet();
		getTailSet(tail, headSet, edge);
		headSet.addAll(getTreeSet(tail));
		headSet.removeAll(headSet);

		// Get all non-tree edges connecting the head and tail of this edge
		Collection joiningEdges = getReplacementEdges(edge, headSet, tailSet);
		removeTreeEdge(edge);
		removeTreeEdge(child);
		if (joiningEdges.isEmpty())
		{
			// If there are no edges connecting the head and tail, then
			// split into two trees
			normalizeTree(headSet);
			normalizeTree(tailSet);
			// System.err.println("Remove tree edge " + edge);
			return null;
		}

		Object replacement = joiningEdges.iterator().next();
		Object source = getSource(replacement);
		Object joinParent = source;
		Object target = getTarget(replacement);
		Object joinChild = target;
		if (!headSet.contains(source))
		{
			joinParent = joinChild;
			joinChild = source;
		}
		System.err.println(this + ": Replace tree edge " + edge + " with " + replacement
				+ " out of " + joiningEdges);
		addTreeEdge(joinParent, joinChild, replacement);
		return replacement;
	}

	private void normalizeTree(Set tree)
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
		if (lowestRank != Integer.MAX_VALUE && lowestRank > getMinimumRank(lowestNode))
		{
			moveNodes(tree, getMinimumRank(lowestNode) - lowestRank);
		}
	}
	
	private int getMinimumTreeRank(Set tree)
	{
		Iterator nodes = tree.iterator();
		int lowestRank = Integer.MAX_VALUE;
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			setTreeSet(node, tree);
			int rank = getRank(node) - getMinimumRank(node);
			if (rank < lowestRank)
			{
				lowestRank = rank;
			}
		}
		return lowestRank;
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
					// treeAddChildren(source);
				}
				else
				{
					int edgeLength = getRank(target) - getRank(source);
					if (edgeLength < getMinimumEdgeLength(edge))
					{
						// TODO Need to break some other edge
						// Get path? Choose edge to remove?
						// System.err.println("Edge " + edge + " less than min
						// length!");
						// addTreeEdge(source, target, edge);
					}
					// else
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
	 * @return <code>true</code> if the edge forms part of the graph spanning tree
	 */
	protected boolean isTreeEdge(Object edge)
	{
		return getTreeParent(edge) != null;
	}
}