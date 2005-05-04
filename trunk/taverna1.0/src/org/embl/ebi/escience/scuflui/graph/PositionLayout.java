/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Rectangle2D;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphModel;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.17 $
 * 
 */
public class PositionLayout extends ModelSpanningTree
{
	class Row extends AbstractList
	{
		private ArrayList nodes = new ArrayList();
		private int y = 0;

		// private int height = 0;

		Row(int rowIndex)
		{
			y = rowIndex * (ROW_HEIGHT + Y_SEPARATION) + Y_SEPARATION;
		}

		public Object set(int index, Object element)
		{
			return nodes.set(index, element);
		}

		public void add(int index, Object node)
		{
			setInitialPosition(index, node);
			nodes.add(index, node);
			//System.err.println("Add node " + node + " to row " + ((y - Y_SEPARATION) / (ROW_HEIGHT + Y_SEPARATION)));			
		}

		protected void updateEdges()
		{
			for (int index = 1; index < size(); index++)
			{
				Object left = get(index - 1);
				Object right = get(index);

				Map leftAttributes = getAttributes(left);
				Map rightAttributes = getAttributes(right);
				Edge edge = LayoutConstants.getRightEdge(leftAttributes);
				if (edge != null && edge.getTarget() != right)
				{
					removeEdge(edge);
					edge = LayoutConstants.getRightEdge(rightAttributes);
					if (edge != null && edge.getSource() != left)
					{
						removeEdge(edge);
						edge = null;
					}
				}

				if (edge == null)
				{
					edge = new Edge(left, right);
					LayoutConstants.setLeftEdge(rightAttributes, edge);
					LayoutConstants.setRightEdge(leftAttributes, edge);

					edges.add(edge);
					setInitialPosition(index, right);
				}
			}
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#moveNode(java.lang.Object,
		 *      java.lang.Object, int)
		 */
		private void setInitialPosition(int index, Object node)
		{
			int x = X_SEPARATION;
			int y = this.y;
			if (index > 0)
			{
				BoundingBox bounds = getBounds(get(index - 1));
				x = (int) bounds.getMaxX() + X_SEPARATION;
			}

			if (node instanceof VirtualNode)
			{
				y += 10;
			}
			else if (GraphUtilities.isGroup(model, node))
			{
				y -= 15;
			}

			getBounds(node).setPosition(x, y);
		}

		private void setLeftEdge(Object node, Edge edge)
		{
			Map attributes = getAttributes(node);
			LayoutConstants.setLeftEdge(attributes, edge);
		}

		private void setRightEdge(Object node, Edge edge)
		{
			Map attributes = getAttributes(node);
			LayoutConstants.setRightEdge(attributes, edge);
		}

