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



public class ScuflInputPortView extends PortView {

    public static ImageIcon inputPortIcon = null;
    
    static {
	URL inputPortIconURL = ScuflInputPortView.class.getClassLoader().getResource("org/embl/ebi/escience/taverna/retsina/input.gif");
	inputPortIcon = new ImageIcon(inputPortIconURL);
    }
    
    protected static ScuflInputPortRenderer renderer = new ScuflInputPortRenderer();
    
    public ScuflInputPortView(Object cell, JGraph graph, CellMapper cm) {
	super(cell, graph, cm);
    }

    /** 
     * Returns the bounds for the port view. 
     */
    public Rectangle getBounds() {
	if (inputPortIcon != null) {
	    Rectangle bounds = new Rectangle(getLocation(null));
	    int width = inputPortIcon.getIconWidth();
	    int height = inputPortIcon.getIconHeight();
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
    
    public static class ScuflInputPortRenderer extends PortRenderer {
	
	public void paint(Graphics g) {
	    g.setColor(graph.getBackground());
	    inputPortIcon.paintIcon(graph, g, 0, 0);
	}
    
    }

}
