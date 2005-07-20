/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.*;

import java.lang.String;
import java.lang.StringBuffer;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;


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
    private boolean showBoring = true;

    public static final int ALL = 0;
    public static final int BOUND = 1;
    public static final int NONE = 2;
    public static final int BLOB = 3;

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
	// Get the fill colours
	if (System.getProperty("taverna.scufldiagram.fillcolours") != null) {
	    this.fillColours = System.getProperty("taverna.scufldiagram.fillcolours").split(",");
	}
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
     * Define whether to show boring things in the diagram
     */
    public void setBoring(boolean showBoring) {
	if (showBoring != this.showBoring) {
	    cacheValid = false;
	    this.showBoring = showBoring;
	}
    }
    
    /**
     * Are we showing boring things?
     */
    public boolean getShowBoring() {
	return this.showBoring;
    }

    /**
     * Define whether we are looking at all,
     * none or only bound input output ports
     * in the view, using the DotView.ALL|BOUND|NONE|BLOB
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
		
	dot.append(createWorkflow(null, model, "", this.portDisplay, 0));
	dot.append("}");
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

    /**
     * Set whether subworkflows should be expanded out
     */
    public void setExpandWorkflow(boolean e) {
	this.expandWorkflow = e;
    }
    
    /**
     * Are workflows expanded out inline?
     */
    public boolean getExpandWorkflow() {
	return this.expandWorkflow;
    }

    /**
     * Set the list of colours used for expanded workflow fills,
     * defaults to 'white','yellow','goldenrod1'. Colours should be
     * string names that GraphViz is able to deal with. See the
     * complete list at http://www.graphviz.org/cvs/doc/info/colors.html
     * <p>If the system property 'taverna.scufldiagram.fillcolours' is
     * set it is interpreted as a comma seperated list and overrides
     * this default.
     */
    public void setFillColours(String[] colours) {
	this.fillColours = colours;
    }

    /**
     * Get the list of colours used to fill the backgrounds of nested
     * workflows
     */
    public String[] getFillColours() {
	return this.fillColours;
    }
    
    boolean expandWorkflow = true;
    
    String[] fillColours = {"white","yellow","goldenrod1"};


    private String createWorkflow(String name, ScuflModel model, String prefix, int detail, int fill) {
	StringBuffer sb = new StringBuffer();
	if (name != null) {
	    sb.append("subgraph cluster_"+prefix+name+" {\n");
	    sb.append(" label="+q(name)+",\n");
	    sb.append(" fontname=\"Helvetica\", fontsize=\"10\", fontcolor=\"black\"\n\n");
	    sb.append(" fillcolor=\""+fillColours[fill]+"\", style=\"filled\"\n");
	}
	Processor[] processors = model.getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    if (processors[i].isBoring() == false || showBoring) {
		sb.append(createProcessor(processors[i], prefix, detail, fill));	    
	    }
	}
	// Create input and output ports in the workflow
	sb.append(createSinks(model, prefix, detail));
	sb.append(createSources(model, prefix, detail));
	sb.append(createEdges(model, prefix, detail));
	sb.append(createCoordination(model, prefix, detail));
	if (name != null) {
	    sb.append("}\n");
	}
	return sb.toString();
    }

    private String createCoordination(ScuflModel model, String prefix, int detail) {
	ConcurrencyConstraint[] cc = model.getConcurrencyConstraints();
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < cc.length; i++) {
	    ConcurrencyConstraint c = cc[i];
	    if (showBoring == true || (!c.getControllingProcessor().isBoring() && !c.getTargetProcessor().isBoring())) {
		String constraintName = prefix+"CONSTRAINT"+c.getName();
		String controllerName = prefix+c.getControllingProcessor().getName();
		if (c.getControllingProcessor() instanceof WorkflowProcessor && expandWorkflow == true) {
		    controllerName = "cluster_"+controllerName;
		}
		String targetName = prefix+c.getTargetProcessor().getName();
		if (c.getTargetProcessor() instanceof WorkflowProcessor && expandWorkflow == true) {
		    targetName = "cluster_"+targetName;
		}
		if (this.portDisplay != DotView.NONE && displayTypes) {
		    // Create the box
		    sb.append(" "+q(constraintName)+" [\n");
		    sb.append("  shape=\"rectangle\",\n");
		    sb.append("  fillcolor=\"white\",\n");
		    sb.append("  height=\"0\",\n");
		    sb.append("  width=\"0\",\n");
		    sb.append("  color=\"gray\",\n");
		    sb.append("  label=\"coordination\"\n");
		    sb.append(" ]\n");
		    // Create the edge from controller to box
		    sb.append(" "+q(controllerName)+"->"+q(constraintName)+" [\n");
		    sb.append("  arrowhead=\"none\",\n");
		    sb.append("  arrowtail=\"dot\",\n");
		    sb.append("  color=\"gray\",\n");
		    sb.append("  fontcolor=\"brown\",\n");
		    sb.append("  label=\""+ConcurrencyConstraint.statusCodeToString(c.getControllerStateGuard())+"\"\n");
		    sb.append(" ]\n");
		    // Create the edge from box to target
		    sb.append(q(constraintName)+"->"+q(targetName)+" [\n");
		    sb.append("  arrowhead=\"odot\",\n");
		    sb.append("  arrowtail=\"none\",\n");
		    sb.append("  color=\"gray\",\n");
		    sb.append("  fontcolor=\"darkgreen\",\n");
		    String stateChangeLabel =
			"from:"+
			ConcurrencyConstraint.statusCodeToString(c.getTargetStateFrom())+"\\nto:"+
			ConcurrencyConstraint.statusCodeToString(c.getTargetStateTo());
		    sb.append("  label=\""+stateChangeLabel+"\"\n");
		    sb.append(" ];\n");
		}
		else {
		    sb.append(" "+q(controllerName)+"->"+q(targetName)+" [\n");
		    sb.append("  color=\"gray\",\n");
		    sb.append("  arrowhead=\"odot\",\n");
		    sb.append("  arrowtail=\"none\"\n");
		    sb.append(" ];\n");
		}
	    }
	}
	return sb.toString();
    }


    private String createEdges(ScuflModel model, String prefix, int detail) {
	StringBuffer sb = new StringBuffer();
	DataConstraint[] links = model.getDataConstraints();
	for (int i = 0; i < links.length; i++) {
	    DataConstraint dc = links[i];
	    // Create the new edge
	    String sourcePortName = dc.getSource().getName();
	    String sourceProcessorName = prefix+dc.getSource().getProcessor().getName();
	    String sinkPortName = dc.getSink().getName();
	    String sinkProcessorName = prefix+dc.getSink().getProcessor().getName();
	    if (showBoring == true || 
		(dc.getSink().getProcessor().isBoring() == false && 
		 dc.getSource().getProcessor().isBoring() == false)) {
		String toName;
		if (dc.getSink().getProcessor() == model.getWorkflowSinkProcessor()) {
		    toName = q(prefix+"WORKFLOWINTERNALSINK_"+sinkPortName);
		}
		else if (dc.getSink().getProcessor() instanceof WorkflowProcessor &&
			 expandWorkflow == true) {
		    toName = q(sinkProcessorName+"WORKFLOWINTERNALSOURCE_"+sinkPortName);
		}
		else {
		    toName = q(sinkProcessorName)+":"+q(sinkPortName);
		}
		String fromName;
		if (dc.getSource().getProcessor() == model.getWorkflowSourceProcessor()) {
		    fromName = q(prefix+"WORKFLOWINTERNALSOURCE_"+sourcePortName);
		}
		else if (dc.getSource().getProcessor() instanceof WorkflowProcessor &&
			 expandWorkflow == true) {
		    fromName = q(sourceProcessorName+"WORKFLOWINTERNALSINK_"+sourcePortName);
		}
		else {
		    fromName = q(sourceProcessorName)+":"+q(sourcePortName);
		}
		sb.append(" "+fromName+"->"+toName+" [ \n");
		if (displayTypes) {
		    sb.append("  label = \""+dc.getSource().getSyntacticType()+"\\n"+dc.getSink().getSyntacticType()+"\"");
		}
		sb.append(" ];\n");
	    }
	}
	return sb.toString();
    }

    private String createSinks(ScuflModel model, String prefix, int detail) {
	Port[] sinks = model.getWorkflowSinkProcessor().getPorts();
	if (sinks.length == 0) {
	    return "";
	}
	else {
	    StringBuffer sb = new StringBuffer();
	    sb.append(" subgraph cluster_"+prefix+"sinks {\n");
	    sb.append("  style=\"dotted\"\n");
	    sb.append("  label=\"Workflow Outputs\"\n");
	    sb.append("  fontname=\"Helvetica\"\n");
	    sb.append("  fontsize=\"10\"\n");	
	    sb.append("  fontcolor=\"black\"  \n");
	    sb.append("  rank=\"same\"\n");
	    for (int i = 0; i < sinks.length; i++) {
		sb.append(q(prefix+"WORKFLOWINTERNALSINK_"+sinks[i].getName())+" [\n");
		if (detail == NONE) {
		    sb.append("   shape=\"box\",\n");
		}
		else if (detail == BLOB) {
		    sb.append("   shape=\"circle\",\n");
		}
		else {
		    sb.append("   shape=\"triangle\",\n");
		}	
		if (detail != BLOB) {
		    sb.append("   label="+q(sinks[i].getName())+",\n");
		    sb.append("   width=\"0\",\n");
		    sb.append("   height=\"0\",\n");
		}
		
		sb.append("   fillcolor=\"lightsteelblue2\"\n");
		sb.append(" ]\n");
	    }
	    sb.append("}\n");
	    return sb.toString();
	}
    }

    private String createSources(ScuflModel model, String prefix, int detail) {
	Port[] sources = model.getWorkflowSourceProcessor().getPorts();
	if (sources.length == 0) {
	    return "";
	}
	else {
	    StringBuffer sb = new StringBuffer();
	    sb.append(" subgraph cluster_"+prefix+"sources {\n");
	    sb.append("  style=\"dotted\"\n");
	    sb.append("  label=\"Workflow Inputs\"\n");
	    sb.append("  fontname=\"Helvetica\"\n");
	    sb.append("  fontsize=\"10\"\n");	
	    sb.append("  fontcolor=\"black\"  \n");
	    sb.append("  rank=\"same\"\n");
	    for (int i = 0; i < sources.length; i++) {
		sb.append(q(prefix+"WORKFLOWINTERNALSOURCE_"+sources[i].getName())+" [\n");
		if (detail == NONE) {
		    sb.append("   shape=\"box\",\n");
		}
		else if (detail == BLOB) {
		    sb.append("   shape=\"circle\",\n");
		}
		else {
		    sb.append("   shape=\"triangle\",\n");
		}	
		if (detail != BLOB) {
		    sb.append("   label="+q(sources[i].getName())+",\n");
		    sb.append("   width=\"0\",\n");
		    sb.append("   height=\"0\",\n");
		}
		
		sb.append("   fillcolor=\"lightsteelblue2\"\n");
		sb.append(" ]\n");
	    }
	    sb.append("}\n");
	    return sb.toString();
	}
    }

    private String createProcessor(Processor p, String prefix, int detail, int fill) {
	if (p instanceof WorkflowProcessor && expandWorkflow == true) {
	    fill++;
	    if (fill == fillColours.length) {
		fill = 0;
	    }
	    return createWorkflow(p.getName(), ((WorkflowProcessor)p).getInternalModel(), prefix+p.getName(), detail, fill);
	}
	String colour = ProcessorHelper.getPreferredColour(p);
	String name = q((prefix==null)?"":prefix+p.getName());
	StringBuffer sb = new StringBuffer();
	// Open processor definition
	sb.append(name + " [ \n");
	// Set colour
	sb.append("  fillcolor=\""+colour+"\",\n");
	
	if (detail == BLOB) {
	    sb.append("  shape=\"circle\", \n");
	    sb.append("  style=\"filled\"  \n");
	}
	else if (detail == NONE) {
	    sb.append("  shape=\"box\",           \n");
	    sb.append("  style=\"filled\",\n");
	    sb.append("  height=\"0\",            \n");
	    sb.append("  width=\"0\",             \n");
	    
	}
	else if (detail == ALL ||
		 detail == BOUND) {
	    sb.append("  shape=\"record\",        \n");
	    sb.append("  style=\"filled\",\n");
	}
	// Generate the label if this is not a blob
	if (detail != BLOB) {
	    if (detail == NONE) {
		sb.append("  label=\""+p.getName()+"\"\n");
	    }
	    else {
		InputPort[] inputs;
		OutputPort[] outputs;
		if (detail == BOUND) {
		    inputs = p.getBoundInputPorts();
		    outputs = p.getBoundOutputPorts();
		}
		else {
		    inputs = p.getInputPorts();
		    outputs = p.getOutputPorts();
		}
		sb.append("  label=\"");
		// Show display name for ALL ports case
		if (detail == ALL) {
		    sb.append("{"+getNameWithAlternate(p)+"}|");
		}
		sb.append("{");
		
		// Show inputs
		sb.append("{");
		for (int i = 0; i < inputs.length; i++) {
		    if (i > 0) {
			sb.append("|");
		    }
		    sb.append("<"+inputs[i].getName()+">"+inputs[i].getName());
		}
		sb.append("}|");
		// Show display name
		if (detail == BOUND) {
		    sb.append(getNameWithAlternate(p)+"|");
		}
		// Show outputs
		sb.append("{");
		for (int i = 0; i < outputs.length; i++) {
		    if (i > 0) {
			sb.append("|");
		    }
		    sb.append("<"+outputs[i].getName()+">"+outputs[i].getName());
		}
		sb.append("}}");
		sb.append("\"\n");
	    }
	}
	// Close definition
	sb.append(" ]; \n");
	return sb.toString();	    
    }
    
    private String getNameWithAlternate(Processor p) {
	if (p.getAlternatesList().isEmpty()) {
	    return p.getName();
	}
	else {
	    StringBuffer sb = new StringBuffer();
	    sb.append(p.getName()+"\\n"+p.getAlternatesList().size()+" alternate");
	    if (p.getAlternatesList().size()!=1) {
		sb.append("s");
	    }
	    return sb.toString();
	}
    }
    

}
