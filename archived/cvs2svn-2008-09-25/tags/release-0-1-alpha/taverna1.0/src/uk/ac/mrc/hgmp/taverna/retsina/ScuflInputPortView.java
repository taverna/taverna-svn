package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.JGraph;
import com.jgraph.graph.CellMapper;
import com.jgraph.graph.CellViewRenderer;
import com.jgraph.graph.EdgeView;
import com.jgraph.graph.PortRenderer;
import com.jgraph.graph.PortView;
import java.awt.*;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

// Network Imports
import java.net.URL;

import uk.ac.mrc.hgmp.taverna.retsina.ScuflInputPort;
import java.lang.Object;
import java.lang.String;



public class ScuflInputPortView extends PortView {

    public static ImageIcon inputPortIcon = null;
    private static int fontSize = 12;
    private static Font font = new Font("Monospaced",
                      Font.PLAIN, fontSize);

    static 
    {
	URL inputPortIconURL = ScuflInputPortView.class.getClassLoader().getResource("images/input.gif");
	inputPortIcon = new ImageIcon(inputPortIconURL);
    }
    
    protected ScuflInputPortRenderer renderer = new ScuflInputPortRenderer();
    
    public ScuflInputPortView(Object cell, JGraph graph, CellMapper cm) {
	super(cell, graph, cm);
    }

    /** 
     * Returns the bounds for the port view. 
     */
    public Rectangle getBounds() 
    {
      Rectangle bounds = new Rectangle(super.getLocation(null));
      int width = getWidth((String)((ScuflInputPort)cell).getUserObject());
      int height = getHeight();
      bounds.x = bounds.x - width;
      bounds.y = bounds.y - height/2;
      bounds.width = width*2;
      bounds.height = height;
      return bounds;
      
    }

    /**
     * Override this so that the edge connects to
     * the left hand side of the port.
     */
    public Point getLocation(EdgeView e)
    {
      Point p = super.getLocation(null);
      p.x -= getWidth((String)((ScuflInputPort)cell).getUserObject());
      return p;
    }

    public CellViewRenderer getRenderer() 
    {
        return renderer;
    }

    public static int getWidth(String name)
    {
      JLabel c = new JLabel(); 
      FontMetrics fm = c.getFontMetrics(font);
      int width = fm.stringWidth(name);
      return width+inputPortIcon.getIconWidth();
    }

    public int getHeight()
    {
      JLabel c = new JLabel();
      FontMetrics fm = c.getFontMetrics(font);
      return fm.getHeight()+8;
    }
    
    public class ScuflInputPortRenderer extends PortRenderer 
    {
      public void paint(Graphics g) 
      {
        String param = (String)((ScuflInputPort)cell).getUserObject();
        FontMetrics fm = getFontMetrics(font);
        int width = fm.stringWidth(param);
//      g.setXORMode(graph.getBackground());
//      boolean offset =
//              (GraphConstants.getOffset(view.getAllAttributes()) != null);

        g.setColor(Color.red);
        g.setFont(font);
        g.drawString(param,0,18);
        g.setColor(graph.getBackground());
        inputPortIcon.paintIcon(graph,g,width,5);
      }
    
    }

}

