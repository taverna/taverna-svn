/*
 * Created on Jan 4, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.CellMapper;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.1 $
 */
public class PositionLayout extends GraphSpanningTree
{
	class Row implements Collection
	{
		private int rowIndex = -1;
		private int groups = 0;

		protected Object start;
		private Object end;

		Row(int rowIndex)
		{
			this.rowIndex = rowIndex;
		}

		/*
		 * @see java.util.Collection#add(java.lang.Object)
		 */
		public boolean add(Object node)
		{
			//System.err.println("Add " + node + " to row " + rowIndex);
			setY(node, (rowIndex * 40) + 10);
			if (isEmpty())
			{
				start = node;
				end = node;
				getRank(node);
			}
			else
			{
				RowEdge edge = new RowEdge(end, node);
				setLeftEdge(node, edge);
				setRightEdge(end, edge);
				end = node;
				// Sort here?

				addEdge(edge);
			}

			if (GraphUtilities.isGroup(model, node))
			{
				groups++;
			}
			return true;
		}

		private void setLeftEdge(Object node, RowEdge edge)
		{
			Map attributes = getAttributes(node);
			LayoutConstants.setLeftEdge(attributes, edge);
		}

		private void setRightEdge(Object node, RowEdge edge)
		{
			Map attributes = getAttributes(node);
			LayoutConstants.setRightEdge(attributes, edge);
		}

		public int indexOf(Object node)
		{
			Iterator nodes = iterator();
			int index = 0;
			while (nodes.hasNext())
			{
				if (node.equals(nodes.next()))
				{
					return index;
				}
				index++;
			}
			return -1;
		}

		public boolean swap(Object node1, Object node2)
		{
			// TODO Check node exists in this row?
			Map attributes = getAttributes(node1);
			RowEdge leftEdge1 = LayoutConstants.getLeftEdge(attributes);
			RowEdge rightEdge1 = LayoutConstants.getRightEdge(attributes);
			attributes = getAttributes(node2);
			RowEdge leftEdge2 = LayoutConstants.getLeftEdge(attributes);
			RowEdge rightEdge2 = LayoutConstants.getRightEdge(attributes);
			if (leftEdge1 != null)
			{
				leftEdge1.setTarget(node2);
			}
			else
			{
				start = node2;
			}
			if (leftEdge2 != null)
			{
				leftEdge2.setTarget(node1);
			}
			else
			{
				start = node1;
			}
			if (rightEdge1 != null)
			{
				rightEdge1.setSource(node2);
			}
			else
			{
				end = node2;
			}
			if (rightEdge2 != null)
			{
				rightEdge2.setSource(node1);
			}
			else
			{
				end = node1;
			}
			return true;
		}
		
		/*
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		public boolean remove(Object node)
		{
			if(!contains(node))
			{
				return false;
			}
			//System.err.println("Remove node " + node + " from row " + rowIndex);			
			Map attributes = getAttributes(node);
			RowEdge leftEdge = LayoutConstants.getLeftEdge(attributes);
			RowEdge rightEdge = LayoutConstants.getRightEdge(attributes);
			if (rightEdge != null)
			{
				if (leftEdge == null)
				{
					start = rightEdge.getTarget();
					setLeftEdge(start, null);
					moveNode(start, null, GRAPH_EDGE);
				}
				else
				{
					rightEdge.setSource(leftEdge.getSource());
					setRightEdge(rightEdge.getSource(), rightEdge);
					if (isTreeEdge(leftEdge))
					{
						if (isTreeEdge(rightEdge))
						{
							tightenEdge(rightEdge, rightEdge.getSource());
						}
					}
					// TODO Else?
				}
			}
			else if (leftEdge != null)
			{
				end = leftEdge.getSource();
				setRightEdge(end, null);
			}
			else
			{
				start = end = null;
			}
			attributes.remove(POSITION_TREE_SET);
			attributes.remove(POSITION_TREE_PARENT);			
			LayoutConstants.setLeftEdge(attributes, null);
			LayoutConstants.setRightEdge(attributes, null);
			return true;
		}

		/**
		 * @return true
		 */
		public boolean isEmpty()
		{
			return start == null;
		}

		/*
		 * @see java.util.Collection#size()
		 */
		public int size()
		{
			int count = 0;
			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				iterator.next();
				count++;
			}
			return count;
		}

		/*
		 * @see java.util.Collection#clear()
		 */
		public void clear()
		{
			start = null;
			end = null;
		}

