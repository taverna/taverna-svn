/*
 * Created on Feb 15, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.2 $
 */
public class VirtualNode
{
	private Point2D position = new Point();
	private Map attributes = new HashMap();
	protected Edge previous;
	protected Edge next;

	public Edge getNextEdge()
	{
		return next;
	}

	public void setNextEdge(Edge next)
	{
		this.next = next;
	}

	public Edge getPreviousEdge()
	{
		return previous;
	}

	public void setPreviousEdge(Edge previous)
	{
		this.previous = previous;
	}

	/**
	 * 
	 */
	public VirtualNode()
	{
		super();
	}

	public Map getAttributes()
	{
		return attributes;
	}

	public String toString()
	{
		return "VirtualNode";
	}

	public Point2D getPosition()
	{
		return position;
	}
}