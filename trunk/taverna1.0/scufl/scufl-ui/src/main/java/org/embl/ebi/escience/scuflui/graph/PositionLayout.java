/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Rectangle2D;
import java.util.AbstractList;
import java.util.ArrayList;
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
import org.jgraph.graph.PortView;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.3 $
 * 
 */
public class PositionLayout extends ModelSpanningTree
{
	class Row extends AbstractList
	{
		private int row;
		private ArrayList nodes = new ArrayList();

		public Row(int row)
		{
			this.row = row;
		}

		public int getRow()
		{
			return row;
		}

		public Object set(int index, Object element)
		{
			return nodes.set(index, element);
		}

		public void add(int index, Object node)
		{
			Map attributes = getAttributes(node);
			LayoutConstants.setRow(attributes, this);
			nodes.add(index, node);
		}

		protected int updateEdges(int rowy)
		{
			int y = rowy + Y_SEPARATION;
			if (size() > 0)
			{
				int height = (int) getBounds(get(0)).getHeight();
				Object left = null;
				Iterator nodes = iterator();
				while (nodes.hasNext())
				{
					Object right = nodes.next();
					Map rightAttributes = getAttributes(right);

					Edge edge = LayoutConstants.getLeftEdge(rightAttributes);
					Edge correctEdge = null;
					if (edge != null && (left == null || edge.getSource() != left))
					{
						removeEdge(edge);
					}
					else
					{
						correctEdge = edge;
					}

					if (left != null)
					{
						if (correctEdge == null || isRemoved(correctEdge))
						{
							correctEdge = new Edge(left, right);
							newEdges.add(correctEdge);
						}						
						
						Map leftAttributes = getAttributes(left);
						LayoutConstants.setRightEdge(leftAttributes, correctEdge);
					}
					LayoutConstants.setLeftEdge(rightAttributes, correctEdge);

					if(!nodes.hasNext())
					{
						edge = LayoutConstants.getRightEdge(rightAttributes);
						if(edge != null)
						{
							removeEdge(edge);
							LayoutConstants.setRightEdge(rightAttributes, null);							
						}
					}
					
					height = (int) Math.max(height, getBounds(right).getHeight());
					left = right;
				}

				nodes = iterator();
				while (nodes.hasNext())
				{
					setInitialPosition(nodes.next(), y, height);
				}

				return y + height;
			}
			return 0;
		}

		/*
		 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#moveNode(java.lang.Object,
		 *      java.lang.Object, int)
		 */
		private void setInitialPosition(Object node, int y, int height)
		{
			BoundingBox bounds = getBounds(node);
			int dy = (int) ((y + (height - bounds.getHeight()) / 2) - bounds.getY());
			if (dy != 0 /* && dx != 0 */)
			{
				bounds.translate(0, dy);
			}
		}

