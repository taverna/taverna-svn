/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.*;

import java.lang.String;
import java.lang.StringBuffer;



/**
 * Represents a ScuflModel instance as a dot file
 * which may then be rendered by standard graph
 * rendering tools.
 * @author Tom Oinn
 */
public class DotView implements ScuflModelEventListener, java.io.Serializable {

    private ScuflModel model = null;
    private boolean cacheValid = false;
    private String cachedRepresentation = null;
    private int portDisplay = DotView.NONE;

    public static final int ALL = 0;
    public static final int BOUND = 1;
    public static final int NONE = 2;

    /**
     * Construct the view and attach it to the 
     * given model.
     */
    public DotView(ScuflModel model) {
	this.model = model;
	// Set the cache to invalid, means
	// that the dot view will be generated
	// when it is first asked for.
	this.cacheValid = false;
	// Register ourselves as a listener
	this.model.addListener(this);
    }

    /**
     * Define whether we are looking at all,
     * none or only bound input output ports
     * in the view, using the DotView.ALL|BOUND|NONE
     * constants.
     */
    public void setPortDisplay(int policy) {
	this.cacheValid = false;
	this.portDisplay = policy;
    }

    /**
     * Return a dot representation of the underlying
     * model.
     */
    public String getDot() {
	if (!this.cacheValid) {
	    generateDot();
	}
	return this.cachedRepresentation;
    }
    
    /**
     * Generate the dot view
     */
    void generateDot() {
	StringBuffer dot = new StringBuffer();

	// Overall graph style
	dot.append("digraph scufl_graph {\n");
	dot.append(" graph [             \n");
	dot.append("  style=\"\"         \n");
	// Only set left to right view if using port views
	if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
	    dot.append("  rankdir=\"LR\"     \n");
	}
	dot.append(" ]                   \n"); 
	
