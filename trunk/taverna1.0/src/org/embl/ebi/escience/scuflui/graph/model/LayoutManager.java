/*
 * Created on Dec 6, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.1 $
 */
public class LayoutManager implements GraphModelListener
{
	private class EdgeList extends ArrayList
	{
		public boolean add(Object edge)
		{
			int length = getEdgeLength(edge);
			for(int index = 0; index < size(); index++)
			{
				if(length < getEdgeLength(get(index)))
				{
					add(index, edge);
					return true;
				}
			}
			return super.add(edge);
		}
	}
	
	private static final String EDGE_PARENT = "tree edge parent";
	private static final String TREE_SET = "tree set";
	private static final String LOW = "low";
	private static final String LIM = "lim";

	// private static final String LAYOUT_MEDIAN = "layout median";

	private GraphModel model;
	private GraphRows rows;
	
	/**
	 * @param model
	 */
	public LayoutManager(GraphModel model)
	{
		super();
		this.model = model;
		rows = new GraphRows(model);
		model.addGraphModelListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
	 */
	public void graphChanged(GraphModelEvent e)
	{
		List edges = new EdgeList();

		Object[] removed = e.getChange().getRemoved();
		if (removed != null)
		{
			for (int index = 0; index < removed.length; index++)
			{
				if (model.isEdge(removed[index]))
				{
					if (isTreeEdge(removed[index]))
					{
						treeRemoveEdge(removed[index]);
					}
					else
					{
						rows.remove(removed[index]);
					}
				}
				else
				{
					Object root = GraphUtilities.getRoot(model,removed[index]);
					if(!model.contains(root))
					{
						rows.remove(root);
						getTreeSet(root).remove(root);
					}
				}
			}
		}
		
		Object[] inserted = e.getChange().getInserted();
		if (inserted != null)
		{
			for (int index = 0; index < inserted.length; index++)
			{
				if (model.isEdge(inserted[index]))
				{
					//TODO Check both ends aren't null
					edges.add(inserted[index]);
				}
				else
				{
					rows.getRow(GraphUtilities.getRoot(model, inserted[index]));
				}
			}
		}
		
		while(!edges.isEmpty())
		{
			Object edge = edges.get(0);
			edges.remove(0);
			if(!isTreeEdge(edge))
			{
				System.err.println(edge);
				Object source = GraphUtilities.getSourceNode(model, edge);
				Object target = GraphUtilities.getTargetNode(model, edge);
				Set sourceTreeSet = getTreeSet(source);
				Set targetTreeSet = getTreeSet(target);
				if(sourceTreeSet != targetTreeSet)
				{
					int newRow = 0;
					Object parent = null;
					Object child = null;
					Object movedNode = null;
					if(sourceTreeSet == null)
					{
						newRow = rows.getRow(target) - 1;
						parent = target;
						child = source;
						movedNode = source;
					}
					else if(targetTreeSet == null)
					{
						newRow = rows.getRow(target) - 1;
						parent = source;
						child = target;
						movedNode = source;
					}
					else
					{
						if(targetTreeSet.size() > sourceTreeSet.size())
						{
							newRow = rows.getRow(target) - 1;
							parent = target;
							child = source;
							movedNode = source;
						}
						else
						{
							newRow = rows.getRow(source) + 1;
							parent = source;
							child = target;
							movedNode = target;
						}
					}
					adjustTree(movedNode, newRow);
					treeAddEdge(parent, child, edge);
					treeAdd(child, edges);
				}
				else
				{
					if(sourceTreeSet == null)
					{
						treeAddEdge(source, target, edge);
						treeAdd(target, edges);					
					}
					else
					{
						// TODO Check below max?
						if(rows.getRow(target) < rows.getMinimumRow(target))
						{
							// TODO New tree edge. Find existing path and break.							
						}
						else
						{
							// Non-tree edge, so add as virtual node chain.
							rows.addEdge(edge);							
						}
					}
				}
			}
		}
	}

	/**
	 * @param source
	 * @param newRow
	 */
	private void adjustTree(Object node, int row)
	{
		rows.setRow(node, row);
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] {node}).iterator();
		while(edges.hasNext())
		{
			Object edge = edges.next();
			if(isTreeEdge(edge))
			{
				int newRow = row - 1;
				Object neighbour = GraphUtilities.getSourceNode(model,edge);
				if(neighbour.equals(node))
				{
					newRow = row + 1;
					neighbour = GraphUtilities.getTargetNode(model, edge);
				}
				
				int neighbourRow = rows.getRow(neighbour);
				if(newRow != neighbourRow)
				{
					adjustTree(neighbour, newRow);
				}
			}
		}
	}
	
	private Set getTreeSet(Object node)
	{
		Map attributes = model.getAttributes(node);
		return (Set) attributes.get(TREE_SET);
	}
	
	private void setTreeSet(Object node, Set treeSet)
	{
		Map parentAttr = model.getAttributes(node);
		parentAttr.put(TREE_SET, treeSet);
	}

	private void addTailSet(Object node, Set tailSet)
	{
		tailSet.add(node);
		Object parent = getParent(node);		
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] {node}).iterator();
		while(edges.hasNext())
		{
			Object edge = edges.next();
			if(!edge.equals(parent) && isTreeEdge(edge))
			{
				addTailSet(GraphUtilities.getNeighbour(model, node, edge), tailSet);
			}
		}
	}
	
	private int calculateLim(Object node, int currentLim)
	{
		int low = currentLim + 1;
		Object parent = getParent(node);
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] {node}).iterator();
		while (edges.hasNext())
		{
			Object edge = edges.next();
			if(!edge.equals(parent) && isTreeEdge(edge))
			{
				Object neighbour = GraphUtilities.getNeighbour(model, node, edge);
				currentLim = calculateLim(neighbour, currentLim);
				low = Math.max(low, currentLim);
			}
		}
		int lim = currentLim + 1;
		Map attributes = model.getAttributes(node);
		attributes.put(LOW, new Integer(low));
		attributes.put(LIM, new Integer(lim));
		return lim;
	}

	int getEdgeLength(Object edge)
	{
		return rows.getRow(GraphUtilities.getTargetNode(model, edge))
				- rows.getRow(GraphUtilities.getSourceNode(model, edge));
	}

	private Object getParent(Object node)
	{
		Map attributes = model.getAttributes(node);
		if (attributes != null)
		{
			return attributes.get(EDGE_PARENT);
		}
		return null;
	}

	private boolean isParent(Object parent, Object node)
	{
		Object nodeParent = getParent(node);
		if(node.equals(nodeParent))
		{
			return true;
		}
		else if(nodeParent == null)
		{
			return false;
		}
		else
		{
			return isParent(parent, nodeParent);
		}
	}
	
	private boolean isTreeEdge(Object edge)
	{
		Map attributes = model.getAttributes(edge);
		if (attributes != null)
		{
			Object treeEdgeParent = attributes.get(EDGE_PARENT);
			if (treeEdgeParent != null)
			{
				return true;
			}
		}
		return false;
	}

	private void treeAdd(Object parent, Collection slackEdges)
	{
		Iterator edges = DefaultGraphModel.getEdges(model, new Object[] { parent }).iterator();
		while (edges.hasNext())
		{
			// Follow all tight edges
			Object edge = edges.next();
			if (!isTreeEdge(edge))
			{
				Object child = GraphUtilities.getNeighbour(model, parent, edge);
				if (getTreeSet(child) == null)
				{
					int parentRow = rows.getRow(parent);
					int neighbourRow = rows.getRow(child);
					if (Math.abs(neighbourRow - parentRow) == 1)
					{
						treeAddEdge(parent, child, edge);
						treeAdd(child, slackEdges);
					}
					else
					{
						slackEdges.add(edge);
					}
				}
			}
		}
	}

	/**
	 * @param parent
	 * @param child
	 * @param edge
	 */
	private void treeAddEdge(Object parent, Object child, Object edge)
	{
		System.err.println("Add tree edge " + edge);
		Set tree1 = getTreeSet(parent);
		Set tree2 = getTreeSet(child);
		if (tree1 == null)
		{
			tree1 = new HashSet();
			tree1.add(parent);
			setTreeSet(parent, tree1);
		}
		if (tree2 == null)
		{
			tree1.add(child);
			setTreeSet(child, tree1);
		}
		else
		{
			if (tree1 != tree2)
			{
				if (tree2.size() > tree1.size())
				{
					Set temp = tree1;
					tree1 = tree2;
					tree2 = temp;
				}
				tree1.addAll(tree2);
				Iterator nodes = tree2.iterator();
				while (nodes.hasNext())
				{
					setTreeSet(nodes.next(), tree1);
				}
			}
		}

		Map attributes = model.getAttributes(edge);
		attributes.put(EDGE_PARENT, parent);	
		//GraphConstants.setLineColor(attributes, Color.RED);
		setParent(child, edge);
	}

	private void treeRemoveEdge(Object edge)
	{
		System.err.println("Remove tree edge " + edge);
		Object parent = getParent(edge);
		Object tailParent = GraphUtilities.getNeighbour(model, parent, edge);
		Set tailSet = new HashSet();
		addTailSet(tailParent, tailSet);
		Set headSet = new HashSet();
		headSet.addAll(getTreeSet(parent));
		headSet.removeAll(tailSet);
		// Get all non-tree edges connecting the head and tail of this edge
		List joiningEdges = new EdgeList();
		for(int index = 0; index < model.getRootCount(); index++)
		{
			Object otherEdge = model.getRootAt(index);
			if(model.isEdge(otherEdge))
			{
				if(!isTreeEdge(otherEdge))
				{
					Object source = GraphUtilities.getSourceNode(model, otherEdge);
					Object target = GraphUtilities.getTargetNode(model, otherEdge);									
					if(headSet.contains(source))
					{
						if(tailSet.contains(target))
						{
							joiningEdges.add(otherEdge);
						}
					}
					else if(tailSet.contains(source))
					{
						if(headSet.contains(target))
						{
							joiningEdges.add(otherEdge);
						}
					}
				}
			}
		}
		if(joiningEdges.isEmpty())
		{
			normalize(headSet);
			normalize(tailSet);
		}
		else
		{
			// Else replace this tree edge with a non-tree edge
			Object joinEdge = joiningEdges.get(0);
			Object source = GraphUtilities.getSourceNode(model, joinEdge);
			Object joinParent = source;
			Object child = GraphUtilities.getTargetNode(model, joinEdge);
			int newRow = rows.getRow(child) - 1;
			if(!headSet.contains(source))
			{
				joinParent = child;
				child = source;
			}
			adjustTree(source, newRow);
			treeAddEdge(joinParent, child, joinEdge);
		}
	}
	
	private void normalize(Set tree)
	{
		Iterator nodes = tree.iterator();
		Object lowestNode = null;
		int lowestRow = Integer.MAX_VALUE;
		while(nodes.hasNext())
		{
			Object node = nodes.next();
			setTreeSet(node, tree);
			int row = rows.getRow(node);
			if(row < lowestRow)
			{
				lowestRow = row;
				lowestNode = node;
			}
		}
		if(lowestRow > 0)
		{
			adjustTree(lowestNode, 0);
		}
	}
	
	/**
	 * @param child
	 * @param edge
	 */
	private void setParent(Object child, Object edge)
	{
		Map attributes = model.getAttributes(child);
		Object currentParent = attributes.get(EDGE_PARENT);
		if(currentParent != null && currentParent != edge)
		{
			Object oldNode = getParent(currentParent);
			Map edgeAttr = model.getAttributes(currentParent);
			edgeAttr.put(EDGE_PARENT, child);
			setParent(oldNode, currentParent);
		}
		attributes.put(EDGE_PARENT, edge);
	}

	// private void updatePositions()
	// {
	// Map positions = initialPositions();
	// model.edit(positions, null, null, null);
	// }
}