		/*
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		public Object remove(int index)
		{
			Object node = nodes.remove(index);
			//System.err.println("Remove node " + node + " from row " + ((y - Y_SEPARATION) / (ROW_HEIGHT + Y_SEPARATION)));

			Map attributes = getAttributes(node);
			Edge leftEdge = LayoutConstants.getLeftEdge(attributes);
			Edge rightEdge = LayoutConstants.getRightEdge(attributes);
			Object leftNode = null;
			Object rightNode = null;
			if (leftEdge != null)
			{
				leftNode = leftEdge.getSource();
				setRightEdge(leftNode, null);
			}

			if (rightEdge != null)
			{
				rightNode = rightEdge.getTarget();
				setLeftEdge(rightNode, null);
			}

			if (rightEdge != null)
			{
				removeEdge(rightEdge);
			}

			if (leftEdge != null)
			{
				removeEdge(leftEdge);
			}

			setTreeSet(node, null);
			LayoutConstants.setLeftEdge(attributes, null);
			LayoutConstants.setRightEdge(attributes, null);

			return node;
		}

		public Object get(int index)
		{
			return nodes.get(index);
		}

		public int size()
		{
			return nodes.size();
		}

		public void sort(Row other, boolean next)
		{
			// TODO Sort children at the same time
			Iterator nodes = this.nodes.iterator();
			while (nodes.hasNext())
			{
				Object node = nodes.next();
				calculateMedianValue(node, other, next);
				// int childCount = model.getChildCount(node);
				// for(int index = 0; index < childCount; index++)
				// {
				// Object child = model.getChild(node, index);
				// calculateMedianValue(child, other, next);
				// }
			}

			Collections.sort(this, new Comparator()
			{
				public int compare(Object o1, Object o2)
				{
					Integer median1 = (Integer) getAttributes(o1).get("Median_value");
					Integer median2 = (Integer) getAttributes(o2).get("Median_value");
					if (median1.intValue() == -1 || median2.intValue() == -1)
					{
						return 0;
					}
					return median1.compareTo(median2);
				}
			});
		}

		private void calculateMedianValue(Object node, Row other, boolean next)
		{
			Iterator edges = getEdges(node);
			ArrayList values = new ArrayList();
			while (edges.hasNext())
			{
				Object edge = edges.next();
				if (getSource(edge) instanceof IntermediateNode)
				{
					IntermediateNode interNode = (IntermediateNode) getSource(edge);
					if (next)
					{
						if (node.equals(interNode.getSource()))
						{
							values.add(new Integer(other.indexOf(interNode.getTarget())));
						}
					}
					else if (node.equals(interNode.getTarget()))
					{
						values.add(new Integer(other.indexOf(interNode.getSource())));
					}
				}
			}
			Integer median;
			if (values.size() == 0)
			{
				median = new Integer(-1);
			}
			else
			{
				median = (Integer) values.get(values.size() / 2);
			}

			getAttributes(node).put("Median_value", median);

		}
	}

	private static final int X_SEPARATION = 15;
	private static final int Y_SEPARATION = 15;
	private static final int ROW_HEIGHT = 30;

	protected List edges;

	private CellMapper mapper;

	/**
	 * @param model
	 * @param mapper
	 */
	public PositionLayout(GraphModel model, CellMapper mapper)
	{
		super(model);
		this.mapper = mapper;
		edges = new ArrayList();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getSource(java.lang.Object)
	 */
	protected Object getSource(Object edge)
	{
		if (edge instanceof Edge)
		{
			return ((Edge) edge).getSource();
		}
		return null;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTarget(java.lang.Object)
	 */
	protected Object getTarget(Object edge)
	{
		if (edge instanceof Edge)
		{
			return ((Edge) edge).getTarget();
		}
		return null;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getEdges(java.lang.Object)
	 */
	protected Iterator getEdges(Object node)
	{
		List list = new ArrayList();
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Object edge = LayoutConstants.getLeftEdge(attributes);
		if (edge != null)
		{
			list.add(edge);
		}
		edge = LayoutConstants.getRightEdge(attributes);
		if (edge != null)
		{
			list.add(edge);
		}
		if (node instanceof VirtualNode)
		{
			VirtualNode virtualNode = (VirtualNode) node;
			edge = virtualNode.getNextEdge();
			if (edge != null)
			{
				list.add(edge);
			}
			edge = virtualNode.getPreviousEdge();
			if (edge != null)
			{
				list.add(edge);
			}
		}
		else
		{
			Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { node }).iterator();
			while (edges.hasNext())
			{
				edge = getAttributes(edges.next()).get(GraphUtilities.getRoot(model, node));
				if (edge != null)
				{
					list.add(edge);
				}
			}
		}
		return list.iterator();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getEdgeWeight(java.lang.Object)
	 */
	protected int getEdgeWeight(Object edge)
	{
		if (edge instanceof Edge)
		{
			return ((Edge) edge).getWeight();
		}
		return 1;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getRank(java.lang.Object)
	 */
	protected int getRank(Object node)
	{
		BoundingBox rect = getBounds(node);
		assert rect != null;
		return (int) rect.getX();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#addNonTreeEdge(java.lang.Object)
	 */
	protected void addNonTreeEdge(Object edge)
	{
		// TODO Implement addNonTreeEdge
	}

	private Edge getNextEdgeSegment(Object previous, Object edge)
	{
		if (previous instanceof VirtualNode)
		{
			return ((VirtualNode) previous).getNextEdge();
		}
		return (Edge) getAttributes(edge).get(previous);
	}

	public IntermediateNode updateIntermediateNode(Object source, Object target, Object edge)
	{
		Edge nextEdge = getNextEdgeSegment(source, edge);
		IntermediateNode node = null;
		if (nextEdge != null)
		{
			node = (IntermediateNode) nextEdge.getSource();
			if (!node.getTarget().equals(target))
			{
				removeEdge(nextEdge);
				removeEdge(node.getTargetEdge());
				node = null;
			}
		}
		Edge previousEdge = getPreviousEdgeSegment(target, edge);
		if (previousEdge != null)
		{
			node = (IntermediateNode) previousEdge.getSource();
			if (!node.getSource().equals(source))
			{
				removeEdge(previousEdge);
				removeEdge(node.getSourceEdge());
				node = null;
			}
		}
		if (node == null)
		{
			node = new IntermediateNode(edge, source, target);
		}
		setNextEdgeSegment(source, edge, node.getSourceEdge());
		setPreviousEdgeSegment(target, edge, node.getTargetEdge());
		if (!edges.contains(node.getSourceEdge()))
		{
			edges.add(node.getSourceEdge());
		}
		if (!edges.contains(node.getTargetEdge()))
		{
			edges.add(node.getTargetEdge());
		}
		return node;
	}

	protected void removeEdge(Object edge)
	{
		edges.remove(edge);
		assert !edges.contains(edge) : this + ": " + edge;
		super.removeEdge(edge);
	}

	public void removeIntermediateNode(Object source, Object target, Object edge)
	{
		// System.err.println("Remove intermediates between " + source + " & " +target);
		Edge intermediateEdge = getNextEdgeSegment(source, edge);
		IntermediateNode node;
		if (intermediateEdge == null)
		{
			intermediateEdge = getPreviousEdgeSegment(target, edge);
		}
		setNextEdgeSegment(source, edge, null);
		setPreviousEdgeSegment(target, edge, null);
		if (intermediateEdge != null)
		{
			node = (IntermediateNode) intermediateEdge.getSource();
			removeEdge(node.getSourceEdge());
			removeEdge(node.getTargetEdge());
			// TODO remove(node);
		}
	}

	private void setNextEdgeSegment(Object source, Object edge, Edge nextEdge)
	{
		if (source instanceof VirtualNode)
		{
			((VirtualNode) source).setNextEdge(nextEdge);
		}
		else
		{
			if (nextEdge != null)
			{
				getAttributes(edge).put(source, nextEdge);
			}
			else
			{
				getAttributes(edge).remove(source);
			}
		}
	}

	protected BoundingBox getBounds(Object node)
	{
		if (node instanceof VirtualNode)
		{
			return ((VirtualNode) node).getBounds();
		}
		final CellView view = mapper.getMapping(node, false);
		if (view != null)
		{
			return new BoundingBox()
			{
				public void setPosition(int x, int y)
				{
					translate(x - (int) getX(), y - (int) getY());
				}

				public void setRect(double x, double y, double w, double h)
				{
					view.getBounds().setRect(x, y, w, h);
				}

				public int outcode(double x, double y)
				{
					// TODO Implement outcode
					return 0;
				}

				public boolean isEmpty()
				{
					return getWidth() * getHeight() == 0;
				}

				public double getY()
				{
					return view.getBounds().getY();
				}

				public double getX()
				{
					return view.getBounds().getX();
				}

				public double getWidth()
				{
					return view.getBounds().getWidth();
				}

				public double getHeight()
				{
					return view.getBounds().getHeight();
				}

				public Rectangle2D createUnion(Rectangle2D r)
				{
					// TODO Implement createUnion
					return null;
				}

				public Rectangle2D createIntersection(Rectangle2D r)
				{
					// TODO Implement createIntersection
					return null;
				}

				public void translate(int dx, int dy)
				{
					if (view.isLeaf())
					{
						view.getAllAttributes().translate(dx, dy);
					}
					else
					{
						CellView[] cells = view.getChildViews();
						for (int index = 0; index < cells.length; index++)
						{
							CellView child = cells[index];
							child.getAllAttributes().translate(dx, dy);
							child.update();
						}
					}
					view.update();
				}
			};
		}
		throw new NullPointerException("No view for " + node);
	}

	private Edge getPreviousEdgeSegment(Object previous, Object edge)
	{
		if (previous instanceof VirtualNode)
		{
			return ((VirtualNode) previous).getPreviousEdge();
		}
		return (Edge) getAttributes(edge).get(previous);
	}

	private void setPreviousEdgeSegment(Object target, Object edge, Edge previousEdge)
	{
		if (target instanceof VirtualNode)
		{
			((VirtualNode) target).setPreviousEdge(previousEdge);
		}
		else
		{
			if (previousEdge != null)
			{
				getAttributes(edge).put(target, previousEdge);
			}
			else
			{
				getAttributes(edge).remove(target);
			}
		}
	}

	protected void shiftRank(Object node, int rankChange)
	{
//		if (rankChange < 0)
//		{
//			assert rankChange >= -getMaxRankMoveNegative(node) : node + ": " + rankChange + " < "
//					+ -getMaxRankMoveNegative(node);
//		}
//		else
//		{
//			assert rankChange <= getMaxRankMovePositive(node) : node + ": " + rankChange + " > "
//					+ getMaxRankMovePositive(node);
//		}
		try
		{
			getBounds(node).translate(rankChange, 0);
		}
		catch (NullPointerException e)
		{
			// No bounding box/view. Do nothing?
			e.printStackTrace();
		}
	}

	public String toString()
	{
		return "Position Tree";
	}

	protected List createInitialTree(Collection unused)
	{
		Collections.sort(edges, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				if (o1 == o2)
				{
					return 0;
				}

				int edgeWeight1 = getEdgeWeight(o1);
				if (edgeWeight1 == 0)
				{
					edgeWeight1 = 16;
				}
				int edgeWeight2 = getEdgeWeight(o2);
				if (edgeWeight2 == 0)
				{
					edgeWeight2 = 16;
				}

				if (edgeWeight1 == edgeWeight2)
				{
					int targetRow1 = getRank(getTarget(o1));
					int targetRow2 = getRank(getTarget(o2));

					if (targetRow1 == targetRow2)
					{
						return o1.toString().compareTo(o2.toString());
					}
					return targetRow1 - targetRow2;
				}
				return edgeWeight2 - edgeWeight1;
			}
		});
		List result = super.createInitialTree(edges);
		edges.clear();
		return result;
	}

	public void updateNode(Object node)
	{
		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (isTreeEdge(edge))
			{
				Set tailSet = new HashSet();
				Set headSet = new HashSet();

				getTailSet(getTarget(edge), tailSet, edge);
				getTailSet(getSource(edge), headSet, edge);
				assert !headSet.isEmpty() : edge;
				tightenEdge(edge, headSet, tailSet);
			}
		}
	}

	protected int getMaxRankMoveNegative(Set set, Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Edge leftEdge = LayoutConstants.getLeftEdge(attributes);
		if (leftEdge != null)
		{
			if (!isTreeEdge(leftEdge))
			{
				return getSlack(leftEdge);
			}
		}
		else
		{
			return getRank(node) - X_SEPARATION;
		}
		return Integer.MAX_VALUE;
	}

	protected int getMaxRankMovePositive(Set set, Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Edge rightEdge = LayoutConstants.getRightEdge(attributes);
		if (rightEdge != null)
		{
			if (!isTreeEdge(rightEdge))
			{
				return getSlack(rightEdge);
			}
		}
		return Integer.MAX_VALUE;
	}

	protected int getSlack(Object edge)
	{
		Object source = getSource(edge);
		Object target = getTarget(edge);
		BoundingBox sourceRect = getBounds(source);
		BoundingBox targetRect = getBounds(target);

		int edgeLength = (int) (targetRect.getCenterX() - sourceRect.getCenterX());
		int minimumEdgeLength = 0;

		if (edge instanceof Edge)
		{
			Edge theEdge = (Edge) edge;
			if (theEdge.getWeight() == 0)
			{
				minimumEdgeLength = (int) (targetRect.getWidth() + sourceRect.getWidth()) / 2
						+ X_SEPARATION;
			}
			else
			{
				// Use port locations if available
//				if(source instanceof VirtualNode && !(target instanceof VirtualNode))
//				{
//					VirtualNode node = (VirtualNode)source;
//					Object actualEdge = node.edge;
//					target = model.getTarget(actualEdge);
//					CellView view = mapper.getMapping(target, false);
//					if(view instanceof PortView)
//					{
//						edgeLength = (int) (((PortView)view).getLocation(null).getX() - sourceRect.getCenterX());						
//					}
//				}
//				else if(target instanceof VirtualNode && !(source instanceof VirtualNode))
//				{
//					VirtualNode node = (VirtualNode)target;
//					Object actualEdge = node.edge;
//					source = model.getSource(actualEdge);
//					CellView view = mapper.getMapping(source, false);
//					if(view instanceof PortView)
//					{
//						edgeLength = (int) (targetRect.getCenterX() - ((PortView)view).getLocation(null).getX());
//					}
//				}				
			}
		}
		return edgeLength - minimumEdgeLength;
	}
}