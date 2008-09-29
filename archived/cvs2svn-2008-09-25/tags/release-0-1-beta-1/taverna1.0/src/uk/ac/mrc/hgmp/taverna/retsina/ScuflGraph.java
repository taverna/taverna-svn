package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.JGraph;
import com.jgraph.graph.*;
import com.jgraph.graph.Port; // ambiguous with: org.embl.ebi.escience.scufl.Port 
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.emboss.jemboss.gui.startup.ProgList;

// Utility Imports
import java.util.Hashtable;
import java.util.List; // ambiguous with: java.awt.List 
import java.util.Map;

import uk.ac.mrc.hgmp.taverna.retsina.ProgNode;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflGraphCell;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflInputPortView;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflOutputPort;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflOutputPortView;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflPort;
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
    // ScuflModel instance represented in this panel
    private org.embl.ebi.escience.scufl.ScuflModel scuflModel;
    private ProgList progs;
    private XScuflView scuflView;

    private static int fontSize = 12;
    private static Font font = new Font("Monospaced",
                      Font.PLAIN, fontSize);

    // Construct the Graph using the Model as its Data Source
    public ScuflGraph(GraphModel model, ProgList progs) 
    {
	super(model);
 
        this.progs = progs;
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

        newScuflModel();
        setDropTarget(new DropTarget(this,this));
    }
 
    /**
     *
     * Initialise the ScuflModel
     *
     */
    public void newScuflModel()
    {
      scuflModel = new ScuflModel();
      // Register a listener to print to stdout
      scuflModel.addListener(new ScuflModelEventPrinter(null));
      scuflView = new XScuflView(scuflModel);
    }

    /**
     *
     * Load in a XScufl workflow
     * @param String xscufl XScufl as text
     *
     */
    public void loadXScufl(String xscufl)
    {
      // use xscufl to populate the ScuflModel
      try
      {
        XScuflParser.populate(xscufl,scuflModel,null);
      }
      catch(UnknownProcessorException upe)
      {
        System.out.println("loadXScufl::UnknownProcessorException");
      }
      catch(UnknownPortException upte)
      {
        System.out.println("loadXScufl::UnknownPortException");
      }
      catch(ProcessorCreationException pce)
      {
        System.out.println("loadXScufl::ProcessorCreationException");
      }
      catch(DataConstraintCreationException dcce)
      {
        System.out.println("loadXScufl::DataConstraintCreationException");
      }
      catch(DuplicateProcessorNameException dpne)
      {
        System.out.println("loadXScufl::DuplicateProcessorNameException");
      }
      catch(MalformedNameException mne)
      {
        System.out.println("loadXScufl::MalformedNameException");
      }
      catch(ConcurrencyConstraintCreationException ccce)
      {
        System.out.println("loadXScufl::ConcurrencyConstraintCreationException");
      }
      catch(DuplicateConcurrencyConstraintNameException dccne)
      {
        System.out.println("loadXScufl::DuplicateConcurrencyConstraintNameException");
      }
      catch(XScuflFormatException xfe)
      {
        System.out.println("loadXScufl::XScuflFormatException");
      }

      Processor proc[] = scuflModel.getProcessors();

      // draw processors in
      int xpos = 0;
      int ypos = 0;
      for(int i=0;i<proc.length;i++)
      {
        Point p = new Point(xpos,ypos+=20);
        xpos += insertCell(p,proc[i],proc[i].getName());
      }

      // connect data links between processor ports
      DataConstraint dc[] = scuflModel.getDataConstraints();
      Object cells[] = getRoots();
      for(int i=0;i<dc.length;i++)
      {
        String constraint = dc[i].getName();
        System.out.println("**********************DataConstraint "+constraint);
        int ind1 = constraint.indexOf(":");
        int ind2 = constraint.indexOf("'");
        if(ind2 < 0)
          ind2 = constraint.indexOf("-");

        if(ind1 < 0 || ind2 < 0 || ind1 > ind2)
          continue;

//      System.out.println("********************** ind1 ind2"+ind1+" "+ind2);
        String procName = constraint.substring(0,ind1).trim();
        String portName = constraint.substring(ind1+1,ind2).trim();
        Port pstart = getJGraphPort(procName,portName,cells);

//      System.out.println("********************** procName "+procName);
        ind1 = constraint.indexOf("'",ind2+1);  
        if(ind1 < 0)
          ind1 = constraint.indexOf(">");
        ind2 = constraint.indexOf(":",ind1);
        procName = constraint.substring(ind1+1,ind2).trim();
        portName = constraint.substring(ind2+1).trim();
        Port pend = getJGraphPort(procName,portName,cells);
        if(pstart != null && pend != null)
          connect(pstart,pend);
      }

    }
   
    /**
     * 
     * @param String procName processor name
     * @param String portName port name
     * @param Object cells[] all roots (processors)
     * @return Port belonging to procName and called portName
     *
     */
    private Port getJGraphPort(String procName, String portName, Object cells[])
    {
      for(int j=0;j<cells.length;j++)
        if(((String)(((ScuflGraphCell)cells[j]).getUserObject())).equals(procName))
        {
          List children = ((ScuflGraphCell)cells[j]).getChildren();
          for(int k=0;k<children.size();k++)
            if( ((String)((ScuflPort)children.get(k)).getUserObject()).equals(portName))
            {
              System.out.println("**********************FOUND PORT "+portName);
              return (ScuflPort)children.get(k);
            }
        }  

      return null;
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


    /**
    *  Add a program to the editor 
    */
    public void insertCell(Point point,String group,String name)
    {
        setCursor(cbusy);
        Processor proc = addSoaplabProcessor(group,name);
        insertCell(point,proc,name);
        setCursor(cdone);
    }

    public int insertCell(Point point,Processor proc,String name)
    {
        // Construct Vertex 
        ScuflGraphCell vertex = new ScuflGraphCell(name,proc);

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
//      GraphConstants.setBounds(map, new Rectangle(point, size));

        // Set raised border
        GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
        GraphConstants.setBackground(map, Color.orange);
        GraphConstants.setOpaque(map, true);
        // Construct a Map from cells to Maps (for insert)
        Hashtable attributes = new Hashtable();

        // Associate the Vertex with its Attributes
//      attributes.put(vertex, map);

        // Add a load of ports, mainly to test whether I've gotten
        // the rendering code working for the custom port views.

        if(proc != null)
        {
          int maxPortWidth = 0;
          int wid = 0;
          org.embl.ebi.escience.scufl.Port inPorts[] = proc.getInputPorts();

          //find the max width of the input ports
          for(int j=0; j<inPorts.length;j++)
          {
            wid = getInputPortWidth(inPorts[j]);
            if(wid > maxPortWidth)
              maxPortWidth = wid;
          }
          width += maxPortWidth;

          // add the max width of the input ports to insertion point
          point.x += maxPortWidth;
          GraphConstants.setBounds(map, new Rectangle(point, size)); 
          // associate the Vertex with its Attributes
          attributes.put(vertex, map);

          // add input ports
          for(int j=0; j<inPorts.length;j++)
            attributes.putAll(vertex.addInputPort(inPorts[j]));

          // add output ports and find max width
          maxPortWidth = 0;
          org.embl.ebi.escience.scufl.Port outPorts[] = proc.getOutputPorts();
          for(int j=0; j<outPorts.length;j++)
          {
            attributes.putAll(vertex.addOutputPort(outPorts[j]));
            wid = getOutputPortWidth(outPorts[j]);
            if(wid > maxPortWidth)
              maxPortWidth = wid;
          }
          width += maxPortWidth;

        }

        // Insert the Vertex and its Attributes (can also use model)
        getGraphLayoutCache().insert(new Object[]{vertex}, attributes, null, null, null);
        return width;
    }

    /**
     * Insert a new Edge between source and target. The error
     * checking here is done entirely within the ScuflGraphModel class,
     * you don't need to put it in here.
     */
    public void connect(Port source, Port target)
    {
        ConnectionSet cs = new ConnectionSet();
        DefaultEdge edge = new DefaultEdge();
        cs.connect(edge, source, target);
        Map map = GraphConstants.createMap();
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_TECHNICAL);
        Hashtable attributes = new Hashtable();
        attributes.put(edge, map);
        getGraphLayoutCache().insert(new Object[]{edge},
                                  attributes, cs, null, null);
    }

    /**
     *
     *  Get the width of the port
     *
     */
    public int getInputPortWidth(org.embl.ebi.escience.scufl.Port p)
    {
      String name = p.getName();
      JLabel c = new JLabel();
      FontMetrics fm = c.getFontMetrics(font);
      int width = fm.stringWidth(name);
      return width+ScuflInputPortView.inputPortIcon.getIconWidth();
    }

    /**
     *
     *  Get the width of the port
     *
     */
    public int getOutputPortWidth(org.embl.ebi.escience.scufl.Port p)
    {
      String name = p.getName();
      JLabel c = new JLabel();
      FontMetrics fm = c.getFontMetrics(font);
      int width = fm.stringWidth(name);
      return width+ScuflOutputPortView.outputPortIcon.getIconWidth();
    }

    /**
     *
     *  Destroy a processor
     * 
     */
    public void destroyProcessor(Processor processor)
    {
      scuflModel.destroyProcessor(processor);
    }


    /**
     * Get the XML text
     */
    public String getXScufl()
    {
        return scuflView.getXMLText();
    }

    public void addDataConstraint(org.embl.ebi.escience.scufl.Port source_name, 
                                  org.embl.ebi.escience.scufl.Port sink_name)
    {
      
      try
      {
        DataConstraint dc = new DataConstraint(scuflModel,source_name,sink_name);
        scuflModel.addDataConstraint(dc);
      }
      catch(DataConstraintCreationException dcce)
      {
        System.out.println("DataConstraintCreationException:: in addDataConstraint");
      }

    }

    public SoaplabProcessor addSoaplabProcessor(String group, String name)
    {
        SoaplabProcessor processor = null;
        String procName = name;
        // Attempt to create a new SoaplabProcessor
        try
        {
          processor = new SoaplabProcessor(scuflModel,procName,
                       "http://industry.ebi.ac.uk/soap/soaplab/"+group+"::"+name);

          scuflModel.addProcessor(processor);

        } catch(ProcessorCreationException pce)
        {
          System.out.println("ProcessorCreationException addProcessor exception thrown");
        }
        catch(DuplicateProcessorNameException dpne)
        {
          System.out.println("DuplicateProcessorNameException addProcessor exception thrown");        }
        catch(Exception exp)
        {
          System.out.println("addProcessor exception thrown");
        }


        System.out.println("Finished test : SoaplabProcessorCreation");
        return processor;
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
        String name = dropS.getProgramName();
        String group = progs.getProgramGroup(name).toLowerCase().replace(':','_').replace(' ','_');
        insertCell(e.getLocation(),group,name);
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

