package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.DefaultGraphCell;
import com.jgraph.graph.GraphConstants;
import java.awt.Point;
import javax.swing.tree.MutableTreeNode;
import org.embl.ebi.escience.scufl.Processor;

// Utility Imports
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List; // ambiguous with: java.awt.List 
import java.util.Map;




public class ScuflGraphCell extends DefaultGraphCell {

    private List inputPortList = new LinkedList();
    private List outputPortList = new LinkedList();
    private int yCoordInput = 30;
    private int yCoordOutput = 30;
    private static final int xCoordOutput = 1000;
    private static final int yIncrement = 32;
    private Processor processor = null;

    public ScuflGraphCell() {
	super();
    }

    public ScuflGraphCell(Object o) {
        super(o);
    }

    public ScuflGraphCell(Object o, Processor processor) {
        super(o);
        this.processor = processor;
    }

    public ScuflGraphCell(Object o, MutableTreeNode[] mutableTreeNodes) {
        super(o, mutableTreeNodes);
    }

    public ScuflGraphCell(Object o, boolean b) {
        super(o, b);
    }
 
    public Processor getScuflProcessor()
    {
      return processor;
    }
   
    public Map addInputPort(org.embl.ebi.escience.scufl.Port p)
    {
        int xCoordInput = 0;
        Hashtable attributes = new Hashtable();
	Map map = GraphConstants.createMap();
  	GraphConstants.setOffset(map, new Point(xCoordInput, yCoordInput));
	ScuflInputPort defaultPort = new ScuflInputPort(p.getName(),p);
	inputPortList.add(defaultPort);
	attributes.put(defaultPort, map);
	add(defaultPort);
	yCoordInput += yIncrement;
	return attributes;
    }

    public Map addOutputPort(org.embl.ebi.escience.scufl.Port p)
    {
        Hashtable attributes = new Hashtable();
	Map map = GraphConstants.createMap();
	GraphConstants.setOffset(map, new Point(xCoordOutput, yCoordOutput));
	ScuflOutputPort defaultPort = new ScuflOutputPort(p.getName(),p);
	outputPortList.add(defaultPort);
	attributes.put(defaultPort, map);
	add(defaultPort);
	yCoordOutput += yIncrement;
        return attributes;
    }
  
}
