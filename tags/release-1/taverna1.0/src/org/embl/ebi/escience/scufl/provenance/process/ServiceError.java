/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

import org.jdom.*;
import java.util.*;

/**
 * Event corresponding to a single error in a service
 * invocation. This may result in retry, alternate
 * scheduling or service failure if neither retries
 * nor alternates are available.
 * @author Tom Oinn
 */
public class ServiceError extends ProcessEvent {
 
    private Exception underlyingException;
    
    public String getMessage() {
	if (underlyingException!=null) {
	    return underlyingException.getMessage();
	}
	else {
	    return "Unknown exception";
	}
    }
   
    /**
     * Construct a service error event from the given
     * exception.
     */
    public ServiceError(Exception ex) {
	super();
	this.underlyingException = ex;
    }

    /**
     * Show the exception stack trace in the report
     */
    public Element eventElement() {
	Element e = super.eventTopLevelElement();
	// Get the stack trace as a string
	if (underlyingException != null) {
	    boolean doneFullTrace = false;
	    StringBuffer sb = new StringBuffer();
	    Exception ex = underlyingException;
	    while (!doneFullTrace) {
		sb.append(ex.getMessage()+"<br>");
		StackTraceElement stack[] = ex.getStackTrace();
		// stack[0] contains the method that created the exception.
		// stack[stack.length-1] contains the oldest method call.
		// Enumerate each stack element.
		for (int i=0; i<stack.length; i++) {
		    String fileName = stack[i].getFileName();
		    if (fileName == null) {
			fileName = "Unknown filename";
			// The source filename is not available
		    }
		    String[] classNameParts = stack[i].getClassName().split("\\.");
		    String className = classNameParts[classNameParts.length - 1];
		    String methodName = stack[i].getMethodName();
		    boolean isNativeMethod = stack[i].isNativeMethod();
		    int line = stack[i].getLineNumber();
		    sb.append("   "+className+"."+methodName+"(..) : line "+line+" &lt;"+fileName+"&gt;<br>");
		}
		if (ex.getCause()!=null) {
		    ex = (Exception)ex.getCause();
		    sb.append("<br>");
		}
		else {
		    doneFullTrace = true;
		}
	    }
	    e.setText(sb.toString());
	}
	return e;
    }

}