		/*
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		public Object remove(int index)
		{
			Object node = nodes.remove(index);

			Map attributes = getAttributes(node);
			Edge leftEdge = LayoutConstants.getLeftEdge(attributes);
			Edge rightEdge = LayoutConstants.getRightEdge(attributes);
			if (rightEdge != null)
			{
				removeEdge(rightEdge);
			}

			if (leftEdge != null)
			{
				removeEdge(leftEdge);
			}

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
			Iterator nodes = this.nodes.iterator();
			while (nodes.hasNext())
			{
				Object node = nodes.next();
				if (isRemoved(node))
				{
					nodes.remove();
					continue;
				}
				calculateMedianValue(node, other, next);
				int childCount = model.getChildCount(node);
				if (childCount > 0)
				{
					List children = new ArrayList();
					int x = Integer.MAX_VALUE;
					for (int index = 0; index < childCount; index++)
					{
						Object child = model.getChild(node, index);
						children.add(child);
						x = (int) Math.min(getBounds(child).getX(), x);
						calculateMedianValue(child, other, next);
					}

					Collections.sort(children, comparator);

					for (int index = 0; index < children.size(); index++)
					{
						Object child = children.get(index);
						BoundingBox bounds = getBounds(child);
						bounds.translate(x - (int) bounds.getX(), 0);
						x += bounds.getWidth() + X_SEPARATION;
					}
				}
			}

			Collections.sort(this, comparator);
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
						if (interNode.getSource().equals(GraphUtilities.getRoot(model, node)))
						{
							int indexOf = other.indexOf(interNode.getTarget());
							// assert indexOf != -1 : interNode;
							values.add(new Integer(indexOf));
						}
					}
					else if (interNode.getTarget().equals(GraphUtilities.getRoot(model, node)))
					{
						int indexOf = other.indexOf(interNode.getSource());
						// assert indexOf != -1 : interNode;
						values.add(new Integer(indexOf));
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
				if (values.size() > 2 && values.size() % 2 != 0)
				{

					median = new Integer(((Integer) values.get(values.size() / 2)).intValue()
							+ ((Integer) values.get((values.size() / 2) + 1)).intValue() / 2);
				}
				else
				{
					median = (Integer) values.get(values.size() / 2);
				}
			}
			getAttributes(node).put("Median_value", median);
		}
	}

	private static final int X_SEPARATION = 15;
	private static final int Y_SEPARATION = 30;

	CellMapper mapper;
	Comparator comparator = new Comparator()
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
	};

	/**
	 * @param model
	 * @param mapper
	 */
	public PositionLayout(GraphModel model, CellMapper mapper)
	{
		super(model);
		this.mapper = mapper;
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
		// System.err.println(this + ": Update edge " + edge);
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
				removeNode(node);
				removeEdge(previousEdge);
				removeEdge(node.getSourceEdge());
				node = null;
			}
		}
		if (node == null)
		{
			node = new IntermediateNode(edge, source, target);
			newEdges.add(node.getSourceEdge());
			newEdges.add(node.getTargetEdge());
		}
		setNextEdgeSegment(source, edge, node.getSourceEdge());
		setPreviousEdgeSegment(target, edge, node.getTargetEdge());
		return node;
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
			removeNode(node);
			removeEdge(node.getSourceEdge());
			removeEdge(node.getTargetEdge());
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

	protected BoundingBox getBounds(final Object node)
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
				public void setRect(double x, double y, double w, double h)
				{
					view.getBounds().setRect(x, y, w, h);
				}

				public int outcode(double x, double y)
				{
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
					return null;
				}

				public Rectangle2D createIntersection(Rectangle2D r)
				{
					return null;
				}

				public void translate(int dx, int dy)
				{
					// System.err.println(view.getCell() + ": Translate by " + dx + ", " + dy);
					if (view.isLeaf())
					{
						model.getAttributes(node).translate(dx, dy);
					}
					else
					{
						CellView[] cells = view.getChildViews();
						for (int index = 0; index < cells.length; index++)
						{
							CellView child = cells[index];
							model.getAttributes(child.getCell()).translate(dx, dy);
							child.refresh(model, mapper, false);
						}
					}
					view.refresh(model, mapper, false);
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

	protected void shiftRank(Object node, int rankChange, Set set)
	{
		try
		{
			if (rankChange != 0)
			{
				getBounds(node).translate(rankChange, 0);
				// System.err.println(this + " : Move by " + rankChange + " - " + node);
			}
		}
		catch (NullPointerException e)
		{
			// No bounding box/view. Do nothing?
			// System.err.println(this + ": Node " + node + " not moved");
		}
	}

	public String toString()
	{
		return "Position Tree";
	}

	public void updateNode(Object node)
	{
		Iterator edges = getEdges(node);
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if (isTreeEdge(edge))
			{
				Set sourceSet = new HashSet();
				Set targetSet = new HashSet();

				getEndSets(edge, sourceSet, targetSet);

				tightenEdge(edge, sourceSet, targetSet);
			}
		}
	}

