package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.JGraph;
import com.jgraph.graph.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

// Utility Imports
import java.util.Hashtable;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import uk.ac.mrc.hgmp.taverna.retsina.ProgNode;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphCell;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflInputPortView;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflOutputPort;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflOutputPortView;
import java.lang.ClassCastException;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.lang.System;



/**
 * Defines a Graph that uses the Shift-Button (Instead of the Right
 * Mouse Button, which is Default) to add/remove point to/from an edge.
 */
public class ScuflGraph extends JGraph 
              implements DropTargetListener
{
 
    final Cursor cbusy = new Cursor(Cursor.WAIT_CURSOR);
    final Cursor cdone = new Cursor(Cursor.DEFAULT_CURSOR);
   
    // Construct the Graph using the Model as its Data Source
    public ScuflGraph(GraphModel model) {
	super(model);
	// Use a Custom Marquee Handler
	//setMarqueeHandler(new ScuflGraphPanel.ScuflMarqueeHandler());
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

        setDropTarget(new DropTarget(this,this));
    }
    
    /**
     * Override Superclass Method to Return Custom EdgeView
     */
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

    /**
     * A custom portview to provide the orange and green
     * arrow glyphs on input and output ports.
     */
    protected PortView createPortView(Port p, CellMapper cm) {
        if( p instanceof ScuflOutputPort)
          return new ScuflOutputPortView(p,this,cm);
        else
          return new ScuflInputPortView(p,this,cm);
    }


    public void insertCell(Point point,String name)
    {
      insertCell(point,name,null);
    }

    /**
    *  Add a program to the editor 
    */
    public void insertCell(Point point,String name, Processor proc)
    {
        setCursor(cbusy);
        // Add user input parameters
    
        // Construct Vertex with no Label
        ScuflGraphCell vertex = new ScuflGraphCell(name);

        // Create a Map that holds the attributes for the Vertex
        Map map = GraphConstants.createMap();

        // Add a Bounds Attribute to the Map
        int nports = 0;
        if(proc != null)
          nports = proc.getInputPorts().length;
        JLabel lab = new JLabel(name);
        Dimension size = lab.getPreferredSize();
        int width  = (int)size.getWidth()+10;
        int height = (int)size.getHeight() + (18*nports);
        size = new Dimension(width,height);
        GraphConstants.setBounds(map, new Rectangle(point, size));

        // Set raised border
        GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
        GraphConstants.setBackground(map, Color.orange);
        GraphConstants.setOpaque(map, true);
        // Construct a Map from cells to Maps (for insert)
        Hashtable attributes = new Hashtable();

        // Associate the Vertex with its Attributes
        attributes.put(vertex, map);

        // Add a load of ports, mainly to test whether I've gotten
        // the rendering code working for the custom port views.

        if(proc != null)
        {
          org.embl.ebi.escience.scufl.Port inPorts[] = proc.getInputPorts();
          for(int j=0; j<inPorts.length;j++)
            attributes.putAll(vertex.addInputPort(inPorts[j].getName()));

          org.embl.ebi.escience.scufl.Port outPorts[] = proc.getOutputPorts();
          for(int j=0; j<outPorts.length;j++)
            attributes.putAll(vertex.addOutputPort(outPorts[j].getName()));
        }

        // Insert the Vertex and its Attributes (can also use model)
        getGraphLayoutCache().insert(new Object[]{vertex}, attributes, null, null, null);
        setCursor(cdone);
    }


// Drag 'n Drop
  protected static Border dropBorder = new BevelBorder(BevelBorder.LOWERED);
  protected static Border endBorder =
                               BorderFactory.createLineBorder(Color.black);
  public void dragEnter(DropTargetDragEvent e)
  {
    if(e.isDataFlavorSupported(DataFlavor.stringFlavor) ||
       e.isDataFlavorSupported(ProgNode.PROGNODE))
    {
      e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
      this.setBorder(dropBorder);
    }
  }

  public void dragExit(DropTargetEvent e)
  {
    this.setBorder(endBorder);
  }

  public void drop(DropTargetDropEvent e)
  {
    this.setBorder(endBorder);
    Transferable t = e.getTransferable();
    if(t.isDataFlavorSupported(DataFlavor.stringFlavor))
    {
      e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      try
      {
        ProgNode dropS = (ProgNode) t.getTransferData(DataFlavor.stringFlavor);
        System.out.println("DROP DataFlavor.stringFlavor "+dropS.getProgramName());
        insertCell(e.getLocation(),dropS.getProgramName());
        e.dropComplete(true);
      }
      catch (Exception ex) {}
    }
    else if(t.isDataFlavorSupported(ProgNode.PROGNODE))
    {
      e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      try
      {
        ProgNode dropS = (ProgNode)
             t.getTransferData(ProgNode.PROGNODE);
        System.out.println("DROP ProgNode.PROGNODE "+dropS.getProgramName());
        e.dropComplete(true);
      }
      catch (Exception ex) {}
    }
    else
    {
      e.rejectDrop();
      return;
    }
    return;
  }

  public void dragOver(DropTargetDragEvent e) {}
  public void dropActionChanged(DropTargetDragEvent e) {}

}

