/*
 * Created on Mar 8, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.Map;
import java.util.Set;

import org.jgraph.graph.GraphModel;

public abstract class ModelSpanningTree extends GraphSpanningTree
{
	private static final String TREE_PARENT = " parent";	
	private static final String TREE_SET = " set";	
	
	private static final String CUT_VALUE = "cut edge";
	private static final String CUT_TIME_STAMP = "cut time stamp";		
	
	protected GraphModel model;
	
	public ModelSpanningTree(GraphModel model)
	{
		super();
		this.model = model;
	}

	protected Map getAttributes(Object node)
	{
		assert node != null: this;
		if (node instanceof Edge)
		{
			return ((Edge) node).getAttributes();
		}
		if (node instanceof VirtualNode)
		{
			return ((VirtualNode) node).getAttributes();
		}
		return model.getAttributes(node);
	}	
	
	protected Object getTreeParent(Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null: this + ": " + node;
		return attributes.get(this + TREE_PARENT);
	}

	protected Set getTreeSet(Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null: this + ": " + node;
		return (Set) attributes.get(this + TREE_SET);
	}

	protected void removeTreeEdge(Object edge)
	{
		Map attributes = getAttributes(edge);
		assert attributes != null: this + ": " + edge;
		attributes.remove(this + TREE_PARENT);
	}

	protected int getCutValue(Object treeEdge, String timeStamp)
	{
		Map attributes = getAttributes(treeEdge);
		assert attributes != null: this + ": " + treeEdge;
		if (timeStamp.equals(attributes.get(CUT_TIME_STAMP)))
		{
			return ((Integer)attributes.get(CUT_VALUE)).intValue();
		}
		int cutValue = super.getCutValue(treeEdge, timeStamp);
		attributes.put(CUT_TIME_STAMP, timeStamp);
		attributes.put(CUT_VALUE, new Integer(cutValue));
		return cutValue;
	}

	protected void setTreeParent(Object child, Object parent)
	{
		// System.err.println("Set tree parent of " + child + " as " + parent);
		Map attributes = getAttributes(child);
		assert attributes != null;
		assert parent != null;
		Object currentParent = attributes.get(this + TREE_PARENT);
		attributes.put(this + TREE_PARENT, parent);
		if (parent != null && currentParent != null && currentParent != parent)
		{
			setTreeParent(currentParent, child);
		}
	}
	
	protected void removeFromTree(Map attributes)
	{
		attributes.remove(this + TREE_PARENT);
		attributes.remove(this + TREE_SET);		
	}

	protected void setTreeSet(Object node, Set treeSet)
	{
		Map attributes = getAttributes(node);
		assert attributes != null: this;
		assert treeSet != null: this + ": " + node;
		attributes.put(this + TREE_SET, treeSet);
	}
}