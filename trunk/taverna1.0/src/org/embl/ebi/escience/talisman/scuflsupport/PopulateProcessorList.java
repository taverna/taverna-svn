/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.talisman.*;

import org.embl.ebi.escience.talisman.scuflsupport.AbstractScuflAction;
/**
 * Populates a named SelectionList with names of processors
 * in the ScuflModel contained by the named bean field.
 * The selectionlist must be specified in the 'list' parameter,
 * the beanfield containing the ScuflModelBean in the 'model'
 * parameter.
 * @author Tom Oinn
 */
public class PopulateProcessorList extends AbstractScuflAction implements ActionWorker {
    
    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	super.process(request, response, action);
	
	Trigger trigger = (Trigger)action.getParent();
	
	// Require the following parameters (model is redundant here,
	// retained for clarity. The superclass has already checked it)
	action.requireParameters("list,model");

	// Get the selection list
	SelectionList theList = Resolver.getSelection(action.props.getProperty("list"),action);
	
	// Clear the selection list
	theList.clearOptions();
	
	// Get the processor array from the model
	Processor[] p = model.getProcessors();
	for (int i = 0; i<p.length; i++) {
	    theList.addOption(p[i].getName());
	}

    }

}