	protected Comparator getComparator()
	{
		return new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				assert !isRemoved(o1) : o1;
				assert !isRemoved(o2) : o2;
				assert !isRemoved(getSource(o1)) : o1;
				assert !isRemoved(getTarget(o1)) : o1;
				assert !isRemoved(getSource(o2)) : o2;
				assert !isRemoved(getTarget(o2)) : o2;
				if (o1 == o2)
				{
					return 0;
				}

				int edgeWeight1 = getEdgeWeight(o1);
				int edgeWeight2 = getEdgeWeight(o2);
				if ((edgeWeight1 == 0 || edgeWeight2 == 0) && edgeWeight1 != edgeWeight2)
				{
					return edgeWeight1 - edgeWeight2;
				}

				if ((edgeWeight1 == 0 && edgeWeight2 == 0))
				{
					Map attributes1 = getAttributes(getSource(o1));
					Map attributes2 = getAttributes(getSource(o2));
					Row row1 = LayoutConstants.getRow(attributes1);
					Row row2 = LayoutConstants.getRow(attributes2);
					assert row1 != null : getSource(o1);
					assert row2 != null : getSource(o2);

					int index1 = row1.indexOf(getSource(o1));
					int index2 = row2.indexOf(getSource(o2));
					assert index1 != -1 : getSource(o1);
					assert index2 != -1 : getSource(o2);

					return index2 - index1;
				}

				int targetRow1 = getRank(getTarget(o1));
				int targetRow2 = getRank(getTarget(o2));

				if (targetRow1 == targetRow2)
				{
					return o1.toString().compareTo(o2.toString());
				}
				return targetRow1 - targetRow2;
			}
		};
	}

	protected int getMaxRankMoveNegative(Set set, Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Edge leftEdge = LayoutConstants.getLeftEdge(attributes);
		if (leftEdge != null && !isRemoved(leftEdge))
		{
			if (!set.contains(getSource(leftEdge)))
			{
				return getSlack(leftEdge);
			}
			return Integer.MAX_VALUE;
		}
		return getRank(node) - X_SEPARATION;
	}

	protected int getMaxRankMovePositive(Set set, Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Edge rightEdge = LayoutConstants.getRightEdge(attributes);
		if (rightEdge != null && !isRemoved(rightEdge))
		{
			if (!set.contains(getTarget(rightEdge)))
			{
				return getSlack(rightEdge);
			}
		}
		return Integer.MAX_VALUE;
	}

	protected void removeEdge(Object edge)
	{
		if (getEdgeWeight(edge) == 0)
		{
			Object leftNode = getSource(edge);
			Object rightNode = getTarget(edge);

			Map leftAttributes = getAttributes(leftNode);
			Map rightAttributes = getAttributes(rightNode);

			LayoutConstants.setRightEdge(leftAttributes, null);
			LayoutConstants.setLeftEdge(rightAttributes, null);
		}
		super.removeEdge(edge);
	}

	protected int getSlack(Object edge)
	{
		assert !isRemoved(edge) : edge;
		Object source = getSource(edge);
		Object target = getTarget(edge);
		BoundingBox sourceRect = getBounds(source);
		BoundingBox targetRect = getBounds(target);

		double edgeLength = targetRect.getCenterX() - sourceRect.getCenterX();
		double minimumEdgeLength = 0;

		if (edge instanceof Edge)
		{
			Edge theEdge = (Edge) edge;
			if (theEdge.getWeight() == 0)
			{
				minimumEdgeLength = (targetRect.getWidth() + sourceRect.getWidth()) / 2
						+ X_SEPARATION;
			}
			else
			{
				// Use port locations if available
				if (!(target instanceof VirtualNode))
				{
					IntermediateNode node = (IntermediateNode) source;
					Object actualEdge = node.edge;

					Object port = getPort(target, actualEdge);
					PortView portView = (PortView) mapper.getMapping(port, false);
					edgeLength = portView.getLocation().getX() - node.position.getX();
				}
			}
		}

		return (int) (edgeLength - minimumEdgeLength);
	}

	private Object getPort(Object node, Object edge)
	{
		if (!model.isPort(node))
		{
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				Object result = getPort(model.getChild(node, index), edge);
				if (result != null)
				{
					return result;
				}
			}
			return null;
		}
		Iterator edges = model.edges(node);
		while (edges.hasNext())
		{
			if (edge.equals(edges.next()))
			{
				return node;
			}
		}
		return null;
	}

	protected boolean isValid(Object edge)
	{
		if (isTreeEdge(edge))
		{
			return getSlack(edge) == 0;
		}
		if (edge instanceof Edge)
		{
			Edge theEdge = (Edge) edge;
			if (theEdge.getWeight() == 0)
			{
				return getSlack(edge) >= 0;
			}
		}
		return true;
	}
}
