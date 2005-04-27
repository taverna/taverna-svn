/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

// Utility Imports
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Arrays;

import java.lang.reflect.*;


/**
 * A task to invoke a APIConsumerProcessor
 * @author Tom Oinn
 */
public class APIConsumerTask implements ProcessorTaskWorker {
    
    private APIConsumerProcessor processor;

    public APIConsumerTask(Processor p) {
	this.processor = (APIConsumerProcessor)p;
    }
    
    private Object[] inputObjects(Map input) 
	throws TaskExecutionException {
	APIConsumerDefinition def = processor.definition;
	Object[] inputObjects = new Object[def.pTypes.length];
	for (int i = 0; i < inputObjects.length; i++) {
	    DataThing parameterThing = (DataThing)input.get(def.pNames[i]);
	    if (parameterThing == null) {
		throw new TaskExecutionException("Requires input name "+def.pNames[i]);
	    }
	    try {
		inputObjects[i] = APIConsumerDefinition.createInputObject(parameterThing, def.pTypes[i]);
	    }
	    catch (Exception ex) {
		TaskExecutionException tee = new TaskExecutionException(ex.getMessage());
		tee.initCause(ex);
		throw tee;
	    }
	}
	return inputObjects;	
    }

    private Class[] argumentClasses() 
	throws TaskExecutionException {
	APIConsumerDefinition def = processor.definition;
	// Create an array of Class objects corresponding to the arguments
	Class[] inputClasses = new Class[def.pNames.length];
	for (int i = 0; i < inputClasses.length; i++) {
	    String className= def.pTypes[i];
	    try {
		Class baseClass = Class.forName(className);
		if (def.pDimensions[i] > 0) {
		    int[] temp = new int[def.pDimensions[i]];
		    inputClasses[i] = Array.newInstance(baseClass, temp).getClass();
		}
		else {
		    inputClasses[i] = baseClass;
		}
	    }
	    catch (ClassNotFoundException cnfe) {
		throw new TaskExecutionException("Can't locate parameter class "+def.pTypes[i]);
	    }
	}
	return inputClasses;
    }

    public Map execute(Map input, 
		       ProcessorTask parentTask) 
	throws TaskExecutionException {
	Map result = new HashMap();
	// Different code paths for constructor, static and non static
	APIConsumerDefinition def = processor.definition;
	Class targetClass = null;
	try {
	    targetClass = Class.forName(def.className);
	}
	catch (ClassNotFoundException cnfe) {
	    throw new TaskExecutionException("No class "+def.className+" found!");
	}
	if (def.isConstructor == false) {
	    Object targetObject = null;
	    // If this isn't a static method then get the target object out of the 
	    // incoming DataThing - will be called 'object'
	    
	    if (def.isStatic == false) {
		DataThing objectThing = (DataThing)input.get("object");
		if (objectThing == null) {
		    throw new TaskExecutionException("Referenced a non static method but no object supplied to act on!");
		}
		try {
		    targetObject = APIConsumerDefinition.createInputObject(objectThing, 
									   def.className);
		}
		catch (Exception ex) {
		    TaskExecutionException tee = new TaskExecutionException(ex.getMessage());
		    tee.initCause(ex);
		    throw tee;
		}
		// Check whether the target object is assignable to the specified
		// classname, if not then complain
		Class objectClass = targetObject.getClass();
		// Check assignment here
		if (targetClass.isAssignableFrom(objectClass) == false) {
		    throw new TaskExecutionException("Class of "+objectClass.getName()+
						     " not assignable to "+targetClass.getName());
		}
		// Class found and is compatible
	    }

	    Class[] inputClasses = argumentClasses();

	    // Should now have a Class[] containing the input classes
	    Method method = null;
	    try {
		method = targetClass.getMethod(def.methodName, inputClasses);
	    }
	    catch (NoSuchMethodException nsme) {
		throw new TaskExecutionException("No method with name "+def.methodName+" found in "+targetClass.getName());
	    }

	    Object[] inputObjects = inputObjects(input);

	    // Invoke the method
	    Object resultObject;
	    try {
		resultObject = method.invoke(targetObject, inputObjects);
	    }
	    catch (Exception ex) {
		TaskExecutionException tee = new TaskExecutionException(ex.getMessage());
		tee.initCause(ex);
		throw tee;
	    }
	    if (resultObject != null) {
		// void methods return null here
		result.put("result", new DataThing(resultObject));
	    }
	    // TODO - invoke method here
	    if (def.isStatic == false) {
		// Put the original object into the return after the method has been called on it
		result.put("object", new DataThing(targetObject));
	    }
	}
	else {
	    // Constructor, should move argument parse to somewhere else!
	    Class[] inputClasses = argumentClasses();
	    Constructor constructor;
	    try {
		constructor = targetClass.getConstructor(inputClasses);
	    }
	    catch (NoSuchMethodException nsme) {
		TaskExecutionException tee = new TaskExecutionException("Cannot locate Constructor");
		tee.initCause(nsme);
		throw tee;
	    }
	    Object[] inputObjects = inputObjects(input);
	    try {
		Object resultObject = constructor.newInstance(inputObjects);
		result.put("object", new DataThing(resultObject));
	    }
	    catch (Exception ex) {
		TaskExecutionException tee = new TaskExecutionException("Problem calling constructor");
		tee.initCause(ex);
		throw tee;
	    }
		
	}
	return result;
	
    }
}