		/*
		 * @see java.util.Collection#toArray()
		 */
		public Object[] toArray()
		{
			List arrayList = new ArrayList();
			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				arrayList.add(iterator.next());
			}
			return arrayList.toArray();
		}

		/*
		 * @see java.util.Collection#contains(java.lang.Object)
		 */
		public boolean contains(Object o)
		{
			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				if (o.equals(iterator.next()))
				{
					return true;
				}
			}
			return false;
		}

		/*
		 * @see java.util.Collection#addAll(java.util.Collection)
		 */
		public boolean addAll(Collection c)
		{
			boolean changed = false;
			Iterator iterator = c.iterator();
			while (iterator.hasNext())
			{
				changed = add(iterator.next()) || changed;
			}
			return changed;
		}

		/*
		 * @see java.util.Collection#containsAll(java.util.Collection)
		 */
		public boolean containsAll(Collection c)
		{
			Iterator iterator = c.iterator();
			while (iterator.hasNext())
			{
				if (!contains(iterator.next()))
				{
					return false;
				}
			}
			return true;
		}

		/*
		 * @see java.util.Collection#removeAll(java.util.Collection)
		 */
		public boolean removeAll(Collection c)
		{
			boolean removed = false;
			Iterator iterator = c.iterator();
			while (iterator.hasNext())
			{
				removed = remove(iterator.next()) || removed;
			}
			return removed;
		}

		/*
		 * @see java.util.Collection#retainAll(java.util.Collection)
		 */
		public boolean retainAll(Collection c)
		{
			boolean removed = false;
			Iterator iterator = iterator();
			while (iterator.hasNext())
			{
				Object next = iterator.next();
				if (!c.contains(next))
				{
					removed = remove(next) || removed;
				}
			}
			return removed;
		}

		/*
		 * @see java.util.Collection#iterator()
		 */
		public Iterator iterator()
		{
			return new Iterator()
			{
				private Object next = start;
				private Object previous = null;

				public void remove()
				{
					if (previous != null)
					{
						Row.this.remove(previous);
					}
				}

				public boolean hasNext()
				{
					return next != null;
				}

				public Object next()
				{
					previous = next;
					Map attributes = getAttributes(next);
					RowEdge edge = LayoutConstants.getRightEdge(attributes);
					if (edge != null)
					{
						next = edge.getTarget();
					}
					else
					{
						next = null;
					}
					return previous;
				}
			};
		}

		/*
		 * @see java.util.Collection#toArray(java.lang.Object[])
		 */
		public Object[] toArray(Object[] a)
		{
			int size = size();
			if (a.length < size)
			{
				a = (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(),
						size);
			}

			Iterator it = iterator();
			for (int i = 0; i < size; i++)
			{
				a[i] = it.next();
			}

			if (a.length > size)
			{
				a[size] = null;
			}

			return a;
		}
	}

	private static final String POSITION_TREE_PARENT = "psoition tree parent";
	private static final String POSITION_TREE_SET = "position tree set";

	private static final int X_SEPARATION = 10;
	private static final int GRAPH_EDGE = 10;

	protected GraphModel model;

	// private CellMapper mapper;

	/**
	 * @param model
	 * @param mapper
	 */
	public PositionLayout(GraphModel model, CellMapper mapper)
	{
		this.model = model;
		// this.mapper = mapper;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getSource(java.lang.Object)
	 */
	protected Object getSource(Object edge)
	{
		if (edge instanceof RowEdge)
		{
			return ((RowEdge) edge).getSource();
		}
		return null;
	}

	protected int getCutValue(Object edge, String timeStamp)
	{
		if (edge instanceof RowEdge)
		{
			Map attributes = getAttributes(edge);
			if (timeStamp.equals(LayoutConstants.getCutValueTimeStamp(attributes)))
			{
				return LayoutConstants.getCutValue(attributes).intValue();
			}
			int cutValue = super.getCutValue(edge, timeStamp);
			LayoutConstants.setCutValueTimeStamp(attributes, timeStamp);
			LayoutConstants.setCutValue(attributes, cutValue);
			return cutValue;
		}
		return 0;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTarget(java.lang.Object)
	 */
	protected Object getTarget(Object edge)
	{
		if (edge instanceof RowEdge)
		{
			return ((RowEdge) edge).getTarget();
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
		Object leftEdge = LayoutConstants.getLeftEdge(attributes);
		if (leftEdge != null)
		{
			list.add(leftEdge);
		}
		Object rightEdge = LayoutConstants.getRightEdge(attributes);
		if (rightEdge != null)
		{
			list.add(rightEdge);
		}
		return list.iterator();
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getEdgeWeight(java.lang.Object)
	 */
	protected int getEdgeWeight(Object edge)
	{
		if (edge instanceof RowEdge)
		{
			return ((RowEdge) edge).getWeight();
		}
		return 1;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#moveNode(java.lang.Object,
	 *      java.lang.Object, int)
	 */
	protected void setRank(Object node, int x)
	{
		//System.err.println("Set " + node + " = " + x);
		if (node instanceof Point2D)
		{
			Point2D point = (Point2D) node;
			point.setLocation(x, point.getY());
		}
		else if (GraphUtilities.isGroup(model, node))
		{
			int newX = x;
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				Map attributes = getAttributes(model.getChild(node, index));
				assert attributes != null;
				Rectangle2D rect = GraphConstants.getBounds(attributes);
				assert rect != null;
				rect.setFrame(newX, rect.getY(), rect.getWidth(), rect.getHeight());
				newX += rect.getWidth() + 15;
			}
		}
		else
		{
			Map attributes = getAttributes(node);
			assert attributes != null;
			Rectangle2D rect = GraphConstants.getBounds(attributes);
			assert rect != null;
			rect.setFrame(x, rect.getY(), rect.getWidth(), rect.getHeight());
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getRank(java.lang.Object)
	 */
	protected int getRank(Object node)
	{
		if (node instanceof Point2D)
		{
			Point2D point = (Point2D) node;
			int x = (int) point.getX();
			if (x > GRAPH_EDGE)
			{
				return x;
			}
		}
		else
		{
			Map attributes = model.getAttributes(node);
			if (attributes != null)
			{
				Rectangle2D rect = GraphConstants.getBounds(attributes);
				if (rect != null && rect.getX() >= GRAPH_EDGE)
				{
					return (int) rect.getX();
				}
			}
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
		assert attributes != null;
		RowEdge leftEdge = LayoutConstants.getLeftEdge(attributes);
		if (leftEdge != null)
		{
			Object leftNode = leftEdge.getSource();
			return getMinimumRank(leftNode) + getMinimumEdgeLength(leftEdge);
		}
		return GRAPH_EDGE;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#treeSetParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	protected void setTreeParent(Object child, Object parent)
	{
		Object currentParent = null;
		Map attributes = getAttributes(child);
		if (attributes != null)
		{
			currentParent = attributes.get(POSITION_TREE_PARENT);
			attributes.put(POSITION_TREE_PARENT, parent);
			if (parent != null && currentParent != null && currentParent != parent)
			{
				setTreeParent(currentParent, child);
			}
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTreeSet(java.lang.Object)
	 */
	protected Set getTreeSet(Object node)
	{
		Map attributes = getAttributes(node);
		return (Set) attributes.get(POSITION_TREE_SET);
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#setTreeSet(java.lang.Object,
	 *      java.util.Set)
	 */
	protected void setTreeSet(Object node, Set treeSet)
	{
		Map parentAttr = getAttributes(node);
		if (parentAttr != null)
		{
			if (treeSet == null)
			{
				parentAttr.remove(POSITION_TREE_SET);
			}
			else
			{
				parentAttr.put(POSITION_TREE_SET, treeSet);
			}
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getTreeParent(java.lang.Object)
	 */
	protected Object getTreeParent(Object node)
	{
		Map attributes = getAttributes(node);
		if (attributes != null)
		{
			return attributes.get(POSITION_TREE_PARENT);
		}
		return null;
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#removeTreeEdge(java.lang.Object)
	 */
	protected void removeTreeEdge(Object edge)
	{
		Map attributes = getAttributes(edge);
		if (attributes != null)
		{
			attributes.remove(POSITION_TREE_PARENT);
		}
	}

	/*
	 * @see org.embl.ebi.escience.scuflui.graph.GraphSpanningTree#getMaximumRank(java.lang.Object)
	 */
	protected int getMaximumRank(Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null;
		if (attributes != null)
		{
			RowEdge rightEdge = LayoutConstants.getRightEdge(attributes);
			if (rightEdge != null)
			{
				Object rightNode = rightEdge.getSource();
				return getRank(rightNode) - getMinimumEdgeLength(rightEdge);
			}
		}
		return GRAPH_EDGE;
	}

	protected Map getAttributes(Object node)
	{
		if (node instanceof RowEdge)
		{
			return ((RowEdge) node).getAttributes();
		}
		if (node instanceof VirtualNode)
		{
			return ((VirtualNode) node).getAttributes();
		}
		return model.getAttributes(node);
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
		if (edge instanceof RowEdge)
		{
			Object leftNode = ((RowEdge) edge).getSource();
			Map leftAttributes = getAttributes(leftNode);
			Rectangle2D leftRect = GraphConstants.getBounds(leftAttributes);
			if (leftRect != null)
			{
				return (int) leftRect.getWidth() + X_SEPARATION;
			}
			return X_SEPARATION;
		}
		return 0;
	}

	protected void setY(Object node, int y)
	{
		if (node instanceof Point2D)
		{
			Point2D point = (Point2D) node;
			point.setLocation(point.getX(), y + 10);
		}
		else if (GraphUtilities.isGroup(model, node))
		{
			for (int index = 0; index < model.getChildCount(node); index++)
			{
				Map attributes = model.getAttributes(model.getChild(node, index));
				assert attributes != null;
				Rectangle2D rect = GraphConstants.getBounds(attributes);
				assert rect != null;
				rect.setFrame(rect.getX(), y, rect.getWidth(), rect.getHeight());
			}
		}
		else
		{
			Map attributes = model.getAttributes(node);
			assert attributes != null;
			Rectangle2D rect = GraphConstants.getBounds(attributes);
			assert rect != null;
			rect.setFrame(rect.getX(), y, rect.getWidth(), rect.getHeight());
		}
	}

}