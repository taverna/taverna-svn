/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
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
	dot.append("digraph scufl_graph {\n");
	dot.append(" graph [             \n");
	dot.append("  style=\"\"         \n");
	dot.append(" ]                   \n"); 
	
	// For each processor, create a named node
	Processor[] processors = model.getProcessors();
	for (int i=0; i<processors.length; i++) {
	    Processor p = processors[i];
	    // Create the new node
	    dot.append(" "+p.getName()+" [ \n");
	    dot.append("  color = green  \n");
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
	    dot.append(" "+sourceProcessorName+"->"+sinkProcessorName+" [ \n");
	    dot.append("  label = \""+sourcePortName+" ->\\n"+sinkPortName+"\" \n");
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
