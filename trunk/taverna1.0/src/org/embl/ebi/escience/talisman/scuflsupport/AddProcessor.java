/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import org.embl.ebi.escience.talisman.*;

import org.embl.ebi.escience.talisman.scuflsupport.AbstractScuflAction;
import java.lang.String;



/**
 * Adds a new Processor to a ScuflModel within a Talisman BeanField.
 * Requires 'model' to point to the ScuflModel, 'type' to be the
 * category of processor to create (only soaplabwsdl is supported at the
 * moment), 'spec' to be the category specific extra information such
 * as a SOAP endpoint, and 'name' is the name of the new processor
 * within the ScuflModel.
 * @author Tom Oinn
 */
public class AddProcessor extends AbstractScuflAction implements ActionWorker {
    
    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	super.process(request, response, action);
	
	Trigger trigger = (Trigger)action.getParent();
	
	// Require the following parameters (model is redundant here,
	// retained for clarity. The superclass has already checked it)
	action.requireParameters("model,type,spec,name");

	String processorType = Resolver.getFieldValue(action.props.getProperty("type"),action).toLowerCase();
	String processorSpec = Resolver.getFieldValue(action.props.getProperty("spec"),action);
	String processorName = Resolver.getFieldValue(action.props.getProperty("name"),action);

	// Complain if the type is not 'soaplabwsdl'
	if (processorType.equals("soaplabwsdl")==false) {
	    trigger.addError("Only the 'soaplabwsdl' processor type is currently supported, sorry.");
	    throw new AbortActionException();
	}
	
	// Create a new processor and add it to the model.
	try {
	    model.addProcessor(new SoaplabProcessor(model,processorName,processorSpec));
	}
	catch (ProcessorCreationException pce) {
	    trigger.addError(pce.getMessage());
	    throw new AbortActionException();
	}
	catch (DuplicateProcessorNameException dpne) {
	    trigger.addError(dpne.getMessage());
	    throw new AbortActionException();
	}
    }

}
