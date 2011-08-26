package uk.org.mygrid.dataplaygroundui;

import java.awt.Shape;

import uk.org.mygrid.dataplayground.PlaygroundDataObject;
import uk.org.mygrid.dataplayground.PlaygroundDataThing;
import uk.org.mygrid.dataplayground.PlaygroundPortObject;
import uk.org.mygrid.dataplayground.PlaygroundProcessorObject;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.AbstractVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexSizeFunction;

public class PlaygroundVertexShapeSizeFunction  
    extends AbstractVertexShapeFunction 
    implements VertexSizeFunction{

	public PlaygroundVertexShapeSizeFunction() {
		super();
		setSizeFunction(this);
	}

	public int getSize(Vertex v) {

		if(v instanceof PlaygroundProcessorObject)
			   return 25;
		   
		   if(v instanceof PlaygroundPortObject)
			   return 11;
		   
		   if(v instanceof PlaygroundDataObject)
			   return 20;
		   
		   if(v instanceof PlaygroundDataThing)
			   return 18;
		   
		
		
		return 20;
	}

	public Shape getShape(Vertex v) {
		
		if(v instanceof PlaygroundProcessorObject)
			   return factory.getRoundRectangle(v);
		   
		   if(v instanceof PlaygroundPortObject)
			   return factory.getEllipse(v);
		   
		   if(v instanceof PlaygroundDataObject)
			   return factory.getEllipse(v);
		   
		   if(v instanceof PlaygroundDataThing)
			   return factory.getRegularPolygon(v,3);
		   
		
		
		
		return factory.getEllipse(v);
	}
	
	
	
}
