/*
 * Created on Feb 3, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.2 $
 */
public class GraphRow
{
	private static int X_SEPARATION = 15;
	private static int GRAPH_EDGE = 10;

	private boolean valid = true;
	private int groups = 0;

	private List nodes = new ArrayList();
	private GraphModel model;
	private CellMapper mapper;

	/**
	 * @param mapper
	 * 
	 */
	public GraphRow(GraphModel model, CellMapper mapper)
	{
		this.model = model;
		this.mapper = mapper;
	}

	public void add(Object node)
	{
		nodes.add(node);
		if (GraphUtilities.isGroup(model, node))
		{
			groups++;
		}
		valid = false;
	}

	/**
	 */
	public int calculateBounds(int initialY)
	{
		int y = initialY;
		if (!valid)
		{
			if (groups > 0)
			{
				y += 15;
			}
			int x = GRAPH_EDGE;
			Iterator iterator = nodes.iterator();
			while (iterator.hasNext())
			{
				Object node = iterator.next();
				if (model.isEdge(node))
				{
//					Map attributes = model.getAttributes(node);
//					if (attributes != null)
//					{
//						int lineMid = y + 10;
//						x += X_SEPARATION + 5;
//						List points = GraphConstants.getPoints(attributes);
//						Point2D point = new Point2D.Double(x, lineMid);
//						for (int index = 1; index < points.size(); index++)
//						{
//							Point2D existingPoint = (Point2D) points.get(index);
//							if (index == points.size() - 1)
//							{
//								points.add(index, point);
//								break;
//							}
//							else if (existingPoint.getY() == lineMid)
//							{
//								points.set(index, point);
//								break;
//							}
//							else if (existingPoint.getY() > lineMid)
//							{
//								points.add(index, point);
//								break;
//							}
//						}
//						x += X_SEPARATION + 5;
//						GraphConstants.setPoints(attributes, points);
//						CellView view = mapper.getMapping(node, false);
//						view.refresh(model, mapper, false);
//					}
				}
				else
				{
					if (GraphUtilities.isGroup(model, node))
					{
						for (int index2 = 0; index2 < model.getChildCount(node); index2++)
						{
							Object child = model.getChild(node, index2);
							x += setPosition(child, x, y);
						}
					}
					else
					{
						x += setPosition(node, x, y);
					}
				}
			}
		}
		return y + 20;
	}

	private double setPosition(Object node, int x, int y)
	{
		Map attributes = model.getAttributes(node);
		if (attributes != null)
		{
			Rectangle2D bounds = GraphConstants.getBounds(attributes);
			if (bounds != null)
			{
				if (x != bounds.getX() || y != bounds.getY())
				{
					bounds.setRect(x, y, bounds.getWidth(), bounds.getHeight());
					CellView view = mapper.getMapping(node, false);
					view.update();
					// TODO Easier way of updating edges?
					Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { node })
							.iterator();
					while (edges.hasNext())
					{
						CellView edgeView = mapper.getMapping(edges.next(), false);
						edgeView.update();
					}
				}
				return bounds.getWidth() + X_SEPARATION;
			}
		}
		return 0;
	}

	/**
	 * @param node
	 */
	public boolean remove(Object node)
	{
		boolean result = nodes.remove(node);
		if (result)
		{
			valid = false;
			if (GraphUtilities.isGroup(model, node))
			{
				groups--;
			}
		}
		return result;
	}

	/**
	 * @return true
	 */
	public boolean isEmpty()
	{
		return nodes.isEmpty();
	}
}
