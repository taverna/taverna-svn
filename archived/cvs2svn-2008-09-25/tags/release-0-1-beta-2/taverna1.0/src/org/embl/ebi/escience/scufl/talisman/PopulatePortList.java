/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.talisman;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.talisman.*;

import org.embl.ebi.escience.scufl.talisman.AbstractScuflAction;
import java.lang.String;



/**
 * Populates a named SelectionList with names of ports within
 * a given processor in the model. The 'model' parameter
 * specified the ScuflModel to use, the 'processor' parameter
 * the name of a processor within that model, the 'type' parameter should be
 * either 'source', 'sink' or 'all' depending on the type of
 * ports you want included in the list, and the 'list' parameter
 * must be the name of a selection list in the Talisman page.
 * @author Tom Oinn
 */
public class PopulatePortList extends AbstractScuflAction implements ActionWorker {
    
    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	super.process(request, response, action);
	
	Trigger trigger = (Trigger)action.getParent();
	
	// Require the following parameters (model is redundant here,
	// retained for clarity. The superclass has already checked it)
	action.requireParameters("list,processor,type,model");

	// Get the selection list
	SelectionList theList = Resolver.getSelection(action.props.getProperty("list"),action);
	
	// Get the properties of the search
	String typeRequired = Resolver.getFieldValue(action.props.getProperty("type"),action).toLowerCase();
	String processorName = Resolver.getFieldValue(action.props.getProperty("processor"),action);
	
	// Locate the Processor
	Processor p = null;
	try {
	    p = model.locateProcessor(processorName);
	}
	catch (UnknownProcessorException upe) {
	    trigger.addError(upe.getMessage());
	    throw new AbortActionException();
	}

	// Clear the selection list
	theList.clearOptions();
	
	// Get the port array from the model
	Port[] ports = null;
	if (typeRequired.equals("source")) {
	    ports = p.getOutputPorts();
	}
	else { 
	    if (typeRequired.equals("sink")) {
		ports = p.getInputPorts();
	    }
	    else {
		if (typeRequired.equals("all")) {
		    ports = p.getPorts();
		}
	    }
	}
	for (int i = 0; i<ports.length; i++) {
	    theList.addOption(ports[i].getName());
	}

    }

}
