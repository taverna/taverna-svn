/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import org.embl.ebi.escience.scufl.parser.XScuflParser;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import java.lang.Exception;
import java.lang.String;



/**
 * A processor containing a full ScuflModel instance. Ports
 * on the processor are directly copied in terms of names
 * and types from the input and output ports of the underlying
 * ScuflModel object.
 * @author Tom Oinn
 */
public class WorkflowProcessor extends Processor implements java.io.Serializable {

    private ScuflModel theModel = null;
    private String definitionURL = "";

    /**
     * Construct a new processor with the given model to bind to, name
     * and URL of a workflow description to contain. 
     */
    public WorkflowProcessor(ScuflModel model, String name, String definitionURL)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	this.definitionURL = definitionURL;
	try {
	    // Create a new model instance
	    this.theModel = new ScuflModel();
	    // Populate from the definition URL
	    XScuflParser.populate(new URL(definitionURL).openStream(), theModel, null);
	    // Iterate over the workflow sinks to get the output ports
	    Port[] outputs = this.theModel.getWorkflowSinkPorts();
	    for (int i = 0; i < outputs.length; i++) {
		// Create a new output port
		Port newPort = new OutputPort(this, outputs[i].getName());
		newPort.setSyntacticType(outputs[i].getSyntacticType());
		//newPort.setSemanticType(outputs[i].getSemanticType());
		this.addPort(newPort);
	    }
	    // Iterate over workflow sources to get the input ports
	    Port[] inputs = this.theModel.getWorkflowSourcePorts();
	    for (int i = 0; i < inputs.length; i++) {
		// Create a new input port
		Port newPort = new InputPort(this, inputs[i].getName());
		newPort.setSyntacticType(inputs[i].getSyntacticType());
		//newPort.setSemanticType(inputs[i].getSemanticType());
		this.addPort(newPort);
	    }
	}
	catch (MalformedURLException mue) {
	    throw new ProcessorCreationException("The supplied definition URL was malformed, specified as '"
						 +definitionURL+"'");
	}
	catch (Exception e) {
	    throw new ProcessorCreationException("The workflow processor '"+name+
						 "' caused an exception :\n"+e.getMessage()+
						 "\n during creation. The exception had type :\n"+
						 e.getClass().toString());
	}
	
    }

    public ScuflModel getInternalModel() {
	return this.theModel;
    }

    public String getDefinitionURL() {
	return this.definitionURL;
    }
}
