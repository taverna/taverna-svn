/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D.Double;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;

public class DataLineageVertexShapeFunction implements VertexShapeFunction {

	public Shape getShape(Vertex v) {
		if (v instanceof OutputVertex) {
			DataVertex dataVertex = (DataVertex) v;
			double width = dataVertex.toString().length() * 5.0;
			double height = 20.0;
			double arc = 15.0;
			Double rectangle = new Double(-(width / 2.0), -10.0, width, height,
					arc, arc);
			return rectangle;
		}
		if (v instanceof InputVertex) {
			DataVertex dataVertex = (DataVertex) v;
			double width = dataVertex.toString().length() * 5.0;
			double height = 20.0;
			double arc = 25.0;
			Double rectangle = new Double(-(width / 2.0), -10.0, width, height,
					arc, arc);
			return rectangle;
		}
		if (v instanceof DataVertex) {
			DataVertex dataVertex = (DataVertex) v;
			int width = dataVertex.toString().length() * 5;
			int height = 20;
			Rectangle rectangle = new Rectangle(-(width / 2), -10, width,
					height);
			return rectangle;
		}
		if (v instanceof ProcessorVertex)
			return new Rectangle(-30, -15, 60, 30);
		throw new IllegalArgumentException("Vertex of type "
				+ v.getClass().getName());
	}

}