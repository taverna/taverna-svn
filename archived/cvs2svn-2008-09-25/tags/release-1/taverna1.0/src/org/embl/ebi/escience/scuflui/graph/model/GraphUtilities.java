/*
 * Created on Dec 16, 2004
 */
package org.embl.ebi.escience.scuflui.graph.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphModel;

/**
 * COMMENT 
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.5 $
 */
public class GraphUtilities
{
	
	/**
	 * @param model 
	 * @return all edges in the model
	 */
	public static Set getAllEdges(GraphModel model)
	{
		List cells = DefaultGraphModel.getDescendants(model, (DefaultGraphModel.getRoots(model)));
		if (cells != null)
		{
			Set result = new HashSet();
			for (int index = 0; index < cells.size(); index++)
				if (model.isEdge(cells.get(index)))
					result.add(cells.get(index));
			return result;
		}
		return null;
	}	
	
	/**
	 * @param model
	 * @param edge
	 * @return root source node of edge
	 */
	public static Object getSourceNode(GraphModel model, Object edge)
	{
		return getRoot(model, model.getSource(edge));
	}
	
	/**
	 * @param model
	 * @param edge
	 * @return root target node of edge
	 */
	public static Object getTargetNode(GraphModel model, Object edge)
	{
		return getRoot(model, model.getTarget(edge));
	}	
	
	/**
	 * @param model
	 * @param node
	 * @return root node of object
	 */
	public static Object getRoot(GraphModel model, Object node)
	{
		Object result = node;
		Object parent = null;
		while ((parent = model.getParent(result)) != null)
		{
			result = parent;
		}		
		return result;
	}
	
	/**
	 * @param model
	 * @param parent
	 * @param node
	 * @return true if parent is a parent of node
	 */
	public static boolean isParentOf(GraphModel model, Object parent, Object node)
	{
		Object temp = node;
		while((temp = model.getParent(temp)) != null)
		{
			if(parent == temp)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param model
	 * @param node
	 * @return all the edges entering the given <code>node</code>
	 */
	public static Set getIncomingEdges(GraphModel model, Object node)
	{
		Set result = new HashSet();
		Set edges = DefaultGraphModel.getEdges(model, new Object[] { node });
		Iterator it = edges.iterator();

		while (it.hasNext())
		{
			Object edge = it.next();
			if (!isGroup(model, edge))
			{
				Object target = model.getTarget(edge);
				if (target == node || isParentOf(model, node, target))
					result.add(edge);
			}
		}
		return result;
	}

	/**
	 * @param model
	 * @param node
	 * @param edge
	 * @return get neighbour node
	 */
	public static Object getNeighbour(GraphModel model, Object node, Object edge)
	{
		Object source = getSourceNode(model, edge);
		if (node == source)
		{
			return getTargetNode(model, edge);
		}
		return source;		
	}
	
	/**
	 * @param model
	 * @param node
	 * @return all the edges leaving the given <code>node</code>
	 */
	public static Set getOutgoingEdges(GraphModel model, Object node)
	{
		Set result = new HashSet();
		Set edges = DefaultGraphModel.getEdges(model, new Object[] { node });
		Iterator it = edges.iterator();
		while (it.hasNext())
		{
			Object edge = it.next();
			if (!isGroup(model, edge))
			{
				Object source = model.getSource(edge);
				Object parent = model.getParent(source);
				if (source == node || parent == node)
					result.add(edge);
			}
		}
		return result;
	}
	
	/**
	 * @param model
	 * @param node
	 * @return <code>true</code> if <code>node</code> contains other nodes, <code>false</code> if it only contains ports or is empty.
	 */
	public static boolean isGroup(GraphModel model, Object node)
	{
		for (int i = 0; i < model.getChildCount(node); i++)
		{
			if (!model.isPort(model.getChild(node, i)))
				return true;
		}
		return false;
	}	
}
