package org.embl.ebi.escience.taverna.retsina;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;

import com.jgraph.JGraph;
import com.jgraph.event.GraphSelectionEvent;
import com.jgraph.event.GraphSelectionListener;
import com.jgraph.graph.*;

public class ScuflGraphPanel extends JPanel implements GraphSelectionListener, KeyListener {
    
    // JGraph instance
    protected JGraph graph;
    
    // Undo Manager
    protected GraphUndoManager undoManager;
    
    // Actions which Change State
    protected Action undo, redo, remove, group,	ungroup, tofront, toback, cut, copy, paste;

    // Main Method
    public static void main(String[] args) {
	// Construct Frame
	JFrame frame = new JFrame("Retsina application mode test");
	// Set Close Operation to Exit
	//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// Add an Editor Panel
	frame.getContentPane().add(new Editor());
	// Fetch URL to Icon Resource
	URL jgraphUrl = Editor.class.getClassLoader().getResource("jgraph.gif");
	// If Valid URL
	if (jgraphUrl != null) {
	    // Load Icon
	    ImageIcon jgraphIcon = new ImageIcon(jgraphUrl);
	    // Use in Window
	    frame.setIconImage(jgraphIcon.getImage());
	}
	// Set Default Size
	frame.setSize(520, 390);
	// Show Frame
	frame.show();
    }
    
    /**
     * A single Scufl editor panel. The delegate
     * argument is required so that the panel
     * can invoke the createProcessor method of
     * some other class when it requires to create
     * a new processor node in the graph representation.
     */
    public ScuflGraphPanel(IScuflNodeCreator delegate) {
	
	this.creatorDelegate = delegate;

	setLayout(new BorderLayout());

	graph = new ScuflGraph(new ScuflModel()) {
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
	    };
	
	undoManager = new GraphUndoManager() {
		/**
		 * A custom edit handler that updates the toolbar buttons
		 */
		public void undoableEditHappened(UndoableEditEvent e) {
		    super.undoableEditHappened(e);
		    updateHistoryButtons();
		}
	    };
	
	// Register UndoManager with the Model
	graph.getModel().addUndoableEditListener(undoManager);
	
	// Update ToolBar based on Selection Changes
	graph.getSelectionModel().addGraphSelectionListener(this);
	
	// Listen for Delete Keystroke when the Graph has Focus
	graph.addKeyListener(this);
	
	// Add a ToolBar
	add(createToolBar(), BorderLayout.NORTH);
	
	// Add the Graph as Center Component
	add(new JScrollPane(graph), BorderLayout.CENTER);
	
    }

    /**
     * Insert a new Vertex at point. Should use the delegate
     * IScuflNodeCreator class to fetch the real information,
     * but doesn't at the moment.
     */
    public void insert(Point point) {
	
	// Construct Vertex with no Label
	ScuflGraphCell vertex = new ScuflGraphCell("Test");
	
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
	graph.getGraphLayoutCache().insert(new Object[]{vertex}, attributes, null, null, null);
    }

    /**
     * Insert a new Edge between source and target. The error
     * checking here is done entirely within the ScuflModel class,
     * you don't need to put it in here.
     */
    public void connect(Port source, Port target) {
	ConnectionSet cs = new ConnectionSet();
	DefaultEdge edge = new DefaultEdge();
	cs.connect(edge, source, target);
	Map map = GraphConstants.createMap();
	GraphConstants.setLineEnd(map, GraphConstants.ARROW_SIMPLE);
	Hashtable attributes = new Hashtable();
	attributes.put(edge, map);
	graph.getGraphLayoutCache().insert(new Object[]{edge}, attributes, cs, null, null);
    }
    
    /**
     * Group the supplied cells together
     */
    public void group(Object[] cells) {
	cells = graph.getGraphLayoutCache().order(cells);
	if (cells != null && cells.length > 0) {
	    int count = getCellCount(graph);
	    DefaultGraphCell group = new DefaultGraphCell(new Integer(count - 1));
	    ParentMap map = new ParentMap();
	    for (int i = 0; i < cells.length; i++) {
		map.addEntry(cells[i], group);
	    }
	    graph.getGraphLayoutCache().insert(new Object[]{group}, null, null, map, null);
	}
    }
    
