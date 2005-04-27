/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Point2D;
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
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.14 $
 * 
 * TODO Change from center placed to left placed and use the port offset to fix it
 */
public class PositionLayout extends ModelSpanningTree
{
	class Row extends AbstractList
	{
		private ArrayList nodes = new ArrayList();

		// private int rowIndex = -1;

		Row(int rowIndex)
		{
			// this.rowIndex = rowIndex;
		}

		public void add(int index, Object node)
		{
			// setY(node, (rowIndex * 40) + 10);
			// if (index != 0)
			// {
			// Object left = get(index - 1);
			// Edge edge = new Edge(left, node);
			// setLeftEdge(node, edge);
			// setRightEdge(left, edge);
			//
			// edges.add(edge);
			// }
			//
			// if (index < size())
			// {
			// Object right = get(index + 1);
			// Edge edge = new Edge(node, right);
			// setLeftEdge(right, edge);
			// setRightEdge(node, edge);
			//
			// edges.add(edge);
			// }

			nodes.add(index, node);
		}

		protected void updateEdges()
		{
			if (size() > 0)
			{
				getRank(get(0));
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
					}
				}
			}
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

	private static final int X_SEPARATION = 15;
	private static final int ROW_HEIGHT = 45;

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
		// assert x >= getMinimumRank(node) : this + ": Attempted to set " + node + " to " + x;
		if (node instanceof VirtualNode)
		{
			Point2D point = ((VirtualNode) node).getPosition();
			point.setLocation(x, ((VirtualNode) node).row * ROW_HEIGHT + 20);
		}
		else
		{
			CellView view = mapper.getMapping(node, false);
			Map attributes = getAttributes(node);
			assert attributes != null : node;
			Integer row = LayoutConstants.getRow(attributes);
			assert row != null : node;
			int newY = (row.intValue() * ROW_HEIGHT) + 10;

			if (view.isLeaf())
			{
				Rectangle2D rect = GraphConstants.getBounds(view.getAllAttributes());
				assert rect != null : node;
				int newX = x - ((int) rect.getWidth() / 2);
				rect.setFrame(newX, newY, rect.getWidth(), rect.getHeight());
			}
			else
			{
				CellView[] children = view.getChildViews();
				Rectangle2D rect = view.getBounds();
				int newX = x - ((int) rect.getWidth() / 2);
				int oldX = (int) rect.getX();
				for (int index = 0; index < children.length; index++)
				{
					Rectangle2D childRect = GraphConstants.getBounds(children[index]
							.getAllAttributes());
					assert rect != null : model.getChild(node, index);
					int childX = (int) childRect.getX() + (newX - oldX);
					childRect.setFrame(childX, newY, childRect.getWidth(), childRect.getHeight());
					children[index].update();
				}
			}
			view.update();
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
			return (int) rect.getX() + ((int) rect.getWidth() / 2);
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
		return ((int) (getBounds(node).getWidth() / 2) + X_SEPARATION);
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
				return ((int) (getBounds(leftNode).getWidth() / 2) + ((int) getBounds(rightNode)
						.getWidth()) / 2)
						+ X_SEPARATION;
			}
		}
		return 0;
	}

	// private void setY(Object node, int y)
	// {
	// if (node instanceof VirtualNode)
	// {
	// Point2D point = ((VirtualNode) node).getPosition();
	// point.setLocation(point.getX(), y + 10);
	// }
	// else if (GraphUtilities.isGroup(model, node))
	// {
	// for (int index = 0; index < model.getChildCount(node); index++)
	// {
	// Map attributes = getAttributes(model.getChild(node, index));
	// assert attributes != null : model.getChild(node, index);
	// Rectangle2D rect = GraphConstants.getBounds(attributes);
	// assert rect != null : model.getChild(node, index);
	// rect.setFrame(rect.getX(), y, rect.getWidth(), rect.getHeight());
	// }
	// }
	// else
	// {
	// Map attributes = getAttributes(node);
	// assert attributes != null : node;
	// Rectangle2D rect = GraphConstants.getBounds(attributes);
	// assert rect != null : node;
	// rect.setFrame(rect.getX(), y, rect.getWidth(), rect.getHeight());
	// }
	// }

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
			node = new IntermediateNode(edge, source, target);
		}
		setNextEdgeSegment(source, edge, node.getTopEdge());
		setPreviousEdgeSegment(target, edge, node.getBottomEdge());
		if(!edges.contains(node.getTopEdge()))
		{
			edges.add(node.getTopEdge());
		}
		if(!edges.contains(node.getBottomEdge()))
		{
			edges.add(node.getBottomEdge());
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
		//System.err.println("Remove intermediates between " + source + " & " +target);
		Edge intermediateEdge = getNextEdgeSegment(source, edge);
		IntermediateNode node;
		if(intermediateEdge == null)
		{
			intermediateEdge = getPreviousEdgeSegment(target, edge);
		}
		setNextEdgeSegment(source, edge, null);
		setPreviousEdgeSegment(target, edge, null);		
		if(intermediateEdge != null)
		{
			node = (IntermediateNode)intermediateEdge.getSource();
			replaceEdge(node.getTopEdge());
			replaceEdge(node.getBottomEdge());			
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

	private Rectangle2D getBounds(Object node)
	{
		if (node instanceof VirtualNode)
		{
			return ((VirtualNode) node).getBounds();
		}
		CellView view = mapper.getMapping(node, false);
		if (view != null)
		{
			return view.getBounds();
		}
		throw new NullPointerException("No view for " + node);
		//return null;
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
		try
		{
			setRank(node, getRank(node) + rankChange);
		}
		catch(NullPointerException e)
		{
			// Node does exist, do nothing
		}
	}

	public String toString()
	{
		return "Position Tree";
	}

	protected List createInitialTree(Iterator unused)
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
		List result = super.createInitialTree(edges.iterator());
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

				Object tail = getTarget(edge);
				getTailSet(tail, tailSet, edge);
				// getTailSet(getSource(edge), headSet, edge);
				headSet.addAll(getTreeSet(tail));
				headSet.removeAll(tailSet);
				assert !headSet.isEmpty() : edge;
				tightenEdge(edge, headSet, tailSet);
			}
		}
	}
}