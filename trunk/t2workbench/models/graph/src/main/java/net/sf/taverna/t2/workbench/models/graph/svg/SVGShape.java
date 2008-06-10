package net.sf.taverna.t2.workbench.models.graph.svg;

import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;

public interface SVGShape {

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

	public void setErrors(final int errors);

	public void setCompleted(final float complete);

}