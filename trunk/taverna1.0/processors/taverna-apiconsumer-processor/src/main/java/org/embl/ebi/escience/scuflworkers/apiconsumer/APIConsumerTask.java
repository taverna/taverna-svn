/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;


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

    private static void translateNumericTypes(Object[] objects, Class[] classes) {
	for (int i = 0; i < objects.length; i++) {
	    Class target = classes[i];
	    if (Number.class.isAssignableFrom(target)) {
		if (objects[i] instanceof String) {
		    // If the input is a numeric type and the actual object
		    // is a string we need to parse the string into the appropriate
		    // numeric wrapper type
		    String string = (String)objects[i];
		    Number number = null;
		    if (target.equals(BigDecimal.class)) {
			number = new BigDecimal(string);
		    }
		    else if (target.equals(BigInteger.class)) {
			number = new BigInteger(string);
		    }
		    else if (target.equals(Byte.class) ||
			     target.equals(byte.class)) {
			number = new Byte(string);
		    }
		    else if (target.equals(Double.class) ||
			     target.equals(double.class)) {
			number = new Double(string);
		    }
		    else if (target.equals(Float.class) ||
			     target.equals(float.class)) {
			number = new Float(string);
		    }
		    else if (target.equals(Integer.class) ||
			     target.equals(int.class)) {
			number = new Integer(string);
		    }
		    else if (target.equals(Long.class) ||
			     target.equals(long.class)) {
			number = new Long(string);
		    }
		    else if (target.equals(Short.class) ||
			     target.equals(short.class)) {
			number = new Short(string);
		    }
		    objects[i] = number;
		}
	    }
	}
    }

    public Map execute(Map input, 
		       IProcessorTask parentTask) 
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
	    translateNumericTypes(inputObjects, inputClasses);

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
