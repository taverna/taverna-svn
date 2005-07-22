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

package net.sf.taverna.ocula.action;

/**
 * An action that processes the &lt;text&gt; elements of an &lt;input&gt;
 * element. It does this by creating a Map and passing this to the appropriate
 * callback. The keys of the Map are the names of the %lt;text&gt; element
 * and the values are the text contained in the element when this action was 
 * invoked
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 */
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.ocula.Ocula;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

public class ProcessFieldsAction extends AbstractInputAction	{

    private Logger log = Logger.getLogger(ProcessFieldsAction.class);
    
    private Map nameTextMap = new HashMap();
    
    public String getElementName() {
	return "processfields";
    }

    public void act(Ocula ocula, Element element) throws ActionException	{
	super.act(ocula, element);
	Element targetObjectElement = element.getChild("target");
	
	if (targetObjectElement == null) {
	    log.error("<target> does not exist.");
	    return;
	}
	String methodName = targetObjectElement.getAttributeValue("method");
	String objectKey = targetObjectElement.getAttributeValue("object");
	if (objectKey == null) {
	    throw new ActionException("<targetobject> must have a valid key of" +
	    		"an object in the context");
	}
	if (methodName == null) {
	    throw new ActionException("<targetobject> must have a valid method" +
	    		"attribute");
	}
	ocula.putContext("fields", nameTextMap);
	try {
	    ocula.runScript(objectKey + "." + methodName + "(fields)", new String[0]);
	}
	catch(EvalError ee) {
	    ActionException ae = new ActionException(
	    "Error when evaluating script");
	    ae.initCause(ee);
	    throw ae;
	}
    }
    
    protected void runScript(Ocula ocula, String name) throws EvalError{
	String text = (String) ocula.evaluate(name + ".getText(offset, length)");
	nameTextMap.put(name, text);
    }
}
