/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.talisman;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.DotView;
import org.embl.ebi.escience.talisman.*;


/**
 * Provides a Talisman action plugin that parses
 * an XScufl definition found in a named 'input' field and
 * writes the dot format representation of it into 
 * the 'output' field.
 * @author Tom Oinn
 */
public class XScufl2Dot implements ActionWorker {

    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {

	Trigger trigger = (Trigger)action.getParent();

	// Get the fields
	action.requireParameters("input,output");
	Field input = Resolver.getField(action.props.getProperty("input"),action);
	Field output = Resolver.getField(action.props.getProperty("output"),action);

	ScuflModel model = new ScuflModel();
	DotView view = new DotView(model);
	try {
	    XScuflParser.populate(input.getValue(),model,null);
	    output.setValue(view.getDot());
	}
	catch (UnknownProcessorException proc) {
	    trigger.addError(proc.getMessage());
	    throw new AbortActionException();
	}
	catch (UnknownPortException port) {
	    trigger.addError(port.getMessage());
	    throw new AbortActionException();
	}
	catch (ProcessorCreationException pce) {
	     trigger.addError(pce.getMessage());
	    throw new AbortActionException();
	}
	catch (DataConstraintCreationException dce) {
	    trigger.addError(dce.getMessage());
	    throw new AbortActionException();
	}
	catch (DuplicateProcessorNameException dpne) {
	    trigger.addError(dpne.getMessage());
	    throw new AbortActionException();
	}
	catch (MalformedNameException mne) { 
	    trigger.addError(mne.getMessage());
	    throw new AbortActionException();
	}
	catch (XScuflFormatException xsfe) { 
	    trigger.addError(xsfe.getMessage());
	    throw new AbortActionException();
	}
    }

}
