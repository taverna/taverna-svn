package uk.ac.mrc.hgmp.taverna.retsina;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.jgraph.JGraph;
import com.jgraph.graph.CellMapper;
import com.jgraph.graph.CellViewRenderer;
import com.jgraph.graph.Port;
import com.jgraph.graph.PortRenderer;
import com.jgraph.graph.PortView;

public class ScuflOutputPortView extends PortView {

    public static ImageIcon outputPortIcon = null;
    
    static {
	URL outputPortIconURL = ScuflOutputPortView.class.getClassLoader().getResource("images/output.gif");
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
