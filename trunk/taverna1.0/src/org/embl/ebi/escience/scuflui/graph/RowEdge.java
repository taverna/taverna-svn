/*
 * Created on Feb 9, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.util.HashMap;
import java.util.Map;

class RowEdge
{
	private Object source;
	private Object target;
	private Map attributes = new HashMap();

	public RowEdge(Object source, Object target)
	{
		setSource(source);
		setTarget(target);
	}

	public void setSource(Object node)
	{
		if(node == null)
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
		if(node == null)
		{
			throw new NullPointerException("Oi!");
		}
		target = node;
	}

	public String toString()
	{
		return source.toString() + "->" + target.toString();
	}
	
	public int getWeight()
	{
		return 0;
	}
}