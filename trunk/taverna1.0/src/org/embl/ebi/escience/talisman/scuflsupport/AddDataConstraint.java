/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import org.embl.ebi.escience.talisman.*;




/**
 * Adds a new data constraint to the ScuflModel contained within
 * the field named by 'model' in the Talisman page. The required
 * parameters are 'sourceprocessor', 'sourceport', 'sinkprocessor'
 * and 'sinkport', all of which must contain names of the appropriate
 * entities within the ScuflModel.
 * @author Tom Oinn
 */
public class AddDataConstraint extends AbstractScuflAction implements ActionWorker {
    
    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	super.process(request, response, action);
	
	Trigger trigger = (Trigger)action.getParent();
	
	// Require the following parameters (model is redundant here,
	// retained for clarity. The superclass has already checked it)
	action.requireParameters("model,sourceport,sourceprocessor,sinkport,sinkprocessor");

	// Build the description string for the source and sink ports
	String sourcePortString = 
	    Resolver.getFieldValue(action.props.getProperty("sourceprocessor"),action) + ":" +
	    Resolver.getFieldValue(action.props.getProperty("sourceport"),action);
	String sinkPortString = 
	    Resolver.getFieldValue(action.props.getProperty("sinkprocessor"),action) + ":" +
	    Resolver.getFieldValue(action.props.getProperty("sinkport"),action);
		
	// Create a new data constraint and add it to the model.
	try {
	    model.addDataConstraint(new DataConstraint(model,sourcePortString,sinkPortString));
	}
	catch (DataConstraintCreationException dcce) {
	    trigger.addError(dcce.getMessage());
	    throw new AbortActionException();
	}
	catch (UnknownPortException upe) {
	    trigger.addError(upe.getMessage());
	    throw new AbortActionException();
	}
	catch (UnknownProcessorException upre) {
	    trigger.addError(upre.getMessage());
	    throw new AbortActionException();
	}
	catch (MalformedNameException mne) {
	    trigger.addError(mne.getMessage());
	    throw new AbortActionException();
	}
    }

}
