/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * Manages the layout of a directed graph. Listens for change events on the
 * graph to be able to update as the graph changes.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.4 $
 */
public class LayoutManager
{
	private class EdgeList extends ArrayList
	{
		public boolean add(Object edge)
		{
			int sourceRow = rows.getRow(GraphUtilities.getSourceNode(model, edge));
			int targetRow = rows.getRow(GraphUtilities.getTargetNode(model, edge));
			int length = targetRow - sourceRow;
			for (int index = 0; index < size(); index++)
			{
				if (edge.equals(get(index)))
				{
					return true;
				}
				int currentSourceRow = rows.getRow(GraphUtilities.getSourceNode(model, get(index)));
				int currentTargetRow = rows.getRow(GraphUtilities.getTargetNode(model, get(index)));
				int currentLength = currentTargetRow - currentSourceRow;
				if(length == currentLength)
				{
					if(sourceRow < currentSourceRow)
					{
						add(index, edge);
						return true;
					}
				}
				else if (length < currentLength)
				{
					add(index, edge);
					return true;
				}
			}
			return super.add(edge);
		}
	}

	private static final String EDGE_PARENT = "tree edge parent";
	private static final String TREE_SET = "tree set";
	private static final String CUT_EDGE = "cut edge";
	private static final String CUT_TIME_STAMP = "cut time stamp";

	GraphRows rows;
	GraphModel model;

	/**
	 */
	public LayoutManager(GraphModel model, CellMapper mapper)
	{
		this.model = model;
		rows = new GraphRows(model, mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
	 */
	public void layout(GraphModelEvent.GraphModelChange change)
	{
		if (model.getRootCount() == 0)
		{
			rows.clear();
			return;
		}

		List edges = new EdgeList();

		if (change.getRemoved() != null)
		{
			Object[] removed = change.getRemoved();
			for (int index = 0; index < removed.length; index++)
			{
				if (model.isEdge(removed[index]))
				{
					if (isTreeEdge(removed[index]))
					{
						treeRemoveEdge(removed[index], null);
					}
					else
					{
						rows.remove(removed[index]);
					}
				}
				else
				{
					if (!model.isPort(removed[index]))
					{
						rows.remove(removed[index]);
						Set treeSet = getTreeSet(GraphUtilities.getRoot(model, removed[index]));
						if (treeSet != null)
						{
							treeSet.remove(removed[index]);
						}
					}
				}
			}
		}

		if (change.getInserted() != null)
		{
			Object[] inserted = change.getInserted();
			for (int index = 0; index < inserted.length; index++)
			{
				if (model.isEdge(inserted[index]))
				{
					// TODO Check both ends aren't null
					edges.add(inserted[index]);
				}
				else
				{
					Object root = GraphUtilities.getRoot(model, inserted[index]);
					if (root == inserted[index])
					{
						rows.getRow(root);
					}
				}
			}
		}

		while (!edges.isEmpty())
		{
			Object edge = edges.remove(0);
			if (!isTreeEdge(edge))
			{
				// System.err.println(edge);
				Object source = GraphUtilities.getSourceNode(model, edge);
				Object target = GraphUtilities.getTargetNode(model, edge);
				Set sourceTreeSet = getTreeSet(source);
				Set targetTreeSet = getTreeSet(target);
				if (sourceTreeSet != targetTreeSet)
				{
					if (sourceTreeSet == null)
					{
						treeAddEdge(target, source, edge);
					}
					else if (targetTreeSet == null)
					{
						treeAddEdge(source, target, edge);
					}
					else
					{
						if (sourceTreeSet.size() > targetTreeSet.size())
						{
							treeAddEdge(source, target, edge);
						}
						else
						{
							treeAddEdge(target, source, edge);
						}
					}
				}
				else
				{
					if (sourceTreeSet == null)
					{
						treeAddEdge(source, target, edge);
						// Add any tight edges on source as well as target
						treeAddChildren(source);
					}
					else
					{
						int edgeLength = rows.getRow(target) - rows.getRow(source);
						if (edgeLength < 1)
						{
							treeAddEdge(source, target, edge);
							//System.err.println("New tree edge! Existing edges may be broken");
						}
						else if (edgeLength > 1)
						{
							// Decide if this non-tree edge should be replaced
							// by a tree edge in order to make the graph optimal
							Iterator path = getPath(source, target).iterator();
							int lowestCut = Integer.MAX_VALUE;
							Object cutEdge = null;
							String timeStamp = new Date().toString();
							while (path.hasNext())
							{
								Object pathEdge = path.next();
								int cut = getCutValue(pathEdge, timeStamp);
								if (cut < lowestCut)
								{
									lowestCut = cut;
									cutEdge = pathEdge;
								}
							}
//							if (lowestCut < 0)
//							{
//								treeRemoveEdge(cutEdge, edge);
//							}
//							else
//							{
//								// Non-tree edge, so add as virtual node chain.
								rows.addEdge(edge);
//							}
						}
					}
				}
			}
		}
		
		rows.calculateBounds();
	}

	private int getCutValue(Object cutEdge, String timeStamp)
	{
		// TODO Otimise this. Currently calculates the same edges repeatedly
		Map attributes = model.getAttributes(cutEdge);
		if(timeStamp.equals(attributes.get(CUT_TIME_STAMP)))
		{
			return ((Integer)attributes.get(CUT_EDGE)).intValue();
		}
		attributes.put(CUT_TIME_STAMP, timeStamp);
		
		Object parent = treeGetParent(cutEdge);
		Object child = GraphUtilities.getSourceNode(model, cutEdge);
		int direction = -1;
		if (child == parent)
		{
			child = GraphUtilities.getTargetNode(model, cutEdge);
			direction = 1;
		}

		String text = "";

		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { child }).iterator();
		int cutValue = 0;
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (edge == cutEdge)
			{
				cutValue += 1;
				text += 1 + " ";
			}
			else
			{
				if (isTreeEdge(edge))
				{
					int value = getCutValue(edge, timeStamp);
					cutValue += value;
					text += value + "[" + edge + "] ";
				}

				if (child == GraphUtilities.getTargetNode(model, edge))
				{
					cutValue += direction;
					text += direction + " ";
				}
				else
				{
					cutValue -= direction;
					text += -direction + " ";
				}
			}
		}
		//System.err.println("Cut for " + cutEdge + " = " + cutValue + " = " + text);
		attributes.put(CUT_EDGE, new Integer(cutValue));
		return cutValue;
	}

