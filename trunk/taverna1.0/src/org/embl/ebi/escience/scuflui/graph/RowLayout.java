/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Collection;
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
 * @version $Revision: 1.1 $
 */
public class RowLayout extends GraphSpanningTree
{
	private static final String ROW_TREE_PARENT = "row tree parent";
	private static final String ROW_TREE_SET = "row tree set";

	PositionLayout positionLayout;
	GraphModel model;
	List rows = new ArrayList();

	/**
	 */
	public RowLayout(GraphModel model, CellMapper mapper)
	{
		this.model = model;
		positionLayout = new PositionLayout(model, mapper);
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
						replaceTreeEdge(removed[index]);
					}
					else
					{
						//TODO remove(removed[index]);
					}
				}
				else
				{
					if (!model.isPort(removed[index]))
					{
						remove(removed[index]);
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
					getRank(GraphUtilities.getRoot(model, inserted[index]));
				}
			}
		}

		Collection treeEdges = createInitialTree(edges.iterator());
		optimiseTree(treeEdges);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTreeSet(java.lang.Object)
	 */
	protected Set getTreeSet(Object node)
	{
		Map attributes = model.getAttributes(node);
		return (Set) attributes.get(ROW_TREE_SET);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTreeParent(java.lang.Object)
	 */
	protected Object getTreeParent(Object node)
	{
		Map attributes = model.getAttributes(node);
		if (attributes != null)
		{
			return attributes.get(ROW_TREE_PARENT);
		}
		return null;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#setTreeSet(java.lang.Object,
	 *      java.util.Set)
	 */
	protected void setTreeSet(Object node, Set treeSet)
	{
		Map parentAttr = model.getAttributes(node);
		if (parentAttr != null)
		{
			parentAttr.put(ROW_TREE_SET, treeSet);
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#removeTreeEdge(java.lang.Object)
	 */
	protected void removeTreeEdge(Object edge)
	{
		Map attributes = model.getAttributes(edge);
		// if(model.isEdge(edge))
		// {
		// GraphConstants.setLineColor(attributes, Color.BLACK);
		// }
		attributes.remove(ROW_TREE_PARENT);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#treeSetParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	protected void setTreeParent(Object child, Object parent)
	{
		Map attributes = model.getAttributes(child);
		Object currentParent = attributes.get(ROW_TREE_PARENT);
		attributes.put(ROW_TREE_PARENT, parent);
		if (parent != null && currentParent != null && currentParent != parent)
		{
			setTreeParent(currentParent, child);
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#treeMoveNode(java.lang.Object,
	 *      java.lang.Object, int)
	 */
	protected void setRank(Object node, int row)
	{
		//System.err.println("Set row " + row + ": " + node );
		Map attributes = model.getAttributes(node);
		Integer oldRow = LayoutConstants.getRow(attributes);
		LayoutConstants.setRow(attributes, row);
		if (oldRow != null)
		{
			if (oldRow.intValue() == row)
			{
				return;
			}
			remove(node, oldRow.intValue());
			
			Iterator edges = getEdges(node);
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (!isTreeEdge(edge))
				{
					updateNonTreeEdge(edge);
				}
			}			
		}
		assert row >= getMinimumRank(node);
		getRow(row).add(node);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getSource(java.lang.Object)
	 */
	protected Object getSource(Object edge)
	{
		return GraphUtilities.getSourceNode(model, edge);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTarget(java.lang.Object)
	 */
	protected Object getTarget(Object edge)
	{
		return GraphUtilities.getTargetNode(model, edge);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getEdges(java.lang.Object)
	 */
	protected Iterator getEdges(Object node)
	{
		return DefaultGraphModel.getEdges(model, new Object[] { node }).iterator();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getEdgeWeight(java.lang.Object)
	 */
	protected int getEdgeWeight(Object edge)
	{
		return 1;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getRank(java.lang.Object)
	 */
	protected int getRank(Object node)
	{
		Map attributes = getAttributes(node);
		Integer row = LayoutConstants.getRow(attributes);
		if (row == null)
		{
			int newRank = getMaximumRank(node);
			setRank(node, newRank);
			return newRank;
		}
		return row.intValue();
	}

	private void updateNonTreeEdge(Object edge)
	{
		Object previous = getSource(edge);
		int sourceRow = getRank(previous);
		Object target = getTarget(edge);
		int targetRow = getRank(target);
		Map attributes = model.getAttributes(edge);
		List nodeChain = GraphConstants.getPoints(attributes);
		for (int index = 1; index < nodeChain.size();)
		{
			Object obj = GraphUtilities.getRoot(model,nodeChain.get(index));
			int row = index + sourceRow;
			int nodeRow = getRank(obj);
			if (nodeRow < row || (nodeRow == targetRow && obj != target))
			{
				nodeChain.remove(index);
				getRow(nodeRow).remove(obj);
			}
			else
			{
				if (nodeRow > row)
				{
					VirtualNode node = new VirtualNode();
					obj = node;
					LayoutConstants.setRow(node.getAttributes(), row);
					nodeChain.add(index, node);
					getRow(row).add(node);
				}
				index++;
				previous = obj;
			}
		}
		GraphConstants.setPoints(attributes, nodeChain);
	}

	private Map getAttributes(Object node)
	{
		if (node instanceof VirtualNode)
		{
			return ((VirtualNode) node).getAttributes();
		}
		return model.getAttributes(node);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMinimumRank(java.lang.Object)
	 */
	protected int getMinimumRank(Object node)
	{
		int row = -1;
		Iterator edges = GraphUtilities.getIncomingEdges(model, node).iterator();
		while (edges.hasNext())
		{
			int parentRank = getMinimumRank(GraphUtilities.getSourceNode(model, edges.next()));
			row = Math.max(row, parentRank);
		}
		row += 1;
		return row;
	}

	private void remove(Object node)
	{
		Map attributes = model.getAttributes(node);
		assert attributes != null;
		Integer row = LayoutConstants.getRow(attributes);
		assert row != null;
		remove(node, row.intValue());
	}

	/**
	 * @param node
	 * @param row
	 */
	private void remove(Object node, int row)
	{
		boolean removed = true;
		removed = getRow(row).remove(node);
		if (removed)
		{
			for (int index = rows.size() - 1; index > 0; index--)
			{
				if (getRow(index).isEmpty())
				{
					rows.remove(index);
				}
				else
				{
					break;
				}
			}
		}
	}

	private PositionLayout.Row getRow(int index)
	{
		for (int size = rows.size(); size <= index; size++)
		{
			rows.add(positionLayout.new Row(size));
		}
		return (PositionLayout.Row) rows.get(index);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getCutValue(java.lang.Object,
	 *      java.lang.String)
	 */
	protected int getCutValue(Object edge, String timeStamp)
	{
		Map attributes = model.getAttributes(edge);
		if (timeStamp.equals(LayoutConstants.getCutValueTimeStamp(attributes)))
		{
			return LayoutConstants.getCutValue(attributes).intValue();
		}
		int cutValue = super.getCutValue(edge, timeStamp);
		LayoutConstants.setCutValueTimeStamp(attributes, timeStamp);
		LayoutConstants.setCutValue(attributes, cutValue);
		return cutValue;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMaximumRank(java.lang.Object)
	 */
	protected int getMaximumRank(Object node)
	{
		Set edges = GraphUtilities.getOutgoingEdges(model, node);
		if (edges.isEmpty())
		{
			return getMinimumRank(node);
		}
		int row = Integer.MAX_VALUE;
		Iterator edgeIterator = edges.iterator();
		while (edgeIterator.hasNext())
		{
			int parentRank = getMaximumRank(GraphUtilities
					.getTargetNode(model, edgeIterator.next()));
			row = Math.min(row, parentRank);
		}
		row -= 1;
		return row;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#addNonTreeEdge(java.lang.Object)
	 */
	protected void addNonTreeEdge(Object edge)
	{
		updateNonTreeEdge(edge);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMinimumEdgeLength(java.lang.Object)
	 */
	protected int getMinimumEdgeLength(Object edge)
	{
		return 1;
	}
	
	protected void tightenEdge(Object edge, Object fixedNode)
	{
		super.tightenEdge(edge, fixedNode);
		updateNonTreeEdge(edge);
	}
}