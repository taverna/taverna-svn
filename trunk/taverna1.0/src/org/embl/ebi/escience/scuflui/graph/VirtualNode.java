/*
 * Created on Feb 15, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.1 $
 */
public class VirtualNode extends Point2D
{
	private int x;
	private int y;
	private Map attributes = new HashMap();

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
	
	/*
	 * @see java.awt.geom.Point2D#getX()
	 */
	public double getX()
	{
		return x;
	}

	/*
	 * @see java.awt.geom.Point2D#getY()
	 */
	public double getY()
	{
		return y;
	}

	/*
	 * @see java.awt.geom.Point2D#setLocation(double, double)
	 */
	public void setLocation(double x, double y)
	{
		this.x = (int) x;
		this.y = (int) y;
	}
	
	public String toString()
	{
		return "VirtualNode";
	}	
}