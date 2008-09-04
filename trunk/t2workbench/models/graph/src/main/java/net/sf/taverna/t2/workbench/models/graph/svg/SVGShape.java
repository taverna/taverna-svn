/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.models.graph.svg;

import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;

public interface SVGShape {

	/**
	 * Returns the graphController.
	 *
	 * @return the graphController
	 */
	public SVGGraphController getGraphController();

	/**
	 * Sets the graphController.
	 *
	 * @param graphComponent the new graphController
	 */
	public void setGraphController(SVGGraphController graphController);

	/**
	 * Returns the g.
	 *
	 * @return the g
	 */
	public SVGOMGElement getG();

	/**
	 * Sets the g.
	 *
	 * @param g the new g
	 */
	public void setG(SVGOMGElement g);

	/**
	 * Returns the polygon.
	 *
	 * @return the polygon
	 */
	public SVGOMPolygonElement getPolygon();

	/**
	 * Sets the polygon.
	 *
	 * @param polygon the new polygon
	 */
	public void setPolygon(SVGOMPolygonElement polygon);

	/**
	 * Returns the ellipse.
	 *
	 * @return the ellipse
	 */
	public SVGOMEllipseElement getEllipse();

	/**
	 * Sets the ellipse.
	 *
	 * @param ellipse the new ellipse
	 */
	public void setEllipse(SVGOMEllipseElement ellipse);

	/**
	 * Returns the text.
	 *
	 * @return the text
	 */
	public SVGOMTextElement getText();

	/**
	 * Sets the text.
	 *
	 * @param text the new text
	 */
	public void setText(SVGOMTextElement text);

	public void setIteration(final int iteration);

//	public void setErrors(final int errors);
//
//	public void setCompleted(final float complete);

}
