/*
 * Copyright 2005 University of Manchester
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

package net.sf.taverna.ocula;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

/**
 * This class contains methods that parse elements that may appear in many
 * of the FrameBuilder classes. It helps maintain the code in a central place
 * and avoid duplication.
 * 
 * @author Ismael Juma <ismael@juma.me.uk>
 */
public class Parser {
    private Logger log = Logger.getLogger(Parser.class);
    private Ocula ocula;
    public Parser(Ocula ocula) {
	this.ocula = ocula;
    }
    
    /**
     * Looks for the first "script" tag, executes it, and returns the result
     * as an array.
     * @param element An Element containing a "script" tag.
     * @return An array of objects that contain the result of the script.
     * @throws EvalError
     */
    public Object[] parseScript(Element element) throws EvalError {
	Element scriptElement = element.getChild("script");
	final String script;
	if (scriptElement == null) {
	    // Will fail at eval time but that's fine, that's
	    // a checked exception and easier to deal with
	    script = "";
	}
	else {
	    script = scriptElement.getTextTrim();
	}

	ocula.evaluate(script);
	
	Object result = ocula.evaluate(script);
	// Convert Collection to array
	if (result instanceof Collection) {
	    result = ((Collection) result).toArray();
	}
	// Handle Object[]
	if (result instanceof Object[] == false) {
	    log.debug("Got " + result.toString() + " from call");
	    Object[] resultArray = new Object[1];
	    resultArray[0] = result;
	    result = resultArray;
	}
	return (Object[]) result;
    }
}
