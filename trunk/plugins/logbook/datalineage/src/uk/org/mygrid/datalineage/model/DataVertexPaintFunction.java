/**
 * 
 */
package uk.org.mygrid.datalineage.model;

import java.awt.Color;
import java.awt.Paint;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.visualization.PickedInfo;

public class DataVertexPaintFunction implements VertexPaintFunction {

	private PickedInfo pickedInfo;

	public DataVertexPaintFunction(PickedInfo pickedInfo) {
		this.pickedInfo = pickedInfo;
	}

	public Paint getDrawPaint(Vertex v) {
		return Color.BLACK;
	}

	public Paint getFillPaint(Vertex v) {
		if (pickedInfo.isPicked(v))
			return Color.WHITE;
		if (v instanceof EmptyVertex)
			return Color.RED;
//		if (v instanceof OutputCollectionVertex)
//			return Color.MAGENTA;
//		if (v instanceof OutputItemVertex)
//			return Color.GREEN;
		Color color = Color.ORANGE;
		if (v instanceof DataCollectionVertex)
			color = Color.YELLOW.darker();
		if (v instanceof OutputVertex) {
			color = new Color(135,206, 250);
		} else if (v instanceof InputVertex) {
			color = new Color(30, 144, 255);
		}
		return color;
	}

}