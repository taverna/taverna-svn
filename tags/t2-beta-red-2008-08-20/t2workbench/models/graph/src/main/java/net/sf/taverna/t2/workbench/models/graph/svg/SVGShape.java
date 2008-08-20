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