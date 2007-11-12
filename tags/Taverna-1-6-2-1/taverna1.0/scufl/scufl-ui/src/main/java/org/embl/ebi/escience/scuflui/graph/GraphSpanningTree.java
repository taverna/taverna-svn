/*
 * Created on Feb 9, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.3 $
 */
public abstract class GraphSpanningTree
{
	protected Collection newEdges = new HashSet();
	protected Collection removedEdges = new HashSet();

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

	protected void removeEdge(Object edge)
	{
		// System.err.println(this + ": Remove edge " + edge);
		if (isTreeEdge(edge))
		{
			setTreeEdge(edge, false);
			removedEdges.add(edge);
			assert removedEdges.contains(edge) : edge;
		}
		newEdges.remove(edge);
	}

	protected abstract boolean isRemoved(Object edge);

	protected Collection getTreePath(Object source, Object target, Object lastEdge)
	{
		Iterator edges = getEdges(source);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (!edge.equals(lastEdge) && isTreeEdge(edge))
			{
				Object neighbour = getNeighbour(source, edge);
				Collection result;
				if (neighbour.equals(target))
				{
					result = new ArrayList();
				}
				else
				{
					result = getTreePath(neighbour, target, edge);
				}
				if (result != null)
				{
					result.add(edge);
					return result;
				}
			}
		}
		return null;
	}

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
		// System.err.println(this + ": Move nodes " + nodes + " by " + change);
		Iterator iterator = nodes.iterator();
		while (iterator.hasNext())
		{
			Object node = iterator.next();
			// assert !isRemoved(node) : node;
			shiftRank(node, change, nodes);
		}
	}

	protected abstract void shiftRank(Object node, int rankChange, Set set);

	protected void getEndSets(Object edge, Set sourceSet, Set targetSet)
	{
		getSet(getSource(edge), sourceSet, edge);
		getSet(getTarget(edge), targetSet, edge);
		assert targetSet.isEmpty() || !sourceSet.equals(targetSet);
		assert !sourceSet.contains(getTarget(edge));
		assert !targetSet.contains(getSource(edge));		
	}
	
	/**
	 * @param node
	 * @param tailSet
	 */
	private void getSet(Object node, Set tailSet, Object lastEdge)
	{
		if (!isRemoved(node))
		{
			tailSet.add(node);
			Iterator edges = getEdges(node);
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (edge != lastEdge && isTreeEdge(edge) && !isRemoved(edge))
				{
					getSet(getNeighbour(node, edge), tailSet, edge);
				}
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

	protected Comparator getComparator()
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

	private Collection getReplacementEdges(Object replaceEdge, boolean any)
	{
		Set sourceSet = new HashSet();
		Set targetSet = new HashSet();
		getEndSets(replaceEdge, sourceSet, targetSet);

		Collection joiningEdges = new HashSet();
		Iterator nodes = targetSet.iterator();
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			Iterator edges = getEdges(node);
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (!edge.equals(replaceEdge) && !isTreeEdge(edge) && !isRemoved(edge))
				{
					Object source = getSource(edge);
					Object target = getTarget(edge);
					if (targetSet.contains(source) && !targetSet.contains(target))
					{
						joiningEdges.add(edge);
					}
					else if (any && (!targetSet.contains(source) && targetSet.contains(target)))
					{
						joiningEdges.add(edge);
					}
				}
			}
		}
		nodes = sourceSet.iterator();
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			Iterator edges = getEdges(node);
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (!edge.equals(replaceEdge) && !isTreeEdge(edge) && !isRemoved(edge))
				{
					Object source = getSource(edge);
					Object target = getTarget(edge);
					if (sourceSet.contains(target) && !sourceSet.contains(source))
					{
						joiningEdges.add(edge);
					}
					else if (any && (!sourceSet.contains(target) && sourceSet.contains(source)))
					{
						joiningEdges.add(edge);
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

		assert !isRemoved(getSource(edge)): edge;
		assert !isRemoved(getTarget(edge)): edge;		
		
		if (!sourceTree.equals(targetTree))
		{
			assert !sourceTree.contains(getTarget(edge)) : edge;
			assert !targetTree.contains(getSource(edge)) : edge;
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
			getEndSets(edge, sourceSet, targetSet);

			if (!tightenEdge(edge, sourceSet, targetSet))
			{
				return false;
			}
		}
		setTreeEdge(edge, true);
		assert isTreeEdge(edge) : edge;
		// System.err.println(this + ": Added tree edge " + edge);
		return true;
	}

	private int getMaxRankMove(Set set, boolean negative)
	{
		Iterator nodes = set.iterator();
		int move = Integer.MAX_VALUE;
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			try
			{
				if (negative)
				{
					move = Math.min(move, getMaxRankMoveNegative(set, node));
				}
				else
				{
					move = Math.min(move, getMaxRankMovePositive(set, node));
				}
			}
			catch (NullPointerException e)
			{
				// Node doesn't exist, ignore
				// System.err.println("Node doesn't exist");
			}
		}
		if (negative)
		{
			move = -move;
		}
		return move;
	}

	protected abstract int getMaxRankMoveNegative(Set set, Object node);

	protected abstract int getMaxRankMovePositive(Set set, Object node);

	protected abstract boolean isValid(Object edge);

	/**
	 * Minimizes the length of a given edge.
	 * 
	 * @param edge
	 */
	protected boolean tightenEdge(Object edge, Set sourceSet, Set targetSet)
	{
		assert !sourceSet.equals(targetSet);

		int slack = getSlack(edge);
		if (slack != 0)
		{
			int sourceMove = 0;
			int targetMove = 0;
			if (slack < 0)
			{
				// Edge too short, try to move source set away to compensate
				sourceMove = Math.max(slack, getMaxRankMove(sourceSet, true));
				if (sourceMove != slack)
				{
					targetMove = Math.min(sourceMove - slack, getMaxRankMove(targetSet, false));
				}
			}
			else
			{
				// Edge too long, try to move target set closer to compensate
				targetMove = Math.max(-slack, getMaxRankMove(targetSet, true));
				if (targetMove != -slack)
				{
					sourceMove = Math.min(slack + targetMove, getMaxRankMove(sourceSet, false));
				}
			}
			if (sourceMove - targetMove != slack)
			{
				return false;
			}
			moveNodes(sourceSet, sourceMove);
			moveNodes(targetSet, targetMove);
			assert getSlack(edge) == 0 : edge + ": " + "sm=" + sourceMove + ", tm=" + targetMove
					+ ", slack=" + getSlack(edge) + " (" + slack + ")";
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
		// System.err.println(this + ": Optimise " + treeEdges);
		boolean hasCutEdges = true;
		while (hasCutEdges)
		{
			// ArrayList newEdges = new ArrayList();
			hasCutEdges = false;
			ListIterator iterator = treeEdges.listIterator();
			String timeStamp = "" + System.currentTimeMillis() + Math.random();
			while (iterator.hasNext())
			{
				Object edge = iterator.next();
				if (!isTreeEdge(edge))
				{
					// System.err.println(this + ": Can't optimise non-tree edge " + edge + "!");
					iterator.remove();
				}
				else
				{
					Object cutEdge = findCutEdge(getSource(edge), edge, timeStamp);
					if (cutEdge != null)
					{
						List replacements = new ArrayList(getReplacementEdges(cutEdge, false));
						if (!replacements.isEmpty())
						{
							Collections.sort(replacements, new Comparator()
							{
								public int compare(Object o1, Object o2)
								{
									int edgeWeight1 = getEdgeWeight(o1);
									int edgeWeight2 = getEdgeWeight(o2);
									if (edgeWeight1 != edgeWeight2)
									{
										return edgeWeight2 - edgeWeight1;
									}
									return Math.abs(getSlack(o1)) - Math.abs(getSlack(o2));
								}
							});
							Object replacement = replaceEdge(cutEdge, replacements);
							if (replacement != null)
							{
								// System.err.println(this + ": Replaced tree edge " + cutEdge + "
								// with " + replacement);
								hasCutEdges = true;
								timeStamp = "" + System.currentTimeMillis() + Math.random();
								iterator.remove();
								iterator.add(cutEdge);
							}
						}
					}
				}
			}
		}
	}

	protected Object replaceEdge(Object edge, Collection replacementEdges)
	{
		setTreeEdge(edge, false);
		Object previousEdge = getPreviousReplacement(edge);

		Iterator replacements = replacementEdges.iterator();
		while (replacements.hasNext())
		{
			Object replacementEdge = replacements.next();
			if (!replacementEdge.equals(previousEdge) && addTreeEdge(replacementEdge))
			{
				// System.err.println(this + ": Replace tree edge " + edge + " with "
				// + replacementEdge + " out of " + replacementEdges);
				setPreviousReplacement(edge, replacementEdge);
				return replacementEdge;
			}
		}
		// System.err.println(this + ": Failed to replace " + edge + " with any of "
		// + replacementEdges);
		setTreeEdge(edge, true);
		// boolean added = addTreeEdge(edge);
		// assert added : edge;
		return null;
	}

	protected abstract void setPreviousReplacement(Object edge, Object replacementEdge);

	protected abstract Object getPreviousReplacement(Object edge);

	protected void normalizeTree(Set tree)
	{
		if (tree.isEmpty())
		{
			return;
		}
		int change = getMaxRankMove(tree, true);
		Iterator iterator = tree.iterator();
		while (iterator.hasNext())
		{
			Object node = iterator.next();
			setTreeSet(node, tree);
			try
			{
				shiftRank(node, change, tree);
			}
			catch (NullPointerException e)
			{
				// Node no longer exists, ignore
				System.err.println("Node doesn't exist");
			}
		}
	}

	/**
	 * @param edge
	 * @return a new edge that has been added to the spanning tree
	 */
	protected Object addEdge(Object edge)
	{
		assert !isRemoved(edge) : edge;
		if (!isTreeEdge(edge))
		{
			// System.err.println(edge);
			Object source = getSource(edge);
			Object target = getTarget(edge);
			Set sourceTreeSet = getTreeSet(source);
			Set targetTreeSet = getTreeSet(target);
			if (sourceTreeSet != targetTreeSet)
			{
				if (addTreeEdge(edge))
				{
					assert isValid(edge) : edge;
					return edge;
				}
			}
			else if (!isValid(edge))
			{
				Collection path = getTreePath(source, target, null);
				if (path != null && !path.isEmpty())
				{
					Collection replace = new ArrayList();
					replace.add(edge);
					Object result = null;
					Iterator pathIterator = path.iterator();
					while(result == null && pathIterator.hasNext())
					{
						result = replaceEdge(pathIterator.next(), replace);
					}
					return result;
				}
				if (addTreeEdge(edge))
				{
					assert isValid(edge) : edge;
					return edge;
				}
			}
			else
			{
				addNonTreeEdge(edge);
				return edge;
			}
		}
		// System.err.println(this + ": Failed to add edge " + edge);
		return null;
	}

	/**
	 * Creates an initial spanning tree, given a list of graph edges
	 * 
	 * @return the edges which weren't used to create the tree
	 */
	protected List createInitialTree()
	{
		List edgeList = new ArrayList(newEdges);
		newEdges.clear();

		Collections.sort(edgeList, getComparator());
		// System.err.println(this + ": Add edges " + edgeList);

		List treeEdges = new ArrayList();
		Iterator edges = edgeList.iterator();
		while (edges.hasNext())
		{
			Object edge = edges.next();
			assert !isTreeEdge(edge) : edge;
			assert !isRemoved(edge) : edge;
			Object addedEdge = addEdge(edge);
			if (addedEdge != null)
			{
				if (isTreeEdge(addedEdge))
				{
					treeEdges.add(addedEdge);
					// System.err.println(this + ": Added tree edge " + addedEdge);
				}
				edgeList.remove(addedEdge);
				edges = edgeList.iterator();
			}
		}
		edges = edgeList.iterator();
		while (edges.hasNext())
		{
			Object edge = edges.next();
			assert !isTreeEdge(edge) : edge;
			assert !isRemoved(edge) : edge;
			Object addedEdge = addEdge(edge);
			if (addedEdge != null)
			{
				if (isTreeEdge(addedEdge))
				{
					treeEdges.add(addedEdge);
					// System.err.println(this + ": Added tree edge " + addedEdge);
				}
				edgeList.remove(addedEdge);
				edges = edgeList.iterator();
			}
		}
		
		assert edgeList.isEmpty() : edgeList;
		return treeEdges;
	}

	protected void removeEdges()
	{
		// System.err.println(this + ": Remove edges " + removedEdges);
		Iterator removed = removedEdges.iterator();
		while (removed.hasNext())
		{
			Set sourceSet = new HashSet();
			Set targetSet = new HashSet();
			
			getEndSets(removed.next(), sourceSet, targetSet);
			
			normalizeTree(sourceSet);
			normalizeTree(targetSet);
		}
		removed = removedEdges.iterator();
		while (removed.hasNext())
		{
			newEdges.addAll(getReplacementEdges(removed.next(), true));
		}
		removedEdges.clear();
	}

	/**
	 * @param edge
	 * @return <code>true</code> if the edge forms part of the graph spanning tree
	 */
	protected abstract boolean isTreeEdge(Object edge);

	protected abstract void setTreeEdge(Object edge, boolean isTreeEdge);
}
