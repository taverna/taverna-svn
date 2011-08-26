package net.sf.taverna.t2.workbench.models.graph.svg;

import org.apache.batik.dom.svg.SVGOMPolygonElement;

public interface SVGMonitorShape extends SVGShape {

	/**
	 * Returns the polygon used to display the completed value.
	 *
	 * @return the polygon used to display the completed value
	 */
	public SVGOMPolygonElement getCompletedPolygon();

	/**
	 * Sets the polygon used to display the completed value.
	 *
	 * @param polygon the new polygon  used to display the completed value
	 */
	public void setCompletedPolygon(SVGOMPolygonElement polygon);

}
