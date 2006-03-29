/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;
import org.embl.ebi.escience.baclava.*;
import java.lang.reflect.*;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

public class SeqhoundTask implements ProcessorTaskWorker {
    
    private SeqhoundProcessor processor;

    public SeqhoundTask(Processor p) {
	this.processor = (SeqhoundProcessor)p;
    }

    public Map execute(Map inputMap, ProcessorTask parentTask)
	throws TaskExecutionException {
	try {
	    this.processor.initSeqhound();
	}
	catch (java.io.IOException ioe) {
	    throw new TaskExecutionException("Unable to contact SeqHound server : "+ioe);
	}
	// Create an array of arguments to the method call
	Object[] inputArray = new Object[inputMap.size()];
	InputPort[] inputs = processor.getInputPorts();
	for (int i = 0; i < inputs.length; i++) {
	    String inputName = inputs[i].getName();
	    DataThing inputThing = (DataThing)inputMap.get(inputName);
	    Class targetClass = (Class)processor.inputTypes.get(inputName);
	    // Now need to convert the value of the DataThing into an instance
	    // of the target class
	    Object targetObject = null;
	    if (targetClass.isArray() == false) {
		try {
		    if (targetClass.equals(String.class)) {
			targetObject = (String)inputThing.getDataObject();
		    }
		    else if (targetClass.equals(Integer.class) ||
			     targetClass.equals(Integer.TYPE)) {
			targetObject = new Integer((String)inputThing.getDataObject());
		    }
		    else if (targetClass.equals(Float.class) ||
			     targetClass.equals(Float.TYPE)) {
			targetObject = new Float((String)inputThing.getDataObject());
		    }
		    else if (targetClass.equals(Double.class) ||
			     targetClass.equals(Double.TYPE)) {
			targetObject = new Double((String)inputThing.getDataObject());
		    }
		    else if (targetClass.equals(Long.class) ||
			     targetClass.equals(Long.TYPE)) {
			targetObject = new Long((String)inputThing.getDataObject());
		    }
		    else if (targetClass.equals(Boolean.class) ||
			     targetClass.equals(Boolean.TYPE)) {
			targetObject = new Boolean((String)inputThing.getDataObject());
		    }
		    else {
			throw new TaskExecutionException("Unable to generate input of type "+targetClass.toString()+" for input name "+inputName);
		    }
		}
		catch (NumberFormatException nfe) {
		    // Couldn't parse the input as a numeral
		    throw new TaskExecutionException("Attempt to create a numeric input from a non numeral, "+inputThing.getDataObject()+" cannot be parsed as a "+targetClass);
		}
	    }
	    else {
		// DataThing input must be a collection, the iteration framework will
		// have ensured this. So, we need an appropriate array type to populate
		// from this collection.
		List inputList = (List)inputThing.getDataObject();
		// Cope with input arrays
		if (targetClass.equals(String[].class)) {
		    // Just use the collection methods to build an input array
		    targetObject = (String[])inputList.toArray(new String[0]);
		}
		else if (targetClass.equals(int[].class)) {
		    int[] temp = new int[inputList.size()];
		    for (int j = 0; j < temp.length; j++) {
			temp[j] = Integer.parseInt((String)inputList.get(j));
		    }
		    targetObject = temp;
		}
		else if (targetClass.equals(float[].class)) {
		    float[] temp = new float[inputList.size()];
		    for (int j = 0; j < temp.length; j++) {
			temp[j] = Float.parseFloat((String)inputList.get(j));
		    }
		    targetObject = temp;
		}
		else if (targetClass.equals(double[].class)) {
		    double[] temp = new double[inputList.size()];
		    for (int j = 0; j < temp.length; j++) {
			temp[j] = Double.parseDouble((String)inputList.get(j));
		    }
		    targetObject = temp;
		}
		else if (targetClass.equals(long[].class)) {
		    long[] temp = new long[inputList.size()];
		    for (int j = 0; j < temp.length; j++) {
			temp[j] = Long.parseLong((String)inputList.get(j));
		    }
		    targetObject = temp;
		}
		else {
		    throw new TaskExecutionException("Unable to generate array input of type "+
						     targetClass.toString()+" for input name "+
						     inputName);
		}
	    }
	    inputArray[i] = targetObject;
	}
	
	try {
	    Object result = processor.targetMethod.invoke(processor.seqhound, inputArray);
	    if (result == null) {
		throw new TaskExecutionException("SeqHound operation returned null, not allowed!");
	    }
	    // Do we have a simple type or an array being returned?
	    Class resultClass = result.getClass();
	    Map outputMap = new HashMap();
	    if (result instanceof Hashtable == false) {
		DataThing resultThing;
		if (resultClass.isArray()) {
		    if (resultClass.equals(int[].class)) {
			int[] intArray = (int[])result;
			List targetList = new ArrayList();
			for (int i = 0; i < intArray.length; i++) {
			    targetList.add(""+intArray[i]);
			}
			resultThing = new DataThing(targetList);
		    }
		    else {
			// Unknown array type
			Object[] resultArray = (Object[])result;
			List targetList = new ArrayList();
			for (int i = 0; i < resultArray.length; i++) {
			    targetList.add(""+resultArray[i].toString());
			}
			resultThing = new DataThing(targetList);
		    }
		}
		else {
		    resultThing = new DataThing(result.toString());
		}
		outputMap.put("result",resultThing);
	    }
	    else {
		// Is a hashtable of key -> value result pairs, emit in
		// a pair of lists
		Map resultMap = (Map)result;
		List keyList = new ArrayList(resultMap.keySet());
		List valueList = new ArrayList();
		for (Iterator i = keyList.iterator(); i.hasNext(); ) {
		    valueList.add(resultMap.get(i.next()));
		}
		outputMap.put("keys", new DataThing(keyList));
		outputMap.put("values", new DataThing(valueList));
	    }
	    return outputMap;
	}
	catch (Exception ex) {
	    TaskExecutionException tee = new TaskExecutionException("Could not invoke method!");
	    tee.initCause(ex);
	    throw tee;
	}

    }

}
