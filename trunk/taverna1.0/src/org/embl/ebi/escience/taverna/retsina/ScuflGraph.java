package org.embl.ebi.escience.taverna.retsina;

import com.jgraph.JGraph;
import com.jgraph.graph.*;

/**
 * Defines a Graph that uses the Shift-Button (Instead of the Right
 * Mouse Button, which is Default) to add/remove point to/from an edge.
 */
public class ScuflGraph extends JGraph {
    
    // Construct the Graph using the Model as its Data Source
    public ScuflGraph(GraphModel model) {
	super(model);
	// Use a Custom Marquee Handler
	setMarqueeHandler(new ScuflMarqueeHandler());
	// Tell the Graph to Select new Cells upon Insertion
	setSelectNewCells(true);
	// Make Ports Visible by Default
	setPortsVisible(true);
	// Use the Grid (but don't make it Visible)
	setGridEnabled(true);
	// Set the Grid Size to 10 Pixel
	setGridSize(6);
	// Set the Tolerance to 2 Pixel
	setTolerance(2);
    }
    
    // Override Superclass Method to Return Custom EdgeView
    protected EdgeView createEdgeView(Edge e, CellMapper cm) {
	// Return Custom EdgeView
	return new EdgeView(e, this, cm) {
		// Override Superclass Method
		public boolean isAddPointEvent(MouseEvent event) {
		    // Points are Added using Shift-Click
		    return event.isShiftDown();
		}
		// Override Superclass Method
		public boolean isRemovePointEvent(MouseEvent event) {
		    // Points are Removed using Shift-Click
		    return event.isShiftDown();
		}
	    };
    }
}
