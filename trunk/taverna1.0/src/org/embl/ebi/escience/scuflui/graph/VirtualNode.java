/*
 * Created on Feb 15, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover </a>
 * @version $Revision: 1.4 $
 */
public class VirtualNode
{
	Point2D position = new Point();
	private Map attributes = new HashMap();
	protected Edge previous;
	protected Edge next;
	protected Object edge;
	int row;

	public VirtualNode(int row, Object edge)
	{
		this.edge = edge;
		this.row = row;
		LayoutConstants.setRow(attributes, row);		
	}
	
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

	public Map getAttributes()
	{
		return attributes;
	}

	public String toString()
	{
		return "VirtualNode[" + edge + ", " + row + "]";
	}

	public Point2D getPosition()
	{
		return position;
	}
	
	public Rectangle2D getBounds()
	{
		return new Rectangle2D()
		{
			public int outcode(double x, double y)
			{
				// TODO Implement outcode
				return 0;
			}

			public void setRect(double x, double y, double w, double h)
			{
				position.setLocation(x,y);				
			}

			public Rectangle2D createIntersection(Rectangle2D r)
			{
				// TODO Implement createIntersection
				return null;
			}

			public Rectangle2D createUnion(Rectangle2D r)
			{
				// TODO Implement createUnion
				return null;
			}

			public double getHeight()
			{
				return 0;
			}

			public double getWidth()
			{
				return 0;
			}

			public double getX()
			{
				return position.getX();
			}

			public double getY()
			{
				return position.getY();
			}

			public boolean isEmpty()
			{
				return true;
			}
		};
	}
}