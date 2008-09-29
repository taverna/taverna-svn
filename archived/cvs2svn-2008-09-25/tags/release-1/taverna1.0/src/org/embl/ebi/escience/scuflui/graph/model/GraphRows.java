/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.7 $
 */
public class GraphRows
{
	private static final String ROW = "row";
	private static int ITERATIONS = 20;
	private static int ROW_HEIGHT = 45;
	private static int X_SEPARATION = 10;
	private static int GRAPH_EDGE = 10;

	private GraphModel model;
	private List rows = new ArrayList();

	/**
	 * @param model
	 */
	public GraphRows(GraphModel model)
	{
		this.model = model;
	}

	/**
	 * @param node
	 * @param row
	 */
	private void add(Object node, int row)
	{
		List nodes;
		while (row >= rows.size())
		{
			nodes = new ArrayList();
			rows.add(nodes);
		}
		nodes = (List) rows.get(row);
		if (GraphUtilities.isGroup(model, node))
		{
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				if(!nodes.contains(model.getChild(node, index)))
				{
					nodes.add(model.getChild(node, index));
				}
			}
		}
		else
		{
			if(!nodes.contains(node))
			{
				nodes.add(node);
			}
		}
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
			int parentRank = getRow(GraphUtilities.getSourceNode(model, edges.next()));
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
			remove(node, oldRow.intValue());
		}
		System.err.println("Set row " + row + ": " + node);
		add(node, row);
		calculateBounds(row);
	}

	/**
	 * @param node
	 * @param row
	 */
	private void remove(Object node, int row)
	{
		boolean removed = true;
		List nodes = (List) rows.get(row);
		if (GraphUtilities.isGroup(model, node))
		{
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				removed = nodes.remove(model.getChild(node, index)) && removed;
			}
		}
		else
		{
			removed = nodes.remove(node);
		}
		if (removed)
		{
			calculateBounds(row);
			for (int index = rows.size() - 1; index > 0; index--)
			{
				nodes = (List) rows.get(index);
				if (nodes.isEmpty())
				{
					rows.remove(index);
				}
				else
				{
					break;
				}
			}

			// Iterator inputs = getInputs(node);
			// while(inputs.hasNext())
			// {
			// Object input = inputs.next();
			// if(input instanceof VirtualNode)
			// {
			// removeVirtualNodes((VirtualNode)input, row + 1);
			// }
			// }
	
		}
	}

	/**
	 * @param node
	 */
	public void remove(Object node)
	{
		for (int row = 0; row < rows.size(); row++)
		{
			remove(node, row);
		}
	}

	/**
	 * 
	 */
	public void clear()
	{
		rows.clear();
	}

	/**
	 * @param neighbour
	 */
	private void calculateBounds(int row)
	{
		List nodes = (List) rows.get(row);
		Map edits = new HashMap();
		int y = row * ROW_HEIGHT + GRAPH_EDGE;
		int x = GRAPH_EDGE;
		for (int index = 0; index < nodes.size(); index++)
		{
			Object node = nodes.get(index);
			if (model.isEdge(node))
			{
				// Map attributes = model.getAttributes(node);
				// if(attributes != null)
				// {
				// int lineMid = y + (ROW_HEIGHT / 2);
				// x += X_SEPARATION;
				// List points = GraphConstants.getPoints(attributes);
				// Point2D point = new Point(x, lineMid);
				// for(int index2 = 0; index2 < points.size(); index2++)
				// {
				// Point2D existingPoint = (Point2D)points.get(index2);
				// if(existingPoint.getY() > lineMid)
				// {
				// points.add(index2, point);
				// break;
				// }
				// }
				// x += X_SEPARATION;
				// }
			}
			else
			{
				Map attributes = model.getAttributes(node);
				if (attributes != null)
				{
					Rectangle2D bounds = GraphConstants.getBounds(attributes);
					if (bounds != null)
					{
						if (x != bounds.getX() || y != bounds.getY())
						{
							bounds = new Rectangle(x, y, (int) bounds.getWidth(), (int) bounds
									.getHeight());
							Map newAttrs = new HashMap();
							GraphConstants.setBounds(newAttrs, bounds);
							edits.put(node, newAttrs);
						}
						x += bounds.getWidth() + X_SEPARATION;
					}
				}
			}
		}
		if (!edits.isEmpty())
		{
			model.edit(edits, null, null, null);
		}
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

	/**
	 * @param edge
	 */
	public void addEdge(Object edge)
	{
		Object source = GraphUtilities.getSourceNode(model, edge);
		Object target = GraphUtilities.getTargetNode(model, edge);
		int sourceRow = getRow(source);
		int targetRow = getRow(target);
		//System.err.println("Adding non-tree edge " + sourceRow + "-" + targetRow + ": " + edge);		
		for (int index = sourceRow + 1; index < targetRow; index++)
		{
			List nodes = (List) rows.get(index);
			nodes.add(edge);
		}
	}
}