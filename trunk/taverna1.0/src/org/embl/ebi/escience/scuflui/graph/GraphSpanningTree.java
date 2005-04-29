/*
 * Created on Feb 9, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.15 $
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
	 * @param edge
	 */
	protected abstract void addNonTreeEdge(Object edge);

	/**
	 * @param node
	 * @return the set of nodes of the spanning tree
	 */
	protected abstract Set getTreeSet(Object node);

	/**
	 * @param edge
	 * @return the difference between the current edge length and the minimum length
	 */
	protected abstract int getSlack(Object edge);

	/**
	 * @param node
	 * @param treeSet
	 */
	protected abstract void setTreeSet(Object node, Set treeSet);

	protected abstract void removeEdge(Object edge);

	/**
	 * @param nodes
	 */
	protected void moveNodes(Set nodes, int change)
	{
		assert !nodes.isEmpty();
		if (change == 0)
		{
			return;
		}
		//System.err.println(this + ": Move nodes " + nodes + " by " + change);
		Iterator iterator = nodes.iterator();
		while (iterator.hasNext())
		{
			try
			{
				shiftRank(iterator.next(), change);
			}
			catch (NullPointerException e)
			{
				// Node no longer exists, ignore
				System.err.println("Node doesn't exist");
			}
		}
	}

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
		getTailSet(getTarget(edge), headSet, edge);
		getTailSet(getSource(edge), tailSet, edge);

		// Get all non-tree edges connecting the head and tail of this edge
		return getReplacementEdges(edge, headSet, tailSet, false);
	}

	/**
	 * Get all non-tree edges connecting a node in the head set to a node in the tail set
	 * 
	 * @param headSet
	 * @param tailSet
	 * @return list of edges connecting the head and tail sets
	 */
	private Collection getReplacementEdges(Object replaceEdge, Set headSet, Set tailSet, boolean any)
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
					else if (any && tailSet.contains(source))
					{
						if (headSet.contains(target))
						{
							joiningEdges.add(edge);
						}
					}
				}
			}
		}
		return joiningEdges;
	}

	/**
	 * @param edge
	 */
	protected boolean addTreeEdge(Object edge)
	{
		Set sourceTree = getTreeSet(getSource(edge));
		Set targetTree = getTreeSet(getTarget(edge));

		if (sourceTree != targetTree)
		{
			if (!tightenEdge(edge, sourceTree, targetTree))
			{
				return false;
			}

			if (targetTree.size() > sourceTree.size())
			{
				Set temp = sourceTree;
				sourceTree = targetTree;
				targetTree = temp;
			}
			sourceTree.addAll(targetTree);
			Iterator nodes = targetTree.iterator();
			while (nodes.hasNext())
			{
				setTreeSet(nodes.next(), sourceTree);
			}
		}
		else
		{
			Set sourceSet = new HashSet();
			Set targetSet = new HashSet();
			getTailSet(getSource(edge), sourceSet, edge);
			getTailSet(getTarget(edge), targetSet, edge);

			if (!tightenEdge(edge, sourceSet, targetSet))
			{
				return false;
			}
		}
		setTreeSet(edge, sourceTree);
		assert isTreeEdge(edge) : edge;
		return true;
	}

	private int getMaxRankMove(Set targetSet, boolean negative)
	{
		Iterator nodes = targetSet.iterator();
		int move = Integer.MAX_VALUE;
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			try
			{
				if (negative)
				{
					move = Math.min(move, getMaxRankMoveNegative(node));
				}
				else
				{
					move = Math.min(move, getMaxRankMovePositive(node));
				}
			}
			catch (NullPointerException e)
			{
				// Node doesn't exist, ignore
				System.err.println("Node doesn't exist");
			}
		}
		if (negative)
		{
			move = -move;
		}
		return move;
	}

	protected abstract int getMaxRankMoveNegative(Object node);

	protected abstract int getMaxRankMovePositive(Object node);

	/**
	 * Minimizes the length of a given edge.
	 * 
	 * @param edge
	 */
	protected boolean tightenEdge(Object edge, Set sourceSet, Set targetSet)
	{
		assert (sourceSet != targetSet);
		assert (sourceSet != null);
		assert (targetSet != null);

		int slack = getSlack(edge);
		if (slack != 0)
		{
			int sourceMove = 0;
			int targetMove = 0;
			if (slack < 0)
			{
				// Edge too short, try to move source set away to compensate
				sourceMove = Math.max(slack, getMaxRankMove(sourceSet, true));
				if(sourceMove != slack)
				{
					targetMove = Math.min(sourceMove - slack, getMaxRankMove(targetSet, false));
				}
			}
			else
			{
				// Edge too long, try to move target set closer to compensate
				targetMove = Math.max(-slack, getMaxRankMove(targetSet, true));
				if(targetMove != -slack)
				{
					sourceMove = Math.min(slack + targetMove, getMaxRankMove(sourceSet, false));
				}
			}
			if (sourceMove - targetMove != slack)
			{
				return false;
			}
			try
			{
				moveNodes(sourceSet, sourceMove);
				moveNodes(targetSet, targetMove);
			}
			catch (AssertionError e)
			{
				System.err.println(e);
			}
			// assert getSlack(edge) == 0;
		}
		return true;
	}

	protected abstract Integer getCutValue(Object treeEdge, String timeStamp);

	protected abstract void setCutValue(Object treeEdge, String timeStamp, int cutValue);

	/**
	 * @param treeEdge
	 * @param timeStamp
	 * @return the cut value for the given tree edge
	 */
	protected Object findCutEdge(Object node, Object treeEdge, String timeStamp)
	{
		assert isTreeEdge(treeEdge) : treeEdge;

		if (getCutValue(treeEdge, timeStamp) != null)
		{
			return null;
		}

		int direction = 1;
		if (node == getTarget(treeEdge))
		{
			direction = -1;
		}

		// String text = "";

		Iterator edges = getEdges(node);
		int cutValue = 0;
		while (edges.hasNext())
		{
			Object edge = edges.next();
			int value = getEdgeWeight(edge);
			if (edge != treeEdge)
			{
				Object target = getTarget(edge);
				Object neighbour = target;
				if (node == target)
				{
					value *= -direction;
					neighbour = getSource(edge);
				}
				else
				{
					value *= direction;
				}

				if (isTreeEdge(edge))
				{
					Object result = findCutEdge(neighbour, edge, timeStamp);
					if (result != null)
					{
						return result;
					}

					if (node == target)
					{
						value += getCutValue(edge, timeStamp).intValue() * direction;
					}
					else
					{
						value += getCutValue(edge, timeStamp).intValue() * -direction;
					}
					// text += "[" + edge + "]";
				}
			}
			cutValue += value;
			// text += value + " ";
		}
		setCutValue(treeEdge, timeStamp, cutValue);

		if (cutValue < 0)
		{
			// System.err.println(this + ": Cut " + cutValue + " for " + treeEdge);
			return treeEdge;
		}
		return null;
	}

	/**
	 * @param treeEdges
	 */
	protected void optimiseTree(List treeEdges)
	{
		boolean hasCutEdges = true;
		while (hasCutEdges)
		{
			ArrayList newEdges = new ArrayList();
			hasCutEdges = false;
			ListIterator iterator = treeEdges.listIterator();
			String timeStamp = "" + System.currentTimeMillis() + Math.random();
			while (iterator.hasNext())
			{
				Object edge = iterator.next();
				if (!isTreeEdge(edge))
				{
					iterator.remove();
				}
				else
				{
					Object cutEdge = findCutEdge(getSource(edge), edge, timeStamp);
					if (cutEdge != null)
					{
						Collection replacements = getReplacementEdges(cutEdge);
						if (!replacements.isEmpty())
						{
							Object replacement = replaceEdge(cutEdge, replacements);
							if (replacement != null)
							{
								//System.err.println(this + ": Replaced tree edge " + cutEdge + " with " + replacement);
								newEdges.add(replacement);
								hasCutEdges = true;
								timeStamp = "" + System.currentTimeMillis() + Math.random();
								iterator.add(cutEdge);
							}
						}
					}
				}
			}
			treeEdges.addAll(newEdges);
		}
	}

	protected Object replaceEdge(Object edge, Collection replacementEdges)
	{
		removeEdge(edge);

		Iterator replacements = replacementEdges.iterator();
		while (replacements.hasNext())
		{
			Object replacementEdge = replacements.next();
			if (addTreeEdge(replacementEdge))
			{
				// System.err.println(this + ": Replace tree edge " + edge + " with " +
				// replacementEdge);
				return replacementEdge;
			}
		}
		addTreeEdge(edge);
		return null;
	}

	/**
	 * Attempts to replace a edge in the spanning tree with a
	 * 
	 * @param edge
	 *            the tree edge to replace
	 */
	protected Object replaceEdge(Object edge)
	{
		if (isTreeEdge(edge))
		{
			Set tailSet = new HashSet();
			Set headSet = new HashSet();
			getTailSet(getTarget(edge), tailSet, edge);
			getTailSet(getSource(edge), headSet, edge);

			// Get all non-tree edges connecting the head and tail of this edge
			Collection joiningEdges = getReplacementEdges(edge, headSet, tailSet, true);
			if (joiningEdges.isEmpty())
			{
				// If there are no edges connecting the head and tail, then
				// split into two trees
				normalizeTree(headSet);
				normalizeTree(tailSet);
				// System.err.println(this + ": Remove tree edge " + edge);
				return null;
			}

			return replaceEdge(edge, joiningEdges);
		}
		removeEdge(edge);
		return null;
	}

	private void normalizeTree(Set tree)
	{
		moveNodes(tree, getMaxRankMove(tree, true));
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
				return addTreeEdge(edge);
			}
			// int edgeLength = getRank(target) - getRank(source);
			// if (edgeLength < getMinimumEdgeLength(edge))
			// {
			// // TODO Need to break some other edge
			// // Get path? Choose edge to remove?
			// System.err.println("Edge " + edge + " less than min
			// length!");
			// addTreeEdge(source, target, edge);
			// }
			// else
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates an initial spanning tree, given a list of graph edges
	 * 
	 * @param edgeList
	 * @return the edges which weren't used to create the tree
	 */
	protected List createInitialTree(Collection edgeList)
	{
		List treeEdges = new ArrayList();
		boolean addedNew = true;
		while (addedNew)
		{
			Iterator edges = edgeList.iterator();
			addedNew = false;
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (addEdge(edge))
				{
					//System.err.println(this + ": Added tree edge " + edge);
					treeEdges.add(edge);
					addedNew = true;
					edges.remove();
				}
			}
		}

		Iterator edges = edgeList.iterator();
		while (edges.hasNext())
		{
			// System.err.println(this + ": Added non-tree edge " + edge);
			addNonTreeEdge(edges.next());
		}
		return treeEdges;
	}

	/**
	 * @param edge
	 * @return <code>true</code> if the edge forms part of the graph spanning tree
	 */
	protected abstract boolean isTreeEdge(Object edge);
}