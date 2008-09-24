/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.ocula.action;

import org.jdom.Element;
import net.sf.taverna.ocula.Ocula;
import bsh.EvalError;

/**
 * Run an arbitrary BeanShell script, pulling the specified bound variables
 * out of the interpreter context and into the Ocula instance.
 * @author Tom Oinn
 */
public class RunScriptAction implements ActionSPI {

    public String getElementName() {
	return "run";
    }
    public void act(Ocula ocula, Element actionElement) throws ActionException {
	String[] extract;
	String extractValue = actionElement.getAttributeValue("extract");
	if (extractValue == null) {
	    extract = new String[0];
	}
	else {
	    extract = extractValue.split(",");
	}
	String scriptString = actionElement.getTextTrim();
	try {
	    ocula.runScript(scriptString, extract);
	}
	catch (EvalError ee) {
	    ActionException ae = new ActionException("Error when evaluating script");
	    ae.initCause(ee);
	    throw ae;
	}
    }
}