    /**
     * Return the total number of cells in a graph
     */
    protected int getCellCount(JGraph graph) {
	Object[] cells = graph.getDescendants(graph.getRoots());
	return cells.length;
    }
    
    /**
     * Ungroup the cells supplied and select the children
     */
    public void ungroup(Object[] cells) {
	if (cells != null && cells.length > 0) {
	    ArrayList groups = new ArrayList();
	    ArrayList children = new ArrayList();
	    for (int i = 0; i < cells.length; i++) {
		if (isGroup(cells[i])) {
		    groups.add(cells[i]);
		    for (int j = 0; j < graph.getModel().getChildCount(cells[i]); j++) {
			Object child = graph.getModel().getChild(cells[i], j);
			if (!(child instanceof Port)) {
			    children.add(child);
			}
		    }
		}
	    }
	    // Remove Groups from Model (Without Children)
	    graph.getGraphLayoutCache().remove(groups.toArray());
	    // Select Children
	    graph.setSelectionCells(children.toArray());
	}
    }

    /**
     * Determines if a Cell is a Group
     */
    public boolean isGroup(Object cell) {
	// Map the Cell to its View
	CellView view = graph.getGraphLayoutCache().getMapping(cell, false);
	if (view != null) {
	    return !view.isLeaf();
	}
	return false;
    }

    /**
     *  Brings the Specified Cells to Front
     */
    public void toFront(Object[] c) {
	graph.getGraphLayoutCache().toFront(c);
    }
    
    /** 
     * Sends the Specified Cells to Back
     */
    public void toBack(Object[] c) {
	graph.getGraphLayoutCache().toBack(c);
    }
    
    /** 
     * Undo the last Change to the Model or the View
     */
    public void undo() {
	try {
	    undoManager.undo(graph.getGraphLayoutCache());
	} catch (Exception ex) {
	    System.err.println(ex);
	} finally {
	    updateHistoryButtons();
	}
    }

    /**
     * Redo the last Change to the Model or the View
     */
    public void redo() {
	try {
	    undoManager.redo(graph.getGraphLayoutCache());
	} catch (Exception ex) {
	    System.err.println(ex);
	} finally {
	    updateHistoryButtons();
	}
    }

    /**
     *  Update Undo/Redo Button State based on Undo Manager
     */
    protected void updateHistoryButtons() {
	// The View Argument Defines the Context
	undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
	redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
    }

    /**
     * Implement GraphSelectionListener Interface
     */
    public void valueChanged(GraphSelectionEvent e) {
	// Group Button only Enabled if more than One Cell Selected
	group.setEnabled(graph.getSelectionCount() > 1);
	// Update Button States based on Current Selection
	boolean enabled = !graph.isSelectionEmpty();
	remove.setEnabled(enabled);
	ungroup.setEnabled(enabled);
	tofront.setEnabled(enabled);
	toback.setEnabled(enabled);
	copy.setEnabled(enabled);
	cut.setEnabled(enabled);
    }
    
    /** 
     * Implements KeyListener
     */
    public void keyReleased(KeyEvent e) {
    }
    /** 
     * Implements KeyListener
     */
    public void keyTyped(KeyEvent e) {
    }
    /**
     * Trap delete events
     */
    public void keyPressed(KeyEvent e) {
	// Listen for Delete Key Press
	if (e.getKeyCode() == KeyEvent.VK_DELETE)
	    // Execute Remove Action on Delete Key Press
	    remove.actionPerformed(null);
    }
    
    /**
     * MarqueeHandler that Connects Vertices and Displays PopupMenus
     */
    public class MyMarqueeHandler extends BasicMarqueeHandler {
	
	// Holds the Start and the Current Point
	protected Point start, current;
	
	// Holds the First and the Current Port
	protected PortView port, firstPort;
	
