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




public class ScuflOutputPortView extends PortView {

    public static ImageIcon outputPortIcon = null;
    private static int fontSize = 12;
    private static Font font = new Font("Monospaced",
                      Font.PLAIN, fontSize);
    
    static {
	URL outputPortIconURL = ScuflOutputPortView.class.getClassLoader().getResource("images/output.gif");
	outputPortIcon = new ImageIcon(outputPortIconURL);
    }
    
    protected ScuflOutputPortRenderer renderer = new ScuflOutputPortRenderer();
    
    public ScuflOutputPortView(Object cell, JGraph graph, CellMapper cm) {
	super(cell, graph, cm);
    }

    /** 
     * Returns the bounds for the port view. 
     */
    public Rectangle getBounds() 
    {
      int width = getWidth((String)((ScuflOutputPort)cell).getUserObject());
      Rectangle bounds = new Rectangle(super.getLocation(null));
      int height = getHeight();
      bounds.x = bounds.x;
      bounds.y = bounds.y - height / 2;
      bounds.width = width;
      bounds.height = height;
      return bounds;
    }
 
    /**
     * Override this so that the edge connects to
     * the right hand side of the port.
     */
    public Point getLocation(EdgeView edge)
    {
      Point p = super.getLocation(null);
      p.x += getWidth((String)((ScuflOutputPort)cell).getUserObject());
      p.y = p.y-outputPortIcon.getIconHeight()/2+1;
      return p;
    }

    public static int getWidth(String name)
    {
      JLabel c = new JLabel();
      FontMetrics fm = c.getFontMetrics(font);
      int width = fm.stringWidth(name);
      return width+outputPortIcon.getIconWidth();
    }

    public int getHeight()
    {
      JLabel c = new JLabel();
      FontMetrics fm = c.getFontMetrics(font);
      return fm.getHeight()+8;
    }
   
    public CellViewRenderer getRenderer() {
	return renderer;
    }
    
    public class ScuflOutputPortRenderer extends PortRenderer 
    {
	
      public void paint(Graphics g) 
      {
        String param = (String)((ScuflOutputPort)cell).getUserObject();
        FontMetrics fm = getFontMetrics(font);
        int width = fm.stringWidth(param);
        g.setColor(Color.red);
        g.setFont(font);
        g.drawString(param,0,12);
        g.setColor(this.graph.getBackground());
        outputPortIcon.paintIcon(this.graph, g, width, 0);
      }
    
    }

}
