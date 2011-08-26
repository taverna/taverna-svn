package uk.org.mygrid.dataplaygroundui;

import java.awt.Color;
import java.awt.Paint;

import uk.org.mygrid.dataplayground.PlaygroundDataObject;
import uk.org.mygrid.dataplayground.PlaygroundDataThing;
import uk.org.mygrid.dataplayground.PlaygroundPortObject;
import uk.org.mygrid.dataplayground.PlaygroundProcessorObject;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.PickableVertexPaintFunction;
import edu.uci.ics.jung.visualization.PickedInfo;

public class PlaygroundVertexPaintFunction extends PickableVertexPaintFunction {

	private Paint highlighted_paint = Color.MAGENTA;
	private Paint processor_paint = new Color(0,203,0);//green
	private Paint port_paint = new Color(204,204,204);//grey
	private Paint dataObject_paint = new Color(255,153,0);//orange
	private Paint dataThing_paint = new Color(0,153,255);//blue
	private Vertex highlighted;
		
	
	public PlaygroundVertexPaintFunction(PickedInfo pi) {
		super(pi, Color.BLACK, Color.RED, Color.ORANGE);
	}

	@Override
	public Paint getFillPaint(Vertex v) {

	   if(v == highlighted)
			return highlighted_paint;
			
	   if(v instanceof PlaygroundProcessorObject){
		
		   if(((PlaygroundProcessorObject)v).isRunning())
			   return Color.RED;
		   
		   return processor_paint;
	   
	   
	   }
	   if(v instanceof PlaygroundPortObject)
		   return port_paint;
	   
	   if(v instanceof PlaygroundDataObject)
		   return dataObject_paint;
	   
	   if(v instanceof PlaygroundDataThing)
		   return dataThing_paint;
	   
	   return super.getFillPaint(v);
	}

	
	public void setHighlighted(Vertex v){
	
		highlighted = v;
		
	}
	
	
	
	
}