	private Set getPath(Object node1, Object node2)
	{
		Set path = new HashSet();
		Object tempNode = node1;
		Object parentEdge;
		while ((parentEdge = treeGetParent(tempNode)) != null)
		{
			path.add(parentEdge);
			tempNode = treeGetParent(parentEdge);
		}

		tempNode = node2;
		while ((parentEdge = treeGetParent(tempNode)) != null)
		{
			if (path.contains(parentEdge))
			{
				path.remove(parentEdge);
			}
			else
			{
				path.add(parentEdge);
			}
			tempNode = treeGetParent(parentEdge);
		}

		//Iterator pathIterator = path.iterator();
		//String text = "Tree path between " + node1 + " & " + node2 + "= ";
		//while(pathIterator.hasNext())
		//{
			//text = text + ", " + pathIterator.next();
		//}
		//System.err.println(text);
		return path;
	}

	private void treeMinimizeEdgeLength(Object edge, Object fixedNode)
	{
		Object source = GraphUtilities.getSourceNode(model, edge);
		Object target = GraphUtilities.getTargetNode(model, edge);

		int sourceRow = rows.getRow(source);
		int sourceMinRow = rows.getMinimumRow(source);
		int targetRow = rows.getRow(target);
		int targetMinRow = rows.getMinimumRow(target);

		if (targetRow - sourceRow != 1)
		{
			if (target == fixedNode || sourceRow < sourceMinRow)
			{
				int sourceMaxRow = rows.getMaximumRow(source);
				if (targetMinRow - sourceMaxRow > 1)
				{
					//System.err.println("Rearrange & remove tree edge " + edge);
					treeRemoveParent(edge);
				}
				else
				{
					treeMoveNode(source, edge, targetRow - 1);
				}
			}
			else if (source == fixedNode || targetRow < targetMinRow || targetMinRow <= sourceRow)
			{
				treeMoveNode(target, edge, sourceRow + 1);
			}
			else
			{
				int sourceMaxRow = rows.getMaximumRow(source);
				if (targetMinRow - sourceMaxRow > 1)
				{
					treeRemoveParent(edge);
				}
				else
				{
					if (sourceRow < sourceMaxRow)
					{
						treeMoveNode(source, edge, targetRow - 1);
					}
					else
					{
						treeMoveNode(target, edge, sourceRow + 1);
					}
				}
			}
		}
	}

