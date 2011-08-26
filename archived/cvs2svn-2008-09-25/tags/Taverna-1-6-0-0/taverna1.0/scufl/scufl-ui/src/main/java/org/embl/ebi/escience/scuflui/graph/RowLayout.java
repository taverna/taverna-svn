/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.embl.ebi.escience.scuflui.graph.PositionLayout.Row;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * Manages the layout of a directed graph. Listens for change events on the graph to be able to
 * update as the graph changes.
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.2 $
 */
public class RowLayout extends ModelSpanningTree
{
	PositionLayout positionLayout;
	List rows = new ArrayList();
	CellMapper mapper;

	/**
	 */
	public RowLayout(GraphModel model, CellMapper mapper)
	{
		super(model);
		this.mapper = mapper;
		positionLayout = new PositionLayout(model, mapper);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
	 */
	public synchronized void layout(GraphModelEvent.GraphModelChange change)
	{
		if (model.getRootCount() == 0)
		{
			rows.clear();
			newEdges.clear();
			positionLayout.newEdges.clear();
			return;
		}

		Iterator attributes = change.getAttributes().entrySet().iterator();
		while (attributes.hasNext())
		{
			Entry entry = (Entry) attributes.next();
			Map attrs = (Map) entry.getValue();
			if (GraphConstants.getBounds(attrs) != null)
			{
				positionLayout.updateNode(entry.getKey());
			}
		}

		Object[] removed = change.getRemoved();
		if (removed != null)
		{
			// System.err.println(Arrays.asList(removed));
			for (int index = 0; index < removed.length; index++)
			{
				if (model.isEdge(removed[index]))
				{
					removeEdge(removed[index]);
				}
				else if (model.getParent(removed[index]) == null)
				{
					removeNode(removed[index]);
				}
			}
		}

		Object[] inserted = change.getInserted();
		if (inserted != null)
		{
			for (int index = 0; index < inserted.length; index++)
			{
				if (model.isEdge(inserted[index]))
				{
					// TODO Check both ends aren't null
					assert !isRemoved(inserted[index]) : inserted[index];
					newEdges.add(inserted[index]);
				}
				else
				{
					getRank(GraphUtilities.getRoot(model, inserted[index]));
				}
			}
		}

		removeEdges();
		if (!newEdges.isEmpty())
		{
			List treeEdges = createInitialTree();
			optimiseTree(treeEdges);

			reduceCrossovers();
		}

		int y = -15;
		for (int index = 0; index < rows.size(); index++)
		{
			y = getRow(index).updateEdges(y);
		}

		positionLayout.removeEdges();
		List treeEdges = positionLayout.createInitialTree();
		positionLayout.optimiseTree(treeEdges);
	}

	private void reduceCrossovers()
	{
		for (int outer = 0; outer < 10; outer++)
		{
			if (outer % 2 == 0)
			{
				for (int index = 1; index < rows.size(); index++)
				{
					PositionLayout.Row row1 = getRow(index - 1);
					PositionLayout.Row row2 = getRow(index);
					row2.sort(row1, false);
				}
			}
			else
			{
				for (int index = rows.size() - 2; index > 0; index--)
				{
					PositionLayout.Row row1 = getRow(index + 1);
					PositionLayout.Row row2 = getRow(index);
					row2.sort(row1, true);
				}
			}
		}
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
		assert attributes != null : node;
		PositionLayout.Row row = LayoutConstants.getRow(attributes);
		if (row == null)
		{
			PositionLayout.Row newRank = getRow(getStartingRank(node));
			newRank.add(node);
			return newRank.getRow();
		}
		return row.getRow();
	}

	public String toString()
	{
		return "Row Tree";
	}

	private void updateEdgeGraph(Object edge)
	{
		// System.err.println(this + ": Update " + edge);
		assert isValid(edge) : edge;
		Object previousNode = getSource(edge);

		int sourceRow = getRank(previousNode);
		int targetRow = getRank(getTarget(edge));
		int currentRow = sourceRow + 1;

		Map attributes = getAttributes(edge);
		List nodeChain = GraphConstants.getPoints(attributes);

		assert !(nodeChain.get(nodeChain.size() - 1) instanceof VirtualNode) : edge + ": "
				+ nodeChain;

		ListIterator nodes = nodeChain.listIterator(1);
		while (nodes.hasNext())
		{
			Object currentNode = GraphUtilities.getRoot(model, nodes.next());
			int nodeRow = getRank(currentNode);
			if (currentNode instanceof VirtualNode
					&& (nodeRow < currentRow || nodeRow >= targetRow))
			{
				nodes.remove();
				removeNode(currentNode);
				positionLayout.removeIntermediateNode(previousNode, currentNode, edge);
			}
			else
			{
				if (nodeRow > currentRow)
				{
					nodes.previous();
					VirtualNode node = new VirtualNode(currentRow, edge);
					currentNode = node;
					nodes.add(node);
					getRow(currentRow).add(node);
				}
				positionLayout.updateIntermediateNode(previousNode, currentNode, edge);
				currentRow++;
				previousNode = currentNode;
			}
		}
		assert nodeChain.size() == (targetRow - sourceRow) + 1 : edge + ": " + nodeChain + ": "
				+ sourceRow + "-" + targetRow;
		GraphConstants.setPoints(attributes, nodeChain);
		CellView view = mapper.getMapping(edge, false);
		if (view != null)
		{
			view.refresh(model, mapper, false);
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMinimumRank(java.lang.Object)
	 */
	private int getStartingRank(Object node)
	{
		int row = -1;
		Iterator edges = GraphUtilities.getIncomingEdges(model, node).iterator();
		while (edges.hasNext())
		{
			int parentRank = getRank(getSource(edges.next()));
			row = Math.max(row, parentRank);
		}
		row += 1;
		return row;
	}

	protected void removeNode(Object node)
	{
		super.removeNode(node);
		positionLayout.removeNode(node);
		Map attributes = getAttributes(node);
		assert attributes != null;
		PositionLayout.Row row = LayoutConstants.getRow(attributes);
		assert row != null;
		row.remove(node);
	}

	protected void removeEdge(Object edge)
	{
		super.removeEdge(edge);
		removeEdgeGraph(edge);
	}

	private void removeEdgeGraph(Object edge)
	{
		// System.err.println(this+ ": Remove edge graph " + edge);
		Object previousNode = getSource(edge);
		Object target = getTarget(edge);
		Map attributes = getAttributes(edge);
		if (attributes != null)
		{
			List nodeChain = GraphConstants.getPoints(attributes);
			for (int index = 1; index < nodeChain.size(); index++)
			{
				Object currentNode = GraphUtilities.getRoot(model, nodeChain.get(index));
				positionLayout.removeIntermediateNode(previousNode, currentNode, edge);
				previousNode = currentNode;
				if (currentNode != target)
				{
					removeNode(currentNode);
				}
			}
		}
	}

	protected Object replaceEdge(Object edge, Collection replacementEdges)
	{
		Object replacement = super.replaceEdge(edge, replacementEdges);
		updateEdgeGraph(edge);
		return replacement;
	}

	private PositionLayout.Row getRow(int index)
	{
		assert index >= 0;
		for (int size = rows.size(); size <= index; size++)
		{
			rows.add(positionLayout.new Row(index));
		}
		return (PositionLayout.Row) rows.get(index);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#addNonTreeEdge(java.lang.Object)
	 */
	protected void addNonTreeEdge(Object edge)
	{
		updateEdgeGraph(edge);
	}

	protected boolean tightenEdge(Object edge, Set sourceSet, Set targetSet)
	{
		if (super.tightenEdge(edge, sourceSet, targetSet))
		{
			updateEdgeGraph(edge);
			return true;
		}
		return false;
	}

	protected void shiftRank(Object node, int rankChange, Set set)
	{
		// System.err.println("Shift node " + node + " by " + rankChange);
		if (rankChange == 0)
		{
			return;
		}
		Map attributes = getAttributes(node);
		Row oldRank = LayoutConstants.getRow(attributes);
		assert (oldRank != null);
		oldRank.remove(node);
		Row newRank = getRow(oldRank.getRow() + rankChange);
		assert !newRank.contains(node) : node;
		newRank.add(node);
		assert newRank.contains(node) : node;

		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (!isTreeEdge(edge) && !isRemoved(edge))
			{
				Object source = getSource(edge);
				if (!set.contains(source) || !set.contains(getTarget(edge)))
				{
					updateEdgeGraph(edge);
				}
				else
				{
					// Since both the source and target are in the set to move, also move all the
					// virtual nodes on the edge. But, you only want to do it once, so do it when
					// moving the source and not when moving the target.
					if (node.equals(source))
					{
						Map edgeAttributes = getAttributes(edge);
						List nodeChain = GraphConstants.getPoints(edgeAttributes);
						for (int index = 1; index < nodeChain.size() - 1; index++)
						{
							shiftRank(nodeChain.get(index), rankChange, set);
						}
					}
				}
			}
		}
	}

	protected int getMaxRankMoveNegative(Set set, Object node)
	{
		int move = Integer.MAX_VALUE;
		int rank = getRank(node);
		boolean hasParent = false;
		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (!isRemoved(edge) && getTarget(edge).equals(node))
			{
				hasParent = true;
				if (!isTreeEdge(edge) && !set.contains(getSource(edge)))
				{
					move = Math.min(move, getSlack(edge));
				}
			}
		}
		if (hasParent == false)
		{
			return rank;
		}
		return move;
	}

	protected int getMaxRankMovePositive(Set set, Object node)
	{
		int move = Integer.MAX_VALUE;
		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (getSource(edge).equals(node) && !isTreeEdge(edge) && !set.contains(getTarget(edge)))
			{
				move = Math.min(move, getSlack(edge));
			}
		}
		return move;
	}

	protected int getSlack(Object edge)
	{
		assert !isRemoved(edge) : edge;
		return (getRank(getTarget(edge)) - getRank(getSource(edge))) - 1;
	}

	protected boolean isValid(Object edge)
	{
		if (isTreeEdge(edge))
		{
			return getSlack(edge) == 0;
		}
		return getSlack(edge) >= 0;
	}
}