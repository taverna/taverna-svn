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
    private boolean displayTypes = true;
    private boolean lralign = false;

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
     * Define whether the graph should be top to bottom (false)
     * or left to right (true)
     */
    public void setAlignment(boolean alignment) {
	if (alignment != lralign) {
	    cacheValid = false;
	    this.lralign = alignment;
	}
    }
    
    /**
     * Get the alignment, true is equivalent to left to 
     * right, false being top to bottom.
     */
    public boolean getAlignment() {
	return this.lralign;
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
	if (policy == ALL) {
	    this.lralign = true;
	}
	else {
	    this.lralign = false;
	}
    }

    /**
     * Determine whether to show labels on edges for
     * their types
     */
    public void setTypeLabelDisplay(boolean display) {
	this.displayTypes = display;
    }

    /**
     * Are we displaying type labels?
     */
    public boolean getTypeLabelDisplay() {
	return this.displayTypes;
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
    	
    private String q(String name) {
	return "\""+name+"\"";
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
	if (System.getProperty("taverna.scufldiagram.ranksep") != null) {
	    dot.append("  ranksep=\""+System.getProperty("taverna.scufldiagram.ranksep")+"\"\n");
	}
	if (System.getProperty("taverna.scufldiagram.nodesep") != null) {
	    dot.append("  nodesep=\""+System.getProperty("taverna.scufldiagram.nodesep")+"\"\n");
	}
	// Only set left to right view if using port views
	if (this.lralign) {
	    dot.append("  rankdir=\"LR\"     \n");
	}
	dot.append(" ]                   \n"); 
	
	// Overall node style
	dot.append(" node [              \n");
	dot.append("  fontname=\"Helvetica\",         \n");
	dot.append("  fontsize=\"10\",              \n");
	dot.append("  fontcolor=\"black\",  \n");
	// Only set record shape if we're using port views
	if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
	    dot.append("  shape=\"record\",             \n");
	}
	else {
	    dot.append("  shape=\"box\",                \n");
	    dot.append("  height=\"0\",\n");
	    dot.append("  width=\"0\",\n");
	}
	dot.append("  color=\"black\",               \n");
	dot.append("  fillcolor=\"lightgoldenrodyellow\",\n");
	dot.append("  style=\"filled\"  \n");
	dot.append(" ];\n\n");
  
	// Overall edge style
	dot.append(" edge [                         \n");
	dot.append("  fontname=\"Helvetica\",         \n");
	dot.append("  fontsize=\"8\",              \n");
	dot.append("  fontcolor=\"black\",  \n");
	dot.append("  color=\"black\"                \n");
	dot.append(" ];\n\n");

	// Get the external ports, render these as special links
	// and edges to show them in the generated diagram
	// DEPRECATED
	/**
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
	*/
	
		
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
	    dot.append(" "+q(p.getName())+" [ \n");
	    // Change the colour if this is a WSDLBasedProcessor (hack hack hack)
	    dot.append("  fillcolor = \""+
		       org.embl.ebi.escience.scuflworkers.ProcessorHelper.getPreferredColour(p)+
		       "\",\n");
	    
	    // Create the label...
	    dot.append("  label = \"");
	    
	    // Are we generating port views?
	    if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
		// Name of the node
		if (this.portDisplay == DotView.ALL) {
		    if (p.getAlternatesList().isEmpty()) {
			dot.append("{"+p.getName()+"}|{");
		    }
		    else {
			dot.append("{"+p.getName()+"\\n"+p.getAlternatesList().size()+" alternate");
		    if (p.getAlternatesList().size()!=1) {
			dot.append("s");
		    }
		    dot.append("}|{");
		    }
		}
		else {
		    dot.append("{");
		}
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
		
		if (this.portDisplay == DotView.BOUND) {
		    if (p.getAlternatesList().isEmpty()) {
			dot.append(p.getName()+"|");
		    }
		    else {
			dot.append(p.getName()+"\\n"+p.getAlternatesList().size()+" alternate");
			if (p.getAlternatesList().size()!=1) {
			    dot.append("s");
			}
			dot.append("|");
		    }
		}

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
	    // If this is a normal internal link....
	    if (dc.getSource().getProcessor() != model.getWorkflowSourceProcessor() && 
		dc.getSink().getProcessor() != model.getWorkflowSinkProcessor()) {
		if (this.portDisplay == DotView.ALL || this.portDisplay == DotView.BOUND) {
		    dot.append(" "+q(sourceProcessorName)+":"+sourcePortName+"->"+q(sinkProcessorName)+":"+sinkPortName+" [ \n");
		}
		else {
		    dot.append(" "+q(sourceProcessorName)+"->"+q(sinkProcessorName)+" [ \n");
		}
	    }
	    else if (dc.getSource().getProcessor() == model.getWorkflowSourceProcessor()) {
		if (dc.getSink().getProcessor() == model.getWorkflowSinkProcessor()) {
		    // Is a direct source to sink link
		    dot.append(q("WORKFLOWINTERNALSOURCE_"+sourcePortName)+"->"+q("WORKFLOWINTERNALSINK_"+sinkPortName)+" [ \n");
		}
		else {
		    // Is a link from a workflow source to an internal sink
		    dot.append(q("WORKFLOWINTERNALSOURCE_"+sourcePortName)+"->"+q(sinkProcessorName)+":"+sinkPortName+" [ \n");
		}
	    }
	    else if (dc.getSink().getProcessor() == model.getWorkflowSinkProcessor()) {
		// Is a link from an internal source to a workflow sink
		dot.append(q(sourceProcessorName)+":"+sourcePortName+"->"+q("WORKFLOWINTERNALSINK_"+sinkPortName)+" [ \n");
	    }
	    if (displayTypes) {
		dot.append("  label = \""+dc.getSource().getSyntacticType()+"\\n"+dc.getSink().getSyntacticType()+"\"");
	    }
	    dot.append(" ];\n");
	}

	// For each port in the external source and sink processors create a new input or output
	// diamond.
	// Do workflow sources first.
	Port[] sources = model.getWorkflowSourceProcessor().getPorts();
	dot.append(" subgraph cluster_sources {\n");
	dot.append("  style=\"dotted\"\n");
	dot.append("  label=\"Workflow Inputs\"\n");	
	dot.append("  fontname=\"Helvetica\"         \n");
	dot.append("  fontsize=\"10\"              \n");
	dot.append("  fontcolor=\"black\"  \n");
	dot.append("  rank=\"same\"\n");    
	for (int i=0; i<sources.length; i++) {
	    dot.append(q("WORKFLOWINTERNALSOURCE_"+sources[i].getName())+" [\n");
	    if (this.portDisplay != DotView.NONE) {
		dot.append("   shape=\"invtriangle\",\n");
	    }
	    else {
		dot.append("   shape=\"box\",\n");
	    }
	    dot.append("   width=\"0\",\n");
	    dot.append("   height=\"0\",\n");
	    dot.append("   fillcolor=\"skyblue\",\n");
	    dot.append("   label=\""+sources[i].getName()+"\"\n");
	    dot.append("  ]\n");
	}
	dot.append(" }\n");
	dot.append(" subgraph cluster_sinks {\n");
	dot.append("  style=\"dotted\"\n");
	dot.append("  label=\"Workflow Outputs\"\n");
	dot.append("  fontname=\"Helvetica\"\n");
	dot.append("  fontsize=\"10\"\n");	
	dot.append("  fontcolor=\"black\"  \n");
	dot.append("  rank=\"same\"\n");    
	// ...then workflow sinks.
	Port[] sinks = model.getWorkflowSinkProcessor().getPorts();
	for (int i=0; i<sinks.length; i++) {
	    dot.append(q("WORKFLOWINTERNALSINK_"+sinks[i].getName())+" [\n");
	    if (this.portDisplay != DotView.NONE) {
		dot.append("   shape=\"triangle\",\n");
	    }
	    else {
		dot.append("   shape=\"box\",\n");
	    }
	    dot.append("   width=\"0\",\n");
	    dot.append("   height=\"0\",\n");
	    dot.append("   fillcolor=\"lightsteelblue2\",\n");
	    dot.append("   label=\""+sinks[i].getName()+"\"\n");
	    dot.append("  ]\n");
	}
	dot.append(" }\n");
	

	// For each concurrency constraint, create a box and dashed arrow from the
	// controller to the box and from the box to the target
	ConcurrencyConstraint[] cc = model.getConcurrencyConstraints();
	for (int i = 0; i < cc.length; i++) {
	    ConcurrencyConstraint c = cc[i];
	    if (this.portDisplay != DotView.NONE && displayTypes) {
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
		dot.append(" "+q(c.getControllingProcessor().getName())+"->"+q("constraint"+c.getName())+" [\n");
		dot.append("  arrowhead=\"none\",\n");
		dot.append("  arrowtail=\"dot\",\n");
		dot.append("  color=\"gray\",\n");
		dot.append("  fontcolor=\"brown\",\n");
		dot.append("  label=\""+ConcurrencyConstraint.statusCodeToString(c.getControllerStateGuard())+"\"\n");
		dot.append(" ]\n");
		// Create the edge from box to target
		dot.append(q("constraint"+c.getName())+"->"+q(c.getTargetProcessor().getName())+" [\n");
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
	    else {
		dot.append(" "+q(c.getControllingProcessor().getName())+"->"+q(c.getTargetProcessor().getName())+" [\n");
		dot.append("  color=\"gray\",\n");
		dot.append("  arrowhead=\"odot\",\n");
		dot.append("  arrowtail=\"none\"\n");
		dot.append(" ];\n");
	    }
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
