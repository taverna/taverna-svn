/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.talisman.scuflsupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.embl.ebi.escience.scufl.parser.Scufl2XScuflParser;
import org.embl.ebi.escience.talisman.*;


/**
 * Provides a Talisman action plugin that parses
 * a Scufl definition found in a named 'input' field and
 * writes the XScufl version of it out to a second 'output'
 * field.
 * @author Tom Oinn
 */
public class Scufl2XScufl implements ActionWorker {

    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {

	// Get the fields
	action.requireParameters("input,output");
	Field input = Resolver.getField(action.props.getProperty("input"),action);
	Field output = Resolver.getField(action.props.getProperty("output"),action);
	output.setValue(Scufl2XScuflParser.parse(input.getValue()));
    
    }

}
