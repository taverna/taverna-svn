/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgraph.graph.CellMapper;
import org.jgraph.graph.GraphModel;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.4 $
 */
public class GraphRows
{
	private static final String ROW = "row";
	private static int ITERATIONS = 20;

	private GraphModel model;
	private CellMapper mapper;
	private List rows = new ArrayList();

	/**
	 * @param model
	 */
	public GraphRows(GraphModel model, CellMapper mapper)
	{
		this.model = model;
		this.mapper = mapper;
	}

	/**
	 * @param node
	 * @param row
	 */
	private void add(Object node, int row)
	{
		while (row >= rows.size())
		{
			rows.add(new GraphRow(model, mapper));
		}
		getGraphRow(row).add(node);
	}

	/**
	 * @param node
	 * @return minimum row
	 */
	public int getMinimumRow(Object node)
	{
		int row = -1;
		Iterator edges = GraphUtilities.getIncomingEdges(model, node).iterator();
		while (edges.hasNext())
		{
			int parentRank = getMinimumRow(GraphUtilities.getSourceNode(model, edges.next()));
			row = Math.max(row, parentRank);
		}
		row += 1;
		return row;
	}

	/**
	 * @param node
	 * @return the maximum row this node can be in
	 */
	public int getMaximumRow(Object node)
	{
		int row = Integer.MAX_VALUE;
		Iterator edges = GraphUtilities.getOutgoingEdges(model, node).iterator();
		while (edges.hasNext())
		{
			int childRank = getRow(GraphUtilities.getTargetNode(model, edges.next()));
			row = Math.min(row, childRank);
		}
		row -= 1;
		return row;
	}

	/**
	 * @param node
	 * @return row of a node
	 */
	public int getRow(Object node)
	{
		Map nodeAttributes = model.getAttributes(node);
		Integer row = (Integer) nodeAttributes.get(ROW);
		if (row == null)
		{
			int minRow = getMinimumRow(node);
			setRow(node, minRow);
			return minRow;
		}
		return row.intValue();
	}

	/**
	 * @param node
	 * @param row
	 */
	public void setRow(Object node, int row)
	{
		Map nodeAttributes = model.getAttributes(node);
		Integer oldRow = (Integer) nodeAttributes.get(ROW);
		nodeAttributes.put(ROW, new Integer(row));
		if (oldRow != null)
		{
			if (oldRow.intValue() == row)
			{
				return;
			}
			//TODO Check edges!
			
			remove(node, oldRow.intValue());
		}
		assert row >= getMinimumRow(node);
		add(node, row);
	}

	public void calculateBounds()
	{
		int y = 15;
		for(int index = 0; index < rows.size(); index++)
		{
			y = getGraphRow(index).calculateBounds(y) + 20;
		}
	}
	
	/**
	 * @param node
	 * @param row
	 */
	private void remove(Object node, int row)
	{
		boolean removed = true;
		removed = getGraphRow(row).remove(node);
		if (removed)
		{
			for (int index = rows.size() - 1; index > 0; index--)
			{
				if (getGraphRow(index).isEmpty())
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

	/**
	 * @param node
	 */
	public void remove(Object node)
	{
		for (int row = 0; row < rows.size(); row++)
		{
			getGraphRow(row).remove(node);
		}
	}

	/**
	 * 
	 */
	public void clear()
	{
		rows.clear();
	}

	private int getCrossings()
	{
		int crossings = 0;
		for (int index = 1; index < rows.size(); index++)
		{
			crossings += getCrossings(index, false);
		}
		return crossings;
	}

	private int getCrossings(int row, boolean downwards)
	{
		int crossings = 0;
		List nodes = (List) rows.get(row);
		int nextRow = row + 1;
		List nextNodes = (List) rows.get(nextRow);
		for (int index = 0; index < nodes.size(); index++)
		{
			// Iterator inputs = rows.getInputs(nodes.get(index));
			// while (inputs.hasNext())
			// {
			// int inputPos = nextNodes.indexOf(inputs.next());
			// for (int index2 = index; index2 < nodes.size(); index2++)
			// {
			// Iterator inputs2 = rows.getInputs(nodes.get(index2));
			// while (inputs2.hasNext())
			// {
			// int input2Pos = nextNodes.indexOf(inputs2.next());
			// if (Line2D.linesIntersect(index, row, inputPos, nextRow, index2,
			// row,
			// input2Pos, nextRow))
			// {
			// crossings++;
			// }
			// }
			// }
			// }
		}
		return crossings;
	}

	/**
	 * 
	 */
	private void reduceCrossings()
	{
		for (int index = 0; index < ITERATIONS; index++)
		{
			// TODO Reduce crossings
		}
	}

	private GraphRow getGraphRow(int index)
	{
		return (GraphRow)rows.get(index);
	}
	
	/**
	 * @param edge
	 */
	public void addEdge(Object edge)
	{
		Object source = GraphUtilities.getSourceNode(model, edge);
		Object target = GraphUtilities.getTargetNode(model, edge);
		int sourceRow = getRow(source);
		int targetRow = getRow(target);
		for (int index = sourceRow + 1; index < targetRow; index++)
		{
			getGraphRow(index).add(edge);
		}
	}
}