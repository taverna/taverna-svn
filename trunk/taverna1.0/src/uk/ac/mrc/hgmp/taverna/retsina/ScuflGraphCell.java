package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.GraphConstants;
import java.awt.Point;
import javax.swing.tree.MutableTreeNode;
import javax.swing.JLabel;

// Utility Imports
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List; // ambiguous with: java.awt.List 
import java.util.Map;

import uk.ac.mrc.hgmp.taverna.retsina.ScuflInputPort;
import uk.ac.mrc.hgmp.taverna.retsina.ScuflOutputPort;
import java.lang.Object;



public class ScuflGraphCell extends DefaultGraphCell {

    private static final int HEIGHT = 50;
    private static final int WIDTH = 50;
    private List inputPortList = new LinkedList();
    private List outputPortList = new LinkedList();
    private int yCoordInput = 100;
    private int yCoordOutput = 100;
    private static final int xCoordOutput = 1000;
    private static final int yIncrement = 40;

    public ScuflGraphCell() {
	super();
    }

    public ScuflGraphCell(Object o) {
        super(o);
    }

    public ScuflGraphCell(Object o, MutableTreeNode[] mutableTreeNodes) {
        super(o, mutableTreeNodes);
    }

    public ScuflGraphCell(Object o, boolean b) {
        super(o, b);
    }
    
    public Map addInputPort(String name)
    {
        int xCoordInput = 0;
        Hashtable attributes = new Hashtable();
	Map map = GraphConstants.createMap();
  	GraphConstants.setOffset(map, new Point(xCoordInput, yCoordInput));
	ScuflInputPort defaultPort = new ScuflInputPort(name);
	inputPortList.add(defaultPort);
	attributes.put(defaultPort, map);
	add(defaultPort);
	yCoordInput += yIncrement;
	return attributes;
    }

    public Map addOutputPort(String name)
    {
        Hashtable attributes = new Hashtable();
	Map map = GraphConstants.createMap();
	GraphConstants.setOffset(map, new Point(xCoordOutput, yCoordOutput));
	ScuflOutputPort defaultPort = new ScuflOutputPort(name);
	outputPortList.add(defaultPort);
	attributes.put(defaultPort, map);
	add(defaultPort);
	yCoordOutput += yIncrement;
        return attributes;
    }
  
}
