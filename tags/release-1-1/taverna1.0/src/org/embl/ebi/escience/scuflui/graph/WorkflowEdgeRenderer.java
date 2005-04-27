package org.embl.ebi.escience.scuflui.graph;

import org.jgraph.graph.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * A subclass of the EdgeRenderer which always routes
 * edges with vertical normal vectors at the control
 * points.
 * @author Tom Oinn
 */
public class WorkflowEdgeRenderer extends EdgeRenderer {
    
    private float tension = (float)1.0;

    /**
     * Sets the tension - at 1.0 the center point of each inter-point
     * spline is horizontal, at 0.0 it will be a direct line between
     * the two points. At greater than 1.0 it will loop back, so you don't
     * want to do that!
     */
    public void setTension(float tension) {
	this.tension = tension;
    }

    protected Shape createLineEnd(int size, int style, Point2D src, Point2D dst) {
	return super.createLineEnd(size, style, new Point2D.Double(dst.getX(), src.getY()), dst);
    }

    protected Shape createShape() {
	int n = view.getPointCount();
	if (n > 1) {
	    EdgeView tmp = view;
	    Point2D[] p = new Point2D[n];
	    for (int i = 0; i < n; i++) {
		p[i] = tmp.getAttributes().createPoint(tmp.getPoint(i));
	    }
	    // End of Side-Effect Block
	    // Undo Possible MT-Side Effects
	    if (view != tmp) {
		view = tmp;
		installAttributes(view);
	    }
	    // End of Undo

	    // Create / reset the shared path
	    if (view.sharedPath == null) {
		view.sharedPath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	    } else {
		view.sharedPath.reset();
	    }

	    // Add end decorators
	    if (beginDeco != GraphConstants.ARROW_NONE) {
		view.beginShape = createLineEnd(beginSize, beginDeco, p[1], p[0]);
	    }
	    if (endDeco != GraphConstants.ARROW_NONE) {
		view.endShape = createLineEnd(endSize, endDeco, p[n-2], p[n-1]);
	    }
	    
	    // Now have an array of Point2D which we use to construct a set of
	    // bezier bicubic curves from, adding them to the path. First need
	    // to move to the start...
	    Point2D startPoint = p[0];
	    view.sharedPath.moveTo((float)startPoint.getX(), (float)startPoint.getY());

	    // Now loop over the remaining items in the point list and create
	    // the appropriate curve segments
	    for (int i = 1; i < p.length; i++) {
		float startX = (float)p[i-1].getX();
		float startY = (float)p[i-1].getY();
		float endX = (float)p[i].getX();
		float endY = (float)p[i].getY();
		float rowHeight = (endY - startY) * tension; 
		view.sharedPath.curveTo(startX, startY + rowHeight, endX, endY - rowHeight, endX, endY);
	    }
	    
	    // Add line end decorators
	    view.sharedPath.moveTo((float) p[n-1].getX(), (float) p[n-1].getY());
	    view.lineShape = (GeneralPath) view.sharedPath.clone();
	    if (view.endShape != null)
		view.sharedPath.append(view.endShape, true);
	    if (view.beginShape != null)
		view.sharedPath.append(view.beginShape, true);
	    return view.sharedPath;
	}
	// Can't route an edge with only one point, presumably doesn't happen though.
	return null;
    }

}
