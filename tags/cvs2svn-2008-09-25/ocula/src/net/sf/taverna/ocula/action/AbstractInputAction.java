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

import java.util.Iterator;
import java.util.List;

import net.sf.taverna.ocula.Ocula;

import org.apache.log4j.Logger;
import org.jdom.Element;

import bsh.EvalError;

/**
 * An abstract class for dealing with input actions.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public abstract class AbstractInputAction implements ActionSPI {
    private static Logger log = Logger.getLogger(AbstractInputAction.class);

    /**
     * Checks the mode attribute of the element, and then calls the appropriate
     * method, either doAllAction or doExplicitAction. If no mode is specified,
     * then doAllAction is called.
     * @param ocula Ocula instance that contains the appropriate context.
     * @param element The type of element allowed is specified by subclasses.
     */
    public void act(Ocula ocula, Element element) throws ActionException {

	String mode = element.getAttributeValue("mode", "all");
	if (mode.equals("all")) {
	    doAllAction(ocula, element);
	}

	else if (mode.equals("explicit")) {
	    doExplicitAction(ocula, element);
	}
    }

    /**
     * Executes the action in the "all" mode. This implies having to iterate
     * up the hierarchy until the element &lt;input&gt; is found and then
     * finding all the &lt;text&gt; elements inside that. The method then
     * proceeds to process all of these &lt;text&gt; elements.
     * @param ocula Ocula instance that contains the appropriate context.
     * @param element The type of element allowed is specified by subclasses.
     * @throws ActionException If there is a problem executing the action.
     */
    protected void doAllAction(Ocula ocula, Element element)
	    throws ActionException {
	log.debug("Selected mode: all");
	Element parent = element;
	while (parent.getName().equals("simpleinput") == false &&
		parent.getName().equals("input") == false) {
	    parent = parent.getParentElement();
	}
	List textChildren = parent.getChildren("text");
	for (Iterator it = textChildren.iterator(); it.hasNext();) {
	    Element e = (Element) it.next();
	    String name = e.getAttributeValue("name");
	    doAction(ocula, name);
	}
    }

    /**
     * Executes the action in "explicit" mode. This implies getting the names
     * for the &lt;text&gt; elements that must be processed from the &lt;name&gt;
     * elements.
     * 
     * @param ocula Ocula instance that contains the appropriate context.
     * @param element The type of element allowed is specified by subclasses.
     * @throws ActionException
     */
    protected void doExplicitAction(Ocula ocula, Element element)
	    throws ActionException {
	log.debug("Selected mode: explicit");
	List nameElements = element.getChildren("name");
	for (Iterator it = nameElements.iterator(); it.hasNext();) {
	    Element e = (Element) it.next();
	    String name = e.getValue();
	    doAction(ocula, name);
	}
    }

    /**
     * Actually runs the script for this action.
     * 
     * @param ocula Ocula instance that contains the appropriate context.
     * @param name The key value in the context for the object that the action
     * must call. This object must be of type {@link javax.swing.text.Document}.
     * @throws EvalError if there is a problem executing the script.
     */
    protected abstract void runScript(Ocula ocula, String name)
	    throws EvalError;

    /**
     * Populates the context, runs the script and clears the context.
     * 
     * @param ocula Ocula instance that contains the appropriate context.
     * @param name The key value in the context for the object that the action
     * must call. This object must be of type {@link javax.swing.text.Document}.
     * @throws ActionException if there is a problem running the action.
     */
    protected void doAction(Ocula ocula, String name) throws ActionException {
	if (name != null) {
	    try {
		populateContext(ocula, name);
		runScript(ocula, name);
		clearContext(ocula);
	    }
	    catch (EvalError ee) {
		ActionException ae = new ActionException(
			"Error when evaluating script");
		ae.initCause(ee);
		throw ae;
	    }
	}

    }

    /**
     * Populates the context with key/value pairs needed to successfully
     * execute runScript(). If a subclass overrides this method, it must
     * also override clearContext().
     * @param ocula Ocula instance that contains the appropriate context.
     * @param name The key value in the context for the object that the action
     * must call. This object must be of type {@link javax.swing.text.Document}.
     * @throws EvalError if there is an error calling getLength() on
     * <code>name</code>.
     */
    protected void populateContext(Ocula ocula, String name) throws EvalError {
	int offset = 0;
	ocula.putContext("offset", new Integer(offset));
	Object length = ocula.evaluate(name + ".getLength()");
	ocula.putContext("length", length);
    }

    /**
     * Clears the context of the key/value pairs introduced in populateContext().
     * @param ocula Ocula instance that contains the appropriate context.
     */
    protected void clearContext(Ocula ocula) {
	ocula.removeKey("offset");
	ocula.removeKey("length");
    }
}
