/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

import java.lang.reflect.Method;
import java.text.DateFormat;

// Utility Imports
import java.util.Date;

// JDOM Imports
import org.jdom.Element;

import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;



/**
 * Represents a single state change in the processor state
 * transition diagram. Subclasses extend this to show information
 * for each interesting transition.
 * @author Tom Oinn
 */
public abstract class ProcessEvent {
    
    // Create a timestamp when a subclass is created
    private Date eventDate = new Date();
    
    // Provide a public get method for the date string
    public String getTimeStamp() {
	return DateFormat.getDateTimeInstance().format(eventDate);
    }
    
    /**
     * Subclass this method to add information to the Element
     * returned, the eventTopLevelElement will build the
     * enclosing element for you.
     */
    public Element eventElement() {
	return eventTopLevelElement();
    }
    
    // Return an XML Element representing this event
    public Element eventTopLevelElement() {
	Class eventClass = this.getClass();
	Method[] methods = eventClass.getMethods();
	String leafClassName = (eventClass.getName().split("\\."))[eventClass.getName().split("\\.").length-1];
	Element theElement = new Element(leafClassName);
	for (int i = 0; i < methods.length; i++) {
	    String methodName = methods[i].getName();
	    try {
		if (methodName.startsWith("get")) {
		    // Check whether the method is declared in a subclass
		    // of this one. If not then we inherited it from somewhere
		    // and its contents shouldn't be serialized
		    Class declaringClass = methods[i].getDeclaringClass();
		    if (ProcessEvent.class.isAssignableFrom(declaringClass)) {
			// Check the parameters, should be an array with no
			// items in.
			if (methods[i].getParameterTypes().length==0) {
			    // Check the return type, must be a string
			    // or we're not bothered
			    if (methods[i].getReturnType().equals(String.class)) {
				// Eat the 'get' part of the method name
				String attributeName = methodName.substring(3);
				// Invoke the method.
				String attributeValue = (String)methods[i].invoke(this,
										  new Object[0]);
				theElement.setAttribute(attributeName, attributeValue);
			    }
			    else {
				// Didn't return a string so we can't do much
				// with it.
			    }
			}
			else {
			    // get method defined in the right place but
			    // requiring arguments, so not one we're
			    // interested in.
			}
		    }
		    else {
			// get method, but an inherited one so not interesting
		    }
		}
		else {
		    // not a get method
		}
	    }
	    catch (Exception e) {
		// Caught an exception while trying to do the reflection
	    }
	}
	return theElement;
    }
    
}
