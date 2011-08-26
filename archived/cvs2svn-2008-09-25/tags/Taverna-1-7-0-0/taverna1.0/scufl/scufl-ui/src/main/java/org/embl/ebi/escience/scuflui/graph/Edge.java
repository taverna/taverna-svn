/*
 * Created on Feb 9, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.HashMap;
import java.util.Map;

class Edge
{
	private int weight;
	private Object source;
	private Object target;
	private Map attributes = new HashMap();

	public Edge(Object source, Object target)
	{
		this(source, target, 0);
	}

	public Edge(Object source, Object target, int weight)
	{
		assert source != null;
		assert target != null;
		setSource(source);
		setTarget(target);
		this.weight = weight;
		assert (source != target) : this;
	}

	public void setSource(Object node)
	{
		if (node == null)
		{
			throw new NullPointerException("Oi!");
		}
		source = node;
	}

	public Object getSource()
	{
		return source;
	}

	public Map getAttributes()
	{
		return attributes;
	}

	public Object getTarget()
	{
		return target;
	}

	public void setTarget(Object node)
	{
		if (node == null)
		{
			throw new NullPointerException("Oi!");
		}
		target = node;
	}

	//
	// public boolean equals(Object obj)
	// {
	// if(obj instanceof Edge)
	// {
	// Edge edge = (Edge)obj;
	// return source.equals(edge.source) && target.equals(edge.target);
	// }
	// return false;
	// }

	public String toString()
	{
		return source.toString() + "->" + target.toString();
	}

	public int getWeight()
	{
		return weight;
	}
}
