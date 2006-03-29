/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.embl.ebi.escience.scufl.*;
import java.util.*;

/**
 * Processor for the API consumer worker
 * @author Tom Oinn
 */
public class APIConsumerProcessor extends Processor {
    
    APIConsumerDefinition definition;

    public APIConsumerProcessor(ScuflModel model, String name, APIConsumerDefinition definition) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	this.definition = definition;
	setDescription(definition.description);
	
	try {

	    // If not static create an input for the subject to
	    // be operated on.
	    if (definition.isStatic == false) {
		if (definition.isConstructor == false) {
		    InputPort subjectInput = new InputPort(this, "object");
		    subjectInput.setSyntacticType(definition.getTavernaObjectTypeString());
		    addPort(subjectInput);
		}
		OutputPort subjectOutput = new OutputPort(this, "object");
		subjectOutput.setSyntacticType(definition.getTavernaObjectTypeString());
		addPort(subjectOutput);
	    }
	    
	    // Add a return value port for non void operations
	    if (definition.tName.equals("void") == false 
		&& definition.isConstructor == false) {
		// Use the 'object' port for constructors rather than the 'result' one
		OutputPort resultPort = new OutputPort(this, "result");
		resultPort.setSyntacticType(definition.getTavernaOutputTypeString());
		addPort(resultPort);
	    }
	    
	    // Add input ports for parameters
	    String[] tavernaOutputTypes = definition.getTavernaTypeStrings();
	    for (int i = 0; i < definition.pNames.length; i++) {
		// Create inputs...
		InputPort pPort = new InputPort(this, definition.pNames[i]);
		pPort.setSyntacticType(tavernaOutputTypes[i]);
		addPort(pPort);
	    }

	}
	catch (DuplicatePortNameException dpne) {
	    //
	}
	catch (PortCreationException pce) {
	    //
	}
	    
	
	
	
    }
    
    public Properties getProperties() {
	return new Properties();
    }
    
}
