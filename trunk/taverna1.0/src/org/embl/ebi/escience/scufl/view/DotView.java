/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;

import java.lang.String;
import java.lang.StringBuffer;



/**
 * Represents a ScuflModel instance as a dot file
 * which may then be rendered by standard graph
 * rendering tools.
 * @author Tom Oinn
 */
public class DotView implements ScuflModelEventListener {

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

	// For each processor, create a named node
	// Currently creates oval blobs per node,
	// as and when I can get the dot manual to 
	// load I will modify this to use record types
	// and to thereby show the port names.
	Processor[] processors = model.getProcessors();
	for (int i=0; i<processors.length; i++) {
	    Processor p = processors[i];
	    // Create the new node
	    dot.append(" "+p.getName()+" [ \n");
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
	    dot.append(" ];              \n");
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
