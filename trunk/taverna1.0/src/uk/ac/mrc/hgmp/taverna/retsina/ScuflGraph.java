package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.JGraph;
import com.jgraph.graph.*;
import java.awt.event.*;
import java.util.*;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.border.*;
import java.awt.*;
import javax.swing.BorderFactory;

/**
 * Defines a Graph that uses the Shift-Button (Instead of the Right
 * Mouse Button, which is Default) to add/remove point to/from an edge.
 */
public class ScuflGraph extends JGraph 
              implements DropTargetListener
{
    
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
  	try {
  	    ScuflOutputPort port = (ScuflOutputPort)p;
  	    return new ScuflOutputPortView(p,this,cm);
  	}
  	catch (ClassCastException cce) {
  	    return new ScuflInputPortView(p,this,cm);
  	}
    }

    public void insertCell(Point point,String name)
    {
        // Construct Vertex with no Label
        ScuflGraphCell vertex = new ScuflGraphCell(name);

        // Create a Map that holds the attributes for the Vertex
        Map map = GraphConstants.createMap();

        // Add a Bounds Attribute to the Map
        Dimension size = new Dimension(100,80);
        GraphConstants.setBounds(map, new Rectangle(point, size));
        // Add a Border Color Attribute to the Map
        GraphConstants.setBorderColor(map, Color.black);
        // Add a White Background
        GraphConstants.setBackground(map, Color.gray);
        // Make Vertex Opaque
        GraphConstants.setOpaque(map, true);
        // Construct a Map from cells to Maps (for insert)
        Hashtable attributes = new Hashtable();

        // Associate the Vertex with its Attributes
        attributes.put(vertex, map);

        // Add a load of ports, mainly to test whether I've gotten
        // the rendering code working for the custom port views.
        attributes.putAll(vertex.addInputPort());
        attributes.putAll(vertex.addOutputPort());
        attributes.putAll(vertex.addOutputPort());
        attributes.putAll(vertex.addInputPort());
        attributes.putAll(vertex.addOutputPort());

        // Insert the Vertex and its Attributes (can also use model)
        getGraphLayoutCache().insert(new Object[]{vertex}, attributes, null, null, null);
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

