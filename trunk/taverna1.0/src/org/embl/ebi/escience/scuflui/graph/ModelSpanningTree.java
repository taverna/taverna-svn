/*
 * Created on Mar 8, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.GraphModel;

public abstract class ModelSpanningTree extends GraphSpanningTree
{
	protected static final String TREE_SET = " set";
	protected static final String TREE_EDGE = " edge";		
	
	protected static final String CUT_VALUE = "cut edge";
	protected static final String CUT_TIME_STAMP = "cut time stamp";		
	
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

	protected Set getTreeSet(Object node)
	{
		Map attributes = getAttributes(node);
		assert attributes != null: this + ": " + node;
		Set treeSet = (Set) attributes.get(this + TREE_SET);
		if(treeSet == null)
		{
			treeSet = new HashSet();
			treeSet.add(node);
			attributes.put(this+TREE_SET, treeSet);
		}
		return treeSet;
	}

	protected Object getSource(Object edge)
	{
		// TODO Implement getSource
		return null;
	}

	protected boolean isTreeEdge(Object edge)
	{
		Map attributes = getAttributes(edge);
		assert attributes != null: this + ": " + edge;
		return attributes.containsKey(this + TREE_EDGE);
	}
	
	protected void removeEdge(Object edge)
	{
		if(isTreeEdge(edge))
		{
			setTreeEdge(edge, false);
			//System.err.println(this + ": Removed tree edge " + edge);
		}
	}

	protected Integer getCutValue(Object treeEdge, String timeStamp)
	{
		Map attributes = getAttributes(treeEdge);
		assert attributes != null: this + ": " + treeEdge;
		if (timeStamp.equals(attributes.get(CUT_TIME_STAMP)))
		{
			return ((Integer)attributes.get(CUT_VALUE));
		}
		return null;
	}
	
	protected void setCutValue(Object treeEdge, String timeStamp, int cutValue)
	{
		Map attributes = getAttributes(treeEdge);
		assert attributes != null: this + ": " + treeEdge;
		attributes.put(CUT_TIME_STAMP, timeStamp);
		attributes.put(CUT_VALUE, new Integer(cutValue));
	}	

	protected void setTreeSet(Object node, Set treeSet)
	{
		Map attributes = getAttributes(node);
		assert attributes != null: this;
		if(treeSet != null)
		{
			attributes.put(this + TREE_SET, treeSet);			
		}
		else
		{
			attributes.remove(this + TREE_SET);
		}
	}

	protected void setTreeEdge(Object edge, boolean isTreeEdge)
	{
		Map attributes = getAttributes(edge);
		assert attributes != null: this;
		if(isTreeEdge)
		{
			attributes.put(this + TREE_EDGE, Boolean.TRUE);
		}
		else
		{
			attributes.remove(this + TREE_EDGE);
		}
	}
}