	/**
	 * @param source
	 * @param newRow
	 */
	private void treeMoveNode(Object node, Object previousEdge, int row)
	{
		rows.setRow(node, row);
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { node }).iterator();
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (edge != previousEdge && isTreeEdge(edge))
			{
				treeMinimizeEdgeLength(edge, node);
			}
		}
	}

	private Set getTreeSet(Object node)
	{
		Map attributes = model.getAttributes(node);
		return (Set) attributes.get(TREE_SET);
	}

	private void setTreeSet(Object node, Set treeSet)
	{
		Map parentAttr = model.getAttributes(node);
		if (parentAttr != null)
		{
			parentAttr.put(TREE_SET, treeSet);
		}
	}

	private void getTailSet(Object node, Set tailSet)
	{
		tailSet.add(node);
		Object parent = treeGetParent(node);
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { node }).iterator();
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (edge != parent && isTreeEdge(edge))
			{
				getTailSet(GraphUtilities.getNeighbour(model, node, edge), tailSet);
			}
		}
	}

	private List getConnectingEdges(Set tree1, Set tree2)
	{
		// Get all non-tree edges connecting the head and tail of this edge
		List joiningEdges = new EdgeList();
		for (int index = 0; index < model.getRootCount(); index++)
		{
			Object otherEdge = model.getRootAt(index);
			if (model.isEdge(otherEdge))
			{
				if (!isTreeEdge(otherEdge))
				{
					Object source = GraphUtilities.getSourceNode(model, otherEdge);
					Object target = GraphUtilities.getTargetNode(model, otherEdge);
					if (tree1.contains(source))
					{
						if (tree2.contains(target))
						{
							joiningEdges.add(otherEdge);
						}
					}
					else if (tree2.contains(source))
					{
						if (tree1.contains(target))
						{
							joiningEdges.add(otherEdge);
						}
					}
				}
			}
		}
		return joiningEdges;
	}

	private Object treeGetParent(Object node)
	{
		Map attributes = model.getAttributes(node);
		if (attributes != null)
		{
			return attributes.get(EDGE_PARENT);
		}
		return null;
	}

	// private boolean isParent(Object parent, Object node)
	// {
	// Object nodeParent = getParent(node);
	// if(node.equals(nodeParent))
	// {
	// return true;
	// }
	// else if(nodeParent == null)
	// {
	// return false;
	// }
	// else
	// {
	// return isParent(parent, nodeParent);
	// }
	// }

	private boolean isTreeEdge(Object edge)
	{
		Map attributes = model.getAttributes(edge);
		if (attributes != null)
		{
			Object treeEdgeParent = attributes.get(EDGE_PARENT);
			if (treeEdgeParent != null)
			{
				return true;
			}
		}
		return false;
	}

	private void treeAddChildren(Object parent)
	{
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { parent }).iterator();
		while (edges.hasNext())
		{
			// Follow all tight edges
			Object edge = edges.next();
			if (!isTreeEdge(edge))
			{
				Object child = GraphUtilities.getNeighbour(model, parent, edge);
				if (getTreeSet(child) == null)
				{
					int parentRow = rows.getRow(parent);
					int neighbourRow = rows.getRow(child);
					if (Math.abs(neighbourRow - parentRow) == 1)
					{
						treeAddEdge(parent, child, edge);
					}
				}
			}
		}
	}

	/**
	 * @param parent
	 * @param child
	 * @param edge
	 */
	private void treeAddEdge(Object parent, Object child, Object edge)
	{
		//System.err.println("Adding tree edge " + edge);
		treeMinimizeEdgeLength(edge, null);
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
			tree1.add(child);
			setTreeSet(child, tree1);
		}
		else
		{
			if (tree1 != tree2)
			{
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
		}

		Map attributes = model.getAttributes(edge);
		attributes.put(EDGE_PARENT, parent);
		GraphConstants.setLineColor(attributes, Color.RED);

		treeSetParent(child, edge);
		treeAddChildren(child);
	}

	private void treeRemoveEdge(Object edge, Object replacement)
	{
		//System.err.println("Remove tree edge " + edge);
		Object parent = treeGetParent(edge);
		Object tailParent = GraphUtilities.getNeighbour(model, parent, edge);
		if (!model.contains(tailParent))
		{
			Object target = GraphUtilities.getTargetNode(model, edge);
			if (model.contains(target))
			{
				treeNormalize(getTreeSet(target));
			}
			return;
		}
		
		treeRemoveParent(edge);
		treeRemoveParent(tailParent);

		Set tailSet = new HashSet();
		getTailSet(tailParent, tailSet);
		Set headSet = new HashSet();
		headSet.addAll(getTreeSet(parent));
		headSet.removeAll(tailSet);		
		if(replacement == null)
		{
			// Get all non-tree edges connecting the head and tail of this edge
			List joiningEdges = getConnectingEdges(headSet, tailSet);
			if (joiningEdges.isEmpty())
			{
				// If there are no edges connecting the head and tail, then split
				// into two trees
				treeNormalize(headSet);
				treeNormalize(tailSet);
				return;
			}
			// Otherwise, replace this tree edge with a non-tree edge
			replacement = joiningEdges.get(0);
		}
		Object source = GraphUtilities.getSourceNode(model, replacement);
		Object joinParent = source;
		Object target = GraphUtilities.getTargetNode(model, replacement);
		Object child = target;
		if (!headSet.contains(source))
		{
			joinParent = child;
			child = source;
		}
		treeAddEdge(joinParent, child, replacement);		
	}

	private void treeNormalize(Set tree)
	{
		Iterator nodes = tree.iterator();
		Object lowestNode = null;
		int lowestRow = Integer.MAX_VALUE;
		while (nodes.hasNext())
		{
			Object node = nodes.next();
			setTreeSet(node, tree);
			int row = rows.getRow(node);
			if (row < lowestRow && model.contains(node))
			{
				lowestRow = row;
				lowestNode = node;
			}
		}
		if (lowestRow > 0 && lowestRow != Integer.MAX_VALUE)
		{
			treeMoveNode(lowestNode, null, 0);
		}
	}

	private void treeRemoveParent(Object edge)
	{
		Map attributes = model.getAttributes(edge);
//		if(model.isEdge(edge))
//		{
//			GraphConstants.setLineColor(attributes, Color.BLACK);
//		}
		attributes.remove(EDGE_PARENT);
	}

	/**
	 * @param child
	 * @param edge
	 */
	private void treeSetParent(Object child, Object parent)
	{
		Map attributes = model.getAttributes(child);
		// TODO Null pointer here. What conditions?
		Object currentParent = attributes.get(EDGE_PARENT);
		if (parent != null && currentParent != null && currentParent != parent)
		{
			Object oldNode = treeGetParent(currentParent);
			if(oldNode != null)
			{
				Map edgeAttr = model.getAttributes(currentParent);
				edgeAttr.put(EDGE_PARENT, child);
				treeSetParent(oldNode, currentParent);
			}
		}
		attributes.put(EDGE_PARENT, parent);
	}
}