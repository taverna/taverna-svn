/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.jgraph.graph.CellMapper;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.5 $
 */
public class PositionLayout extends ModelSpanningTree
{
	class Row extends AbstractList
	{
		private ArrayList nodes = new ArrayList();

		private int rowIndex = -1;

		Row(int rowIndex)
		{
			this.rowIndex = rowIndex;
		}

		public void add(int index, Object node)
		{
			setY(node, (rowIndex * 40) + 10);
			if (index != 0)
			{
				Object left = get(index - 1);
				Edge edge = new Edge(left, node);
				setLeftEdge(node, edge);
				setRightEdge(left, edge);

				edges.add(edge);
			}

			if (index < size())
			{
				Object right = get(index + 1);
				Edge edge = new Edge(node, right);
				setLeftEdge(right, edge);
				setRightEdge(node, edge);

				edges.add(edge);
			}

			nodes.add(index, node);
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
			// System.err.println("Remove node " + node + " from row " +
			// rowIndex);

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

			removeFromTree(attributes);
			LayoutConstants.setLeftEdge(attributes, null);
			LayoutConstants.setRightEdge(attributes, null);

			if (leftNode != null && rightNode != null)
			{
				Edge edge = new Edge(leftNode, rightNode);
				setLeftEdge(rightNode, edge);
				setRightEdge(leftNode, edge);

				edges.add(edge);
				// addEdge(edge);
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
	}

	private static final int X_SEPARATION = 10;

	protected Collection edges;

	private CellMapper mapper;

	/**
	 * @param model
	 * @param mapper
	 */
	public PositionLayout(GraphModel model, CellMapper mapper)
	{
		super(model);
		this.mapper = mapper;
		edges = new TreeSet(new Comparator()
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
						return o1.toString().compareToIgnoreCase(o2.toString());
					}
					return targetRow1 - targetRow2;
				}
				return edgeWeight2 - edgeWeight1;
			}
		});
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
				edge = getAttributes(edges.next()).get(node);
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
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#moveNode(java.lang.Object,
	 *      java.lang.Object, int)
	 */
	protected void setRank(Object node, int x)
	{
		// assert x >= getMinimumRank(node) : this + ": Attempted to set " +
		// node + " to " + x;
		// System.err.println("Set node " + node + " to " + x);
		if (node instanceof VirtualNode)
		{
			Point2D point = ((VirtualNode) node).getPosition();
			point.setLocation(x, point.getY());
		}
		else if (GraphUtilities.isGroup(model, node))
		{
			CellView view = mapper.getMapping(node, false);
			int newX = (int) (x - (view.getBounds().getWidth() / 2));
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				// TODO Warning!
				Map attributes = getAttributes(model.getChild(node, index));
				assert (attributes != null) : model.getChild(node, index);
				Rectangle2D rect = GraphConstants.getBounds(attributes);
				assert rect != null : model.getChild(node, index);
				rect.setFrame(newX, rect.getY(), rect.getWidth(), rect.getHeight());
				newX += rect.getWidth() + 15;
			}
			view.update();
		}
		else
		{
			Map attributes = getAttributes(node);
			assert attributes != null : node;
			Rectangle2D rect = GraphConstants.getBounds(attributes);
			assert rect != null : node;
			double newX = x - (rect.getWidth() / 2);
			rect.setFrame(newX, rect.getY(), rect.getWidth(), rect.getHeight());
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getRank(java.lang.Object)
	 */
	protected int getRank(Object node)
	{
		Rectangle2D rect = getBounds(node);
		if (rect != null && rect.getX() >= X_SEPARATION)
		{
			return (int) rect.getCenterX();
		}
		int rank = getMinimumRank(node);
		setRank(node, rank);
		return rank;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMinimumRank(java.lang.Object)
	 */
	protected int getMinimumRank(Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Edge leftEdge = LayoutConstants.getLeftEdge(attributes);
		if (leftEdge != null)
		{
			Object leftNode = leftEdge.getSource();
			return getMinimumRank(leftNode) + getMinimumEdgeLength(leftEdge);
		}
		return (int) ((getBounds(node).getWidth() / 2) + X_SEPARATION);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMaximumRank(java.lang.Object)
	 */
	protected int getMaximumRank(Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null : node;
		Edge rightEdge = LayoutConstants.getRightEdge(attributes);
		if (rightEdge != null)
		{
			Object rightNode = rightEdge.getSource();
			return getRank(rightNode) - getMinimumEdgeLength(rightEdge);
		}
		return Integer.MAX_VALUE;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#addNonTreeEdge(java.lang.Object)
	 */
	protected void addNonTreeEdge(Object edge)
	{
		// TODO Implement addNonTreeEdge

	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMinimumEdgeLength(java.lang.Object)
	 */
	protected int getMinimumEdgeLength(Object edge)
	{
		if (edge instanceof Edge)
		{
			Edge theEdge = (Edge) edge;
			if (theEdge.getWeight() == 0)
			{
				Object leftNode = ((Edge) edge).getSource();
				Object rightNode = ((Edge) edge).getTarget();
				return (int) ((getBounds(leftNode).getWidth() + getBounds(rightNode).getWidth()) / 2)
						+ X_SEPARATION;
			}
		}
		return 0;
	}

	protected void setY(Object node, int y)
	{
		if (node instanceof VirtualNode)
		{
			Point2D point = ((VirtualNode) node).getPosition();
			point.setLocation(point.getX(), y + 10);
		}
		else if (GraphUtilities.isGroup(model, node))
		{
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				Map attributes = getAttributes(model.getChild(node, index));
				assert attributes != null : model.getChild(node, index);
				Rectangle2D rect = GraphConstants.getBounds(attributes);
				assert rect != null : model.getChild(node, index);
				rect.setFrame(rect.getX(), y, rect.getWidth(), rect.getHeight());
			}
		}
		else
		{
			Map attributes = getAttributes(node);
			assert attributes != null : node;
			Rectangle2D rect = GraphConstants.getBounds(attributes);
			assert rect != null : node;
			rect.setFrame(rect.getX(), y, rect.getWidth(), rect.getHeight());
		}
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
			if (node.getBottomEdge().getTarget() != target)
			{
				removeEdge(nextEdge);
				removeEdge(node.getBottomEdge());
				node = null;
			}
		}
		Edge previousEdge = getPreviousEdgeSegment(target, edge);
		if (previousEdge != null)
		{
			node = (IntermediateNode) previousEdge.getSource();
			if (node.getTopEdge().getTarget() != source)
			{
				removeEdge(previousEdge);
				removeEdge(node.getTopEdge());
				node = null;
			}
		}
		if (node == null)
		{
			node = new IntermediateNode(source, target);
		}
		setNextEdgeSegment(source, edge, node.getTopEdge());
		setPreviousEdgeSegment(target, edge, node.getBottomEdge());
		edges.add(node.getTopEdge());
		edges.add(node.getBottomEdge());
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
		// System.err.println("Remove intermediates between " + source + " & "
		// +target);
		IntermediateNode node = (IntermediateNode) getNextEdgeSegment(source, edge).getSource();
		setNextEdgeSegment(source, edge, null);
		setPreviousEdgeSegment(target, edge, null);
		removeEdge(node.getTopEdge());
		removeEdge(node.getBottomEdge());
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

	private Rectangle2D getBounds(Object node)
	{
		if (node instanceof VirtualNode)
		{
			return ((VirtualNode) node).getBounds();
		}
		CellView view = mapper.getMapping(node, false);
		return view.getBounds();
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
		// TODO Implement shiftRank
		setRank(node, getRank(node) + rankChange);
	}

	public String toString()
	{
		return "Position Tree";
	}

	protected Comparator getComparator(final Object edge)
	{
		return new Comparator()
		{
			// int edgeWeight = getEdgeWeight(edge);
			// int targetRow = getRank(getTarget(edge));

			public int compare(Object edge1, Object edge2)
			{
				if (edge1 == edge2)
				{
					return 0;
				}
				// int edgeWeight1 = getEdgeWeight(edge1);
				// int edgeWeight2 = getEdgeWeight(edge2);

				// if (edgeWeight1 == edgeWeight2)
				// {

				int sourceRow1 = getRank(getSource(edge1));
				int targetRow1 = getRank(getTarget(edge1));
				int sourceRow2 = getRank(getSource(edge2));
				int targetRow2 = getRank(getTarget(edge2));

				int length1 = targetRow1 - sourceRow1;
				int length2 = targetRow2 - sourceRow2;
				if (length1 == length2)
				{
					if (targetRow1 == targetRow2)
					{
						return edge1.toString().compareTo(edge2.toString());
					}

					// if(targetRow1 < targetRow)
					// {
					// targetRow1 += targetRow;
					// }
					// if(targetRow2 < targetRow)
					// {
					// targetRow2 += targetRow;
					// }
					return targetRow1 - targetRow2;
				}
				return length2 - length1;
			}
			//
			// if(edgeWeight1 > edgeWeight)
			// {
			// edgeWeight1 -= edgeWeight;
			// }
			// if(edgeWeight2 > edgeWeight)
			// {
			// edgeWeight2 -= edgeWeight;
			// }
			// return edgeWeight2 - edgeWeight1;
			// }
		};
	}
}