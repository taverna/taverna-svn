package org.embl.ebi.escience.taverna.retsina;

import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.GraphConstants;

import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ScuflGraphCell extends DefaultGraphCell {

    private static final int HEIGHT = 50;
    private static final int WIDTH = 50;
    private List inputPortList = new LinkedList();
    private List outputPortList = new LinkedList();
    private static final int xCoordInput = 1140;
    private int yCoordInput = 100;
    private int yCoordOutput = 100;
    private static final int xCoordOutput = -140;
    private static final int yIncrement = 160;

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
    
    public Map addInputPort()
    {
        Hashtable attributes = new Hashtable();
	Map map = GraphConstants.createMap();
	GraphConstants.setOffset(map, new Point(xCoordInput, yCoordInput));
	ScuflInputPort defaultPort = new ScuflInputPort("Input");
	inputPortList.add(defaultPort);
	attributes.put(defaultPort, map);
	add(defaultPort);
	yCoordInput += yIncrement;
	return attributes;
    }

    public Map addOutputPort()
    {
        Hashtable attributes = new Hashtable();
	Map map = GraphConstants.createMap();
	GraphConstants.setOffset(map, new Point(xCoordOutput, yCoordOutput));
	ScuflOutputPort defaultPort = new ScuflOutputPort("Target Port");
	outputPortList.add(defaultPort);
	attributes.put(defaultPort, map);
	add(defaultPort);
	yCoordOutput += yIncrement;
	return attributes;
    }
  
}
