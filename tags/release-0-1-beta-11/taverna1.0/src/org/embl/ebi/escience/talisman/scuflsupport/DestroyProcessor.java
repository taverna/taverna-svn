/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.talisman.*;

import org.embl.ebi.escience.talisman.scuflsupport.AbstractScuflAction;
import java.lang.String;



/**
 * Remove a named processor from the ScuflModel held in a Talisman
 * bean. The 'model' parameter specifies the ScuflModel, the 'name'
 * the name of the processor to remove.
 * @author Tom Oinn
 */
public class DestroyProcessor extends AbstractScuflAction implements ActionWorker {
    
    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	super.process(request, response, action);
	
	Trigger trigger = (Trigger)action.getParent();
	
	// Require the following parameters (model is redundant here,
	// retained for clarity. The superclass has already checked it)
	action.requireParameters("model,name");

	String processorName = Resolver.getFieldValue(action.props.getProperty("name"),action);

	try {
	    Processor p = model.locateProcessor(processorName);
	    model.destroyProcessor(p);
	}
	catch (UnknownProcessorException upe) {
	    trigger.addError(upe.getMessage());
	    throw new AbortActionException();
	}

    }

}
