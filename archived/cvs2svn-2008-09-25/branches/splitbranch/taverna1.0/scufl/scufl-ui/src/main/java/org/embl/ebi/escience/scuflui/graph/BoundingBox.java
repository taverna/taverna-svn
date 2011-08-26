/*
 * Created on Apr 27, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.geom.Rectangle2D;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.1.2.1 $
 */
public abstract class BoundingBox extends Rectangle2D
{

	/**
	 * 
	 */
	public BoundingBox()
	{
		super();
		// TODO Implement BoundingBox constructor
	}

	public abstract void translate(int dx, int dy);

	public String toString()
	{
		return "BoundingBox[" + getX() + ", " + getY() + ", " + getWidth() + ", " + getHeight()
				+ "]";
	}
}