	// Override to Gain Control (for PopupMenu and ConnectMode)
	public boolean isForceMarqueeEvent(MouseEvent e) {
	    // If Right Mouse Button we want to Display the PopupMenu
	    if (SwingUtilities.isRightMouseButton(e))
		// Return Immediately
		return true;
	    // Find and Remember Port
	    port = getSourcePortAt(e.getPoint());
	    // If Port Found and in ConnectMode (=Ports Visible)
	    if (port != null && graph.isPortsVisible())
		return true;
	    // Else Call Superclass
	    return super.isForceMarqueeEvent(e);
	}
	
	// Display PopupMenu or Remember Start Location and First Port
	public void mousePressed(final MouseEvent e) {
	    // If Right Mouse Button
	    if (SwingUtilities.isRightMouseButton(e)) {
		// Scale From Screen to Model
		Point loc = graph.fromScreen(e.getPoint());
		// Find Cell in Model Coordinates
		Object cell = graph.getFirstCellForLocation(loc.x, loc.y);
		// Create PopupMenu for the Cell
		JPopupMenu menu = createPopupMenu(e.getPoint(), cell);
		// Display PopupMenu
		menu.show(graph, e.getX(), e.getY());
		
		// Else if in ConnectMode and Remembered Port is Valid
	    } 
	    else if (port != null && !e.isConsumed() && graph.isPortsVisible()) {
		// Remember Start Location
		start = graph.toScreen(port.getLocation(null));
		// Remember First Port
		firstPort = port;
		// Consume Event
		e.consume();
	    } 
	    else {
		// Call Superclass
		super.mousePressed(e);
	    }
	}

	// Find Port under Mouse and Repaint Connector
	public void mouseDragged(MouseEvent e) {
	    // If remembered Start Point is Valid
	    if (start != null && !e.isConsumed()) {
		// Fetch Graphics from Graph
		Graphics g = graph.getGraphics();
		// Xor-Paint the old Connector (Hide old Connector)
		paintConnector(Color.black, graph.getBackground(), g);
		// Reset Remembered Port
		port = getTargetPortAt(e.getPoint());
		// If Port was found then Point to Port Location
		if (port != null) {
		    current = graph.toScreen(port.getLocation(null));
		}
		// Else If no Port was found then Point to Mouse Location
		else {
		    current = graph.snap(e.getPoint());
		}
		// Xor-Paint the new Connector
		paintConnector(graph.getBackground(), Color.black, g);
		// Consume Event
		e.consume();
	    }
	    // Call Superclass
	    super.mouseDragged(e);
	}

	public PortView getSourcePortAt(Point point) {
	    // Scale from Screen to Model
	    Point tmp = graph.fromScreen(new Point(point));
	    // Find a Port View in Model Coordinates and Remember
	    return graph.getPortViewAt(tmp.x, tmp.y);
	}
	
	// Find a Cell at point and Return its first Port as a PortView
	protected PortView getTargetPortAt(Point point) {
	    return getSourcePortAt(point);
	}
	
	// Connect the First Port and the Current Port in the Graph or Repaint
	public void mouseReleased(MouseEvent e) {
	    // If Valid Event, Current and First Port
	    if (e != null
		&& !e.isConsumed()
		&& port != null
		&& firstPort != null
		&& firstPort != port) {
		// Then Establish Connection
		connect((Port) firstPort.getCell(), (Port) port.getCell());
		// Consume Event
		e.consume();
		// Else Repaint the Graph
	    } else
		graph.repaint();
	    // Reset Global Vars
	    firstPort = port = null;
	    start = current = null;
	    // Call Superclass
	    super.mouseReleased(e);
	}
	
	// Show Special Cursor if Over Port
	public void mouseMoved(MouseEvent e) {
	    // Check Mode and Find Port
	    if (e != null
		&& getSourcePortAt(e.getPoint()) != null
		&& !e.isConsumed()
		&& graph.isPortsVisible()) {
		// Set Cusor on Graph (Automatically Reset)
		graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
		// Consume Event
		e.consume();
	    }
	    // Call Superclass
	    super.mouseReleased(e);
	}
	