	// Overall node style
	dot.append(" node [              \n");
	dot.append("  fontname=\"Courier\",         \n");
	dot.append("  fontsize=\"10\",              \n");
	dot.append("  fontcolor=\"black\",  \n");
	// Only set record shape if we're using port views
	if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
	    dot.append("  shape=\"record\",             \n");
	}
	dot.append("  color=\"black\",               \n");
	dot.append("  fillcolor=\"lightgoldenrodyellow\",\n");
	dot.append("  style=\"filled\"  \n");
	dot.append(" ];\n\n");
  
	// Overall edge style
	dot.append(" edge [                         \n");
	dot.append("  fontname=\"Courier\",         \n");
	dot.append("  fontsize=\"10\",              \n");
	dot.append("  fontcolor=\"black\",  \n");
	dot.append("  color=\"black\"                \n");
	dot.append(" ];\n\n");

	// Get the external ports, render these as special links
	// and edges to show them in the generated diagram
	Port[] externalPorts = model.getExternalPorts();
	for (int i=0; i<externalPorts.length; i++) {
	    // Create the new node in the graph for this external port
	    Port thePort = externalPorts[i];
	    Processor theProcessor = thePort.getProcessor();
	    String nodeName = "external"+theProcessor.getName()+thePort.getName();
	    dot.append(" "+nodeName+" [\n");
	    dot.append("  shape=\"diamond\",\n");
	    dot.append("  width=\"0\",\n");
	    dot.append("  height=\"0\",\n");
	    dot.append("  fillcolor=\"skyblue\",\n");
	    if (thePort instanceof InputPort) {
		dot.append("  label=\"in\"\n");
	    }
	    else {
		dot.append("  label=\"out\"\n");
	    }
	    dot.append(" ]\n");
	    // Create an edge to the named port, direction depends on the type
	    // of the port, whether input or output
	    if (thePort instanceof InputPort) {
		dot.append(" "+nodeName+"->"+theProcessor.getName()+":"+thePort.getName()+";\n");
	    }
	    else {
		dot.append(" "+theProcessor.getName()+":"+thePort.getName()+"->"+nodeName+";\n");
	    }
	}
	

	// For each processor, create a named node
	// Currently creates oval blobs per node,
	// as and when I can get the dot manual to 
	// load I will modify this to use record types
	// and to thereby show the port names.
	// - this is now done (tmo, 17th April 2003)
	Processor[] processors = model.getProcessors();
	for (int i=0; i<processors.length; i++) {
	    Processor p = processors[i];
	    // Create the new node
	    dot.append(" "+p.getName()+" [ \n");
	    // Change the colour if this is a WSDLBasedProcessor (hack hack hack)
	    if (p instanceof WSDLBasedProcessor) {
		dot.append("  fillcolor = \"lightsteelblue1\",\n");
	    }
	    // Or if a TalismanProcessor
	    if (p instanceof TalismanProcessor) {
		dot.append("  fillcolor = \"plum2\",\n");
	    }
	    
	    // Create the label...
	    dot.append("  label = \"");

	    // Are we generating port views?
	    if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
		// Name of the node
		dot.append("{"+p.getName().toUpperCase()+"}|{");
				
		// List of inputs
		Port[] inputs = null;
		if (this.portDisplay == DotView.ALL) {
		    inputs = p.getInputPorts();
		}
		else {
		    inputs = p.getBoundInputPorts();
		}
		
		dot.append("{");
		for (int j = 0; j<inputs.length; j++) {
		    dot.append("<"+inputs[j].getName()+">"+inputs[j].getName());
		    if (j < (inputs.length-1)) {
			dot.append("|");
		    }
		}
		dot.append("}|");
		
		// List of outputs
		Port[] outputs = null;
		if (this.portDisplay == DotView.ALL) {
		    outputs = p.getOutputPorts();
		}
		else {
		    outputs = p.getBoundOutputPorts();
		}
		dot.append("{");
		for (int j = 0; j<outputs.length; j++) {
		    dot.append("<"+outputs[j].getName()+">"+outputs[j].getName());
		    if (j < (outputs.length-1)) {
			dot.append("|");
		    }
		}
		dot.append("}");
		
		dot.append("}");
	    }
	    else {
		// Not generating the port view, just append the name of the
		// node.
		dot.append(p.getName());
	    }
	    // Close the label
	    dot.append("\"\n");
	    dot.append(" ];\n");
	}

	// For each data constraint, create an edge
	DataConstraint[] links = model.getDataConstraints();
	for (int i=0; i<links.length; i++) {
	    DataConstraint dc = links[i];
	    // Create the new edge
	    String sourcePortName = dc.getSource().getName();
	    String sourceProcessorName = dc.getSource().getProcessor().getName();
	    String sinkPortName = dc.getSink().getName();
	    String sinkProcessorName = dc.getSink().getProcessor().getName();
	    if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
		dot.append(" "+sourceProcessorName+":"+sourcePortName+"->"+sinkProcessorName+":"+sinkPortName+" [ \n");
	    }
	    else {
		dot.append(" "+sourceProcessorName+"->"+sinkProcessorName+" [ \n");
	    }
	    dot.append("  label = \""+dc.getSource().getSyntacticType()+"\"");
	    dot.append(" ];\n");
	}

	// For each concurrency constraint, create a box and dashed arrow from the
	// controller to the box and from the box to the target
	ConcurrencyConstraint[] cc = model.getConcurrencyConstraints();
	for (int i = 0; i < cc.length; i++) {
	    ConcurrencyConstraint c = cc[i];
	    // Create the box
	    dot.append(" constraint"+c.getName()+" [\n");
	    dot.append("  shape=\"rectangle\",\n");
	    dot.append("  fillcolor=\"white\",\n");
	    dot.append("  height=\"0\",\n");
	    dot.append("  width=\"0\",\n");
	    dot.append("  color=\"gray\",\n");
	    dot.append("  label=\"coordination\"\n");
	    dot.append(" ]\n");
	    // Create the edge from controller to box
	    dot.append(" "+c.getControllingProcessor().getName()+"->constraint"+c.getName()+" [\n");
	    dot.append("  arrowhead=\"none\",\n");
	    dot.append("  arrowtail=\"dot\",\n");
	    dot.append("  color=\"gray\",\n");
	    dot.append("  fontcolor=\"brown\",\n");
	    dot.append("  label=\""+ConcurrencyConstraint.statusCodeToString(c.getControllerStateGuard())+"\"\n");
	    dot.append(" ]\n");
	    // Create the edge from box to target
	    dot.append(" constraint"+c.getName()+"->"+c.getTargetProcessor().getName()+" [\n");
	    dot.append("  arrowhead=\"odot\",\n");
	    dot.append("  arrowtail=\"none\",\n");
	    dot.append("  color=\"gray\",\n");
	    dot.append("  fontcolor=\"darkgreen\",\n");
	    String stateChangeLabel =
		"from:"+
		ConcurrencyConstraint.statusCodeToString(c.getTargetStateFrom())+"\\nto:"+
		ConcurrencyConstraint.statusCodeToString(c.getTargetStateTo());
	    dot.append("  label=\""+stateChangeLabel+"\"\n");
	    dot.append(" ];\n");

	}

	dot.append("}\n");
	this.cachedRepresentation = dot.toString();
    }

    /**
     * Implements ScuflModelEventListener, in this
     * case is used to tell when our cached version
     * of the dot representation may be out of date.
     */
    public void receiveModelEvent(ScuflModelEvent event) {
	this.cacheValid = false;
    }
    
}
