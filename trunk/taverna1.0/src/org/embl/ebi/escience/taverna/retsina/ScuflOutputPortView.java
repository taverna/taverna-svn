package org.embl.ebi.escience.taverna.retsina;

import com.jgraph.JGraph;
import com.jgraph.graph.CellMapper;
import com.jgraph.graph.CellViewRenderer;
import com.jgraph.graph.PortRenderer;
import com.jgraph.graph.PortView;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

// Network Imports
import java.net.URL;

import java.lang.Object;



public class ScuflOutputPortView extends PortView {

    public static ImageIcon outputPortIcon = null;
    
    static {
	URL outputPortIconURL = ScuflOutputPortView.class.getClassLoader().getResource("org/embl/ebi/escience/taverna/retsina/output.gif");
	outputPortIcon = new ImageIcon(outputPortIconURL);
    }
    
    protected static ScuflOutputPortRenderer renderer = new ScuflOutputPortRenderer();
    
    public ScuflOutputPortView(Object cell, JGraph graph, CellMapper cm) {
	super(cell, graph, cm);
    }

    /** 
     * Returns the bounds for the port view. 
     */
    public Rectangle getBounds() {
	if (outputPortIcon != null) {
	    Rectangle bounds = new Rectangle(getLocation(null));
	    int width = outputPortIcon.getIconWidth();
	    int height = outputPortIcon.getIconHeight();
	    bounds.x = bounds.x - width / 2;
	    bounds.y = bounds.y - height / 2;
	    bounds.width = width;
	    bounds.height = height;
	    return bounds;
	}
	return super.getBounds();
    }
    
    public CellViewRenderer getRenderer() {
	return renderer;
    }
    
    public static class ScuflOutputPortRenderer extends PortRenderer {
	
	public void paint(Graphics g) {
	    g.setColor(graph.getBackground());
	    outputPortIcon.paintIcon(graph, g, 0, 0);
	}
    
    }

}