	// Use Xor-Mode on Graphics to Paint Connector
	protected void paintConnector(Color fg, Color bg, Graphics g) {
	    // Set Foreground
	    g.setColor(fg);
	    // Set Xor-Mode Color
	    g.setXORMode(bg);
	    // Highlight the Current Port
	    //paintPort(graph.getGraphics());
	    // If Valid First Port, Start and Current Point
	    if (firstPort != null && start != null && current != null)
		// Then Draw A Line From Start to Current Point
		g.drawLine(start.x, start.y, current.x, current.y);
	}
	 
    }

    /**
     * Create the pop up menu
     */
    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
	JPopupMenu menu = new JPopupMenu();
	if (cell != null) {
	    // Edit
	    menu.add(new AbstractAction("Edit") {
		    public void actionPerformed(ActionEvent e) {
			graph.startEditingAtCell(cell);
		    }
		});
	}
	// Remove
	if (!graph.isSelectionEmpty()) {
	    menu.addSeparator();
	    menu.add(new AbstractAction("Remove") {
		    public void actionPerformed(ActionEvent e) {
			remove.actionPerformed(e);
		    }
		});
	}
	menu.addSeparator();
	// Insert
	menu.add(new AbstractAction("Insert") {
		public void actionPerformed(ActionEvent ev) {
		    insert(pt);
		}
	    });
	return menu;
    }
    
    /**
     * Create the toolbar
     */
    public JToolBar createToolBar() {
	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	
	// Insert
	URL insertUrl = getClass().getClassLoader().getResource("insert.gif");
	ImageIcon insertIcon = new ImageIcon(insertUrl);
	toolbar.add(new AbstractAction("", insertIcon) {
		public void actionPerformed(ActionEvent e) {
		    insert(new Point(10, 10));
		}
	    });
	
	// Toggle Connect Mode
	URL connectUrl = getClass().getClassLoader().getResource("connecton.gif");
	ImageIcon connectIcon = new ImageIcon(connectUrl);
	toolbar.add(new AbstractAction("", connectIcon) {
		public void actionPerformed(ActionEvent e) {
		    graph.setPortsVisible(!graph.isPortsVisible());
		    URL connectUrl;
		    if (graph.isPortsVisible()) {
			connectUrl = getClass().getClassLoader().getResource("connecton.gif");
		    }
		    else {
			connectUrl = getClass().getClassLoader().getResource("connectoff.gif");
		    }
		    ImageIcon connectIcon = new ImageIcon(connectUrl);
		    putValue(SMALL_ICON, connectIcon);
		}
	    });

	// Undo
	toolbar.addSeparator();
	URL undoUrl = getClass().getClassLoader().getResource("undo.gif");
	ImageIcon undoIcon = new ImageIcon(undoUrl);
	undo = new AbstractAction("", undoIcon) {
		public void actionPerformed(ActionEvent e) {
		    undo();
		}
	    };
	undo.setEnabled(false);
	toolbar.add(undo);

	// Redo
	URL redoUrl = getClass().getClassLoader().getResource("redo.gif");
	ImageIcon redoIcon = new ImageIcon(redoUrl);
	redo = new AbstractAction("", redoIcon) {
		public void actionPerformed(ActionEvent e) {
		    redo();
		}
	    };
	redo.setEnabled(false);
	toolbar.add(redo);
	
	//
	// Edit Block
	//
	toolbar.addSeparator();
	Action action;
	URL url;
	
	// Copy
	action = graph.getTransferHandler().getCopyAction();
	url = getClass().getClassLoader().getResource("copy.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(url));
	toolbar.add(copy = new EventRedirector(action));
	
	// Paste
	action = graph.getTransferHandler().getPasteAction();
	url = getClass().getClassLoader().getResource("paste.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(url));
	toolbar.add(paste = new EventRedirector(action));
	
	// Cut
	action = graph.getTransferHandler().getCutAction();
	url = getClass().getClassLoader().getResource("cut.gif");
	action.putValue(Action.SMALL_ICON, new ImageIcon(url));
	toolbar.add(cut = new EventRedirector(action));
	
	// Remove
	URL removeUrl = getClass().getClassLoader().getResource("delete.gif");
	ImageIcon removeIcon = new ImageIcon(removeUrl);
	remove = new AbstractAction("", removeIcon) {
		public void actionPerformed(ActionEvent e) {
		    if (!graph.isSelectionEmpty()) {
			Object[] cells = graph.getSelectionCells();
			cells = graph.getDescendants(cells);
			graph.getModel().remove(cells);
		    }
		}
	    };
	remove.setEnabled(false);
	toolbar.add(remove);

	// Zoom Std
	toolbar.addSeparator();
	URL zoomUrl = getClass().getClassLoader().getResource("zoom.gif");
	ImageIcon zoomIcon = new ImageIcon(zoomUrl);
	toolbar.add(new AbstractAction("", zoomIcon) {
		public void actionPerformed(ActionEvent e) {
		    graph.setScale(1.0);
		}
	    });
	// Zoom In
	URL zoomInUrl = getClass().getClassLoader().getResource("zoomin.gif");
	ImageIcon zoomInIcon = new ImageIcon(zoomInUrl);
	toolbar.add(new AbstractAction("", zoomInIcon) {
		public void actionPerformed(ActionEvent e) {
		    graph.setScale(2 * graph.getScale());
		}
	    });
	// Zoom Out
	URL zoomOutUrl = getClass().getClassLoader().getResource("zoomout.gif");
	ImageIcon zoomOutIcon = new ImageIcon(zoomOutUrl);
	toolbar.add(new AbstractAction("", zoomOutIcon) {
		public void actionPerformed(ActionEvent e) {
		    graph.setScale(graph.getScale() / 2);
		}
	    });
	
	// Group
	toolbar.addSeparator();
	URL groupUrl = getClass().getClassLoader().getResource("group.gif");
	ImageIcon groupIcon = new ImageIcon(groupUrl);
	group = new AbstractAction("", groupIcon) {
		public void actionPerformed(ActionEvent e) {
		    group(graph.getSelectionCells());
		}
	    };
	group.setEnabled(false);
	toolbar.add(group);
	
	// Ungroup
	URL ungroupUrl = getClass().getClassLoader().getResource("ungroup.gif");
	ImageIcon ungroupIcon = new ImageIcon(ungroupUrl);
	ungroup = new AbstractAction("", ungroupIcon) {
		public void actionPerformed(ActionEvent e) {
		    ungroup(graph.getSelectionCells());
		}
	    };
	ungroup.setEnabled(false);
	toolbar.add(ungroup);
	
	// To Front
	toolbar.addSeparator();
	URL toFrontUrl = getClass().getClassLoader().getResource("tofront.gif");
	ImageIcon toFrontIcon = new ImageIcon(toFrontUrl);
	tofront = new AbstractAction("", toFrontIcon) {
		public void actionPerformed(ActionEvent e) {
		    if (!graph.isSelectionEmpty())
			toFront(graph.getSelectionCells());
		}
	    };
	tofront.setEnabled(false);
	toolbar.add(tofront);
	
	// To Back
	URL toBackUrl = getClass().getClassLoader().getResource("toback.gif");
	ImageIcon toBackIcon = new ImageIcon(toBackUrl);
	toback = new AbstractAction("", toBackIcon) {
		public void actionPerformed(ActionEvent e) {
		    if (!graph.isSelectionEmpty())
			toBack(graph.getSelectionCells());
		}
	    };
	toback.setEnabled(false);
	toolbar.add(toback);
	
	return toolbar;
    }
    
    // This will change the source of the actionevent to graph.
    protected class EventRedirector extends AbstractAction {
	
	protected Action action;
	
	// Construct the "Wrapper" Action
	public EventRedirector(Action a) {
	    super("", (ImageIcon) a.getValue(Action.SMALL_ICON));
	    this.action = a;
	}
	
	// Redirect the Actionevent
	public void actionPerformed(ActionEvent e) {
	    e =	new ActionEvent(graph,
				e.getID(),
				e.getActionCommand(),
				e.getModifiers());
	    action.actionPerformed(e);
	}
    }
    
}
