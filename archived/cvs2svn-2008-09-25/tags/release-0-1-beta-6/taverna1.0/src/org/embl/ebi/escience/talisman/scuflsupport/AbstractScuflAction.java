/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.talisman.*;




/**
 * The superclass of all actions that operate on ScuflModel
 * instances contained within ScuflModelBean objects in Talisman
 * fields. The process method sets up a member variable 'model'
 * which points to the actual ScuflModel without having to go through
 * all the layers of indirection every time. It throws appropriate
 * exceptions if the model can't be found. All actions that subclass
 * this expect to find a parameter 'model' in their input that points
 * to the beanfield containing the ScuflModelBean
 * @author Tom Oinn
 */
public abstract class AbstractScuflAction implements ActionWorker {
    
    ScuflModel model = null;

    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	// All subclasses require the model parameter
	action.requireParameters("model");
	// Get the trigger for error reporting
	Trigger trigger = (Trigger)action.getParent();
	// Locate the model and set it up in the model member
	IBeanField beanField = null;
	try {
	    beanField = (IBeanField)Resolver.getField(action.props.getProperty("model"),action);
	}
	catch (ClassCastException cce) {
	    trigger.addError("Supplied model field must implement IBeanField.");
	    throw new AbortActionException();
	}
	ScuflModelBean scuflBean = null;
	try {
	    scuflBean = (ScuflModelBean)beanField.getBean();
	}
	catch (ClassCastException cce) {
	    trigger.addError("The model BeanField must contain an instance of a ScuflModelBean.");
	    throw new AbortActionException();
	}
	if (scuflBean == null) {
	    trigger.addError("The model BeanField must contain a bean, it is null.");
	    throw new AbortActionException();
	}
	// Set the model for this execution of the action.
	this.model = scuflBean.getModel();
    }

}
