package uk.org.mygrid.dataplaygroundui;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.ShapePickSupport;

public class PlaygroundPickSupport extends ShapePickSupport {

	/**
	 * 
	 * Code extracted from ShapePickSupport with Slight modification to be able
	 * to specifiy a vertex which isn't included in the getVertex() allowing for
	 * example dragging vertex A over to Vertex B and finding Vertex B rather
	 * than constantly finding Vertex A because it is being dragged!
	 * 
	 * 
	 * Iterates over Vertices, checking to see if x,y is contained in the
	 * Vertex's Shape. If (x,y) is contained in more than one vertex, use the
	 * vertex whose center is closest to the pick point. Ignores the Vertex
	 * passed as the argument.
	 * 
	 * @see edu.uci.ics.jung.visualization.PickSupport#getVertex(double, double)
	 */
	public Vertex getVertex(double x, double y, Vertex ignore) {
		Layout layout = hasGraphLayout.getGraphLayout();

		Vertex closest = null;
		double minDistance = Double.MAX_VALUE;
		while (true) {
			try {

				for (Iterator iter = layout.getGraph().getVertices().iterator(); iter
						.hasNext();) {
					if (hasShapeFunctions != null) {
						Vertex v = (Vertex) iter.next();
						if (v == ignore)
							continue;
						Shape shape = hasShapeFunctions
								.getVertexShapeFunction().getShape(v);
						// transform the vertex location to screen coords
						Point2D p = layoutTransformer.layoutTransform(layout
								.getLocation(v));
						if (p == null)
							continue;
						AffineTransform xform = AffineTransform
								.getTranslateInstance(p.getX(), p.getY());
						shape = xform.createTransformedShape(shape);
						// see if this vertex center is closest to the pick
						// point
						// among any other containing vertices
						if (shape.contains(x, y)) {

							Rectangle2D bounds = shape.getBounds2D();
							double dx = bounds.getCenterX() - x;
							double dy = bounds.getCenterY() - y;
							double dist = dx * dx + dy * dy;
							if (dist < minDistance) {
								minDistance = dist;
								closest = v;
							}
						}
					}
				}
				break;
			} catch (ConcurrentModificationException cme) {
			}
		}
		return closest;
	}

}
