/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.apiconsumer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.object.VMObjectReference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * API consumer activity enables users to execute a method on a class 
 * from a third party java API. 
 * 
 * @author Alex Nenadic
 * @author Tom Oinn
 */
public class ApiConsumerActivity extends
	AbstractAsynchronousDependencyActivity<ApiConsumerActivityConfigurationBean> {
	
	/**
	 * Configuration bean containing API consumer activity-specific settigs. 
	 */
	private ApiConsumerActivityConfigurationBean configurationBean;	
	

	@Override
	public void configure(ApiConsumerActivityConfigurationBean bean)
			throws ActivityConfigurationException {

		configurationBean = bean;
		
		// Configure activity's input and output ports
		configurePorts();
	}
	
	@Override
	public ApiConsumerActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}
	
	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		
		callback.requestRun(new Runnable() {

			public void run() {
				
				// Workflow run identifier (needed when classloader sharing is set to 'workflow').
				String procID = callback.getParentProcessIdentifier();
				String workflowRunID;
				if (procID.contains(":")){
					workflowRunID = procID.substring(0, procID.indexOf(':'));
				}
				else
					workflowRunID = procID; // for tests, will be an empty string
				
				// Configure the classloader for executing the API consumer
				if (classLoader == null){ // This is just for junit test to work and set its own classloader - classLoader will always be null at this point normally
					try{
						classLoader = findClassLoader(configurationBean, workflowRunID);
					}catch(RuntimeException rex){
						String message = "Unable to obtain the classloader for the API consumer service";
						callback.fail(message, rex);		
					}
				}
				
				ReferenceService referenceService = callback.getContext().getReferenceService();
				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				// Not very useful - can't get the workflow out of the context !!!
				// TODO change this to the identifier of a workflow run
				//workflow = callback.getContext().getEntities(Dataflow.class).get(0);
				
				// We shouldn't really have to do this - classloader should have already been 
				// configured from {@link #configure()}
				if (classLoader == null){
					try{
						classLoader = findClassLoader(configurationBean, workflowRunID);
					}catch(RuntimeException rex){
						callback.fail(rex.getMessage(), rex);
						return;
					}
				}

				// The class of the target object where the method is to be invoked on
				Class<?> targetClass = null;
				try {
					targetClass = classLoader.loadClass(configurationBean.getClassName());
				} catch (ClassNotFoundException cnfe) {
					callback.fail("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "No class " + configurationBean.getClassName() + " found!", cnfe);
					return;
				}
				
				// Non-constructor method
				if (!configurationBean.isMethodConstructor()) {
					// The target object where the method is to be invoked on
					Object targetObject = null;
					// If this isn't a static method then get the target object from 
					// the incoming data - it will be on the input port called 'object'
					if (!configurationBean.isMethodStatic()) {
						// The expected object must have been registered as a VMObjectReference
						// Get the reference set for the VMObjectReference
						ReferenceSet vmObjectReferenceSet = (ReferenceSet) referenceService
								.resolveIdentifier(data.get("object"), null,
										callback.getContext()); 
						// The set should contain only one external reference, i.e. VMObjectReference
						Set<ExternalReferenceSPI> externalReferences = vmObjectReferenceSet.getExternalReferences();
						VMObjectReference vmObjectReference = null;
						for (ExternalReferenceSPI externalReference : externalReferences) {
							if (externalReference instanceof VMObjectReference){
								vmObjectReference = (VMObjectReference)externalReference;
								break;
							}
						}
						
						if (vmObjectReference == null) {
							callback.fail("API Consumer "
									+ configurationBean.getClassName() + "."
									+ configurationBean.getMethodName() + " error: "
									+ "Referenced a non static method but no object supplied to act on!");
							return;
						}
						// Get the actual object contained in the VMObjectReference
						targetObject = vmObjectReference.getObject();

						// Check whether the target object is assignable to the
						// specified classname, if not then complain
						Class<?> objectClass = targetObject.getClass();
						if (!targetClass.isAssignableFrom(objectClass)) {
							callback.fail("API Consumer "
									+ configurationBean.getClassName() + "."
									+ configurationBean.getMethodName() + " error: "
									+"Class of " + objectClass.getName() + " not assignable to "
									+ targetClass.getName() + ".");
							return;
						}
						// Class found and is compatible
					}

					// Input arguments' classes
					Class<?>[] inputClasses = null;
					try{
						inputClasses = argumentClasses();
					}
					catch(Exception ex){
						callback.fail(ex.getMessage());
						return;
					}
					// Input argument' objects
					Object[] inputObjects = null;
					try{
						inputObjects = argumentObjects(data, callback);
					}
					catch(Exception ex){
						callback.fail(ex.getMessage(), ex);
						return;
					}
					// Method
					Method method = null;
					try {
						method = targetClass.getMethod(configurationBean.getMethodName(), inputClasses);
					} catch (NoSuchMethodException nsme) {
						callback.fail("API Consumer "
								+ configurationBean.getClassName() + "."
								+ configurationBean.getMethodName() + " error: "
								+ "No method with name "
								+ configurationBean.getMethodName() + " found.", nsme);
						return;
					}
					// Invoke the method
					Object resultObject = null;
					try {
						resultObject = method.invoke(targetObject, inputObjects);
					} catch (Exception ex) {
						callback.fail(ex.getMessage());
						return;
					}
					
					for (OutputPort outputPort : getOutputPorts()) {
						// There are only two possible output ports, called 'result' and 'object'
						if (resultObject != null) {	// void methods return null here
							if (outputPort.getName().equals("result")){

								// T2 reference to the result 
								T2Reference reference = null;
								
								if (canRegisterAsString(configurationBean.getReturnType())) {
									// Char type is a bit special so we deal with it separately
									if(configurationBean.getReturnType().equals("char")) {
										if(configurationBean.getReturnDimension() == 0){
											// Return object is Character that needs to
											// be converted to a char[] with only one element
											char[] array = new char[1];
											array[0] = ((Character)resultObject).charValue();								
											reference = referenceService.register(convertObjectToString(array), 0, true, callback.getContext());
										}
										// Return object is char[]
										else if(configurationBean.getReturnDimension() == 1){
											reference = referenceService.register(
													convertObjectToString(resultObject), 0, true, callback.getContext());
										}
										// Return object is char[][], char[][][], etc.
										else{
											// Convert char[][] to a list of strings, char[][][] to a list of lists of strings, etc.
											ArrayList<Object>list = convertArrayToListOfStrings(resultObject, configurationBean.getReturnDimension()-1); //list contains char[]s
											reference = referenceService.register(
													list,
													outputPort.getDepth()-1, true,
													callback.getContext());
										}
									}
									else{// Any other type that can be converted to string										
										if(configurationBean.getReturnDimension() == 0){
											reference = referenceService.register(
													convertObjectToString(resultObject),
													outputPort.getDepth(), true,
													callback.getContext());
										} 
										// Returns array of (array of ...) elements that can be converted to 
										// list of (list of ...) strings
										else {
											// Convert to list of (list of ...) strings
											ArrayList<Object> list = convertArrayToListOfStrings(resultObject, configurationBean.getReturnDimension()); // list contains strings
											reference = referenceService.register(
													list,
													outputPort.getDepth(), true,
													callback.getContext());
										}
									}
								} 
								else if(configurationBean.getReturnType().equals("byte")) {
									if(configurationBean.getReturnDimension() == 0){
										// Return object is Byte that needs to
										// be converted to a byte[] with only one element
										byte[] array = new byte[1];
										array[0] = ((Byte)resultObject).byteValue();								
										reference = referenceService.register(array, 0, true, callback.getContext());
									}
									// Return object is byte[]
									else if(configurationBean.getReturnDimension() == 1){
										reference = referenceService.register(resultObject, 0, true, callback.getContext());
									}
									// Return object is byte[][], byte[][][], etc.
									else{
										// Convert byte[][] to list of byte[]s, byte[][][] to list of lists of byte[]s, etc.
										ArrayList<Object>list = convertByteArrayToListOfByteArrays(resultObject); //list contains byte[]s
										reference = referenceService.register(
												list,
												outputPort.getDepth()-1, true,
												callback.getContext());
									}	
								}
								else{  //POJO	
									// Register a VMObjectReference that contains the result object 
									// with Reference Manager
									VMObjectReference vmRef = new VMObjectReference();
									vmRef.setObject(resultObject);
									reference = referenceService.register(
											vmRef, outputPort.getDepth(), false,
											callback.getContext());
								}
								
								outputData.put("result", reference);
							}
						}
						
						if (!configurationBean.isMethodStatic()) {
							if (outputPort.getName().equals("object")){
								// Put the original object back into the return values
								// after the method has been called on it
								T2Reference reference = null;
								VMObjectReference vmRef = new VMObjectReference();
								vmRef.setObject(targetObject);
								reference = referenceService.register(vmRef,
										outputPort.getDepth(), false, callback.getContext());

								outputData.put("object", reference);
							}
						}
					}
				} 
				else { // Method is a constructor
					
					// Input argument' classes
					Class<?>[] inputClasses = null;
					try {
						inputClasses = argumentClasses();
					} catch (Exception ex) {
						callback.fail(ex.getMessage());
						return;
					}
					// Input argument' objects
					Object[] inputObjects = null;
					try {
						inputObjects = argumentObjects(data, callback);
					} catch (Exception ex) {
						callback.fail(ex.getMessage());
						return;
					}
					// Constructor method
					Constructor<?> constructor = null;
					try {
						constructor = targetClass.getConstructor(inputClasses);
					} catch (NoSuchMethodException nsme) {
						callback.fail("API Consumer "
								+ configurationBean.getClassName() + "."
								+ configurationBean.getMethodName() + " error: " 
								+"Cannot locate constructor method.", nsme);
						return;
					}
					// Invoke constuctor
					Object resultObject = null;
					try {
						resultObject = constructor.newInstance(inputObjects);
					} catch (Exception ex) {
						callback.fail("API Consumer "
								+ configurationBean.getClassName() + "."
								+ configurationBean.getMethodName() + " error: "
								+"Problem calling constructor method.", ex);
						return;
						
					} catch(LinkageError le){
						callback.fail("API Consumer "
								+ configurationBean.getClassName() + "."
								+ configurationBean.getMethodName() + " error: "
								+"Problem finding dynamic libraries - "
								+"most probably the user did not set the operating system's dynamic library search path", le);
						return;
					}
									
					for (OutputPort outputPort : getOutputPorts()) {
						// For constructor methods, there can only be one output port, called 'object'
						if (outputPort.getName().equals("object")){
							// Put the object returned by the constructor to the output port
							T2Reference reference = null;
							VMObjectReference vmRef = new VMObjectReference();
							vmRef.setObject(resultObject);
							reference = referenceService.register(vmRef,
									outputPort.getDepth(), false, callback
											.getContext());

							outputData.put("object", reference);
						}
					}
				}
				
				// Send result to the callback
				callback.receiveResult(outputData, new int[0]);
			}
		});
	}
		

	/**
	 * Configures input and output ports for the activity.
	 * 
	 * @throws ClassNotFoundException
	 */
	private void configurePorts() throws ActivityConfigurationException{
		
		removeInputs();
		removeOutputs();
		
			// All non-static methods need the object to invoke the method on and it is
			// passed through the input port called 'object'. Non-static methods and constructors
			// also return the same object through the output port called 'object'
			if (!configurationBean.isMethodStatic()) {
				if (!configurationBean.isMethodConstructor()) {
						addInput("object", 0, false, null, null);
				}
				addOutput("object", 0);
			}
					
			// Add output port 'result' for the return value of non-void methods
			if (!configurationBean.getReturnType().equals("void") && !configurationBean.isMethodConstructor()) {
				// byte[] return type maps to port dimension 0! It is treated as a stream rather than a list of bytes.
				// Similar for char and char[] - not implemented yet
				if (configurationBean.getReturnType().equals("byte") && configurationBean.getReturnDimension()==1){
					addOutput("result", 0);
				}
				else
					addOutput("result", configurationBean.getReturnDimension());
			}

			// Add input ports for method's parameters
			for (int i = 0; i < configurationBean.getParameterNames().length; i++) {
				// Create input ports...					
				if (canRegisterAsString(configurationBean.getParameterTypes()[i])) {
					if(configurationBean.getParameterTypes()[i].equals("char")) {
						// char, char[] are treated as a string (where char is a string with only one character),
						// so the port depth for char[] is 0 rather than 1 as is expected for an array
						if(configurationBean.getParameterDimensions()[i] == 0 || 
								configurationBean.getParameterDimensions()[i] == 1){
							addInput(configurationBean.getParameterNames()[i],
									0, true, null,
									String.class);
						}// char[][], etc.
						else {
							addInput(configurationBean.getParameterNames()[i],
									configurationBean.getParameterDimensions()[i]-1, true, null,
								String.class);
						}
					}
					else{
						addInput(configurationBean.getParameterNames()[i],
								configurationBean.getParameterDimensions()[i], true,
								null, String.class);
					}
				}
				else if(configurationBean.getParameterTypes()[i].equals("byte")) {
					// byte, byte[] are treated as a stream of depth 0 rather that array of bytes of depth 1.
					// byte is treated as byte[] with only one element.
					// Note that the port depth is set to 0 rather than 1 as is expected for an array
					if(configurationBean.getParameterDimensions()[i] == 0 || 
							configurationBean.getParameterDimensions()[i] == 1){
						addInput(configurationBean.getParameterNames()[i],
								0, true, null, byte[].class);
					}
					// byte[][], etc.
					else {
						addInput(configurationBean.getParameterNames()[i],
								configurationBean.getParameterDimensions()[i]-1, true, null, byte[].class);
					}
				}
				else{  //POJO	
					addInput(configurationBean.getParameterNames()[i],
						configurationBean.getParameterDimensions()[i], false, null,
						null);
				}
			}

	}	
	
	/**
	 * Returns whether a given type can be registered as a String with the Reference Service.
	 * 
	 * @param parameterType
	 * @return
	 */
	private boolean canRegisterAsString(String parameterType){
		//TODO org.w3c.dom.Document
		if (parameterType.equals("java.math.BigInteger") ||
				parameterType.equals("java.math.BigDecimal") ||
				
				parameterType.equals("java.lang.Number") ||
				parameterType.equals("java.lang.Integer") ||
				parameterType.equals("java.lang.Long") ||
				parameterType.equals("java.lang.Double") ||
				parameterType.equals("java.lang.Short") ||
				parameterType.equals("java.lang.Float") ||
				parameterType.equals("java.lang.Boolean") ||
				
				parameterType.equals("int") || 
				parameterType.equals("long") ||
				parameterType.equals("double") ||
				parameterType.equals("boolean") ||
				parameterType.equals("short") || 
				parameterType.equals("float") || 
				parameterType.equals("java.lang.String") ||
				parameterType.equals("char")) {
			return true;
		} 
		else
			return false;
	}
	
	/**
	 * Converts array of (arrays of ...) elements to list (of list of ...) strings.
	 * The dimension of the returned list is equal the dimension of the input array. 
	 *
	 * @param obj 1 or more-dimensional array of elements that can be converted to strings
	 * @return
	 */
	protected ArrayList<Object> convertArrayToListOfStrings(Object obj, int listDimension) {
		
		ArrayList<Object> objList = new ArrayList<Object>();

		if (listDimension == 1){
			if (obj instanceof int[]){
				for (int i=0; i< ((int[])obj).length; i++){
					// Convert int[] to a list of string representations of integers
					objList.add(convertObjectToString(new Integer(((int[])obj)[i])));
				}
			}
			else if (obj instanceof long[]){
				for (int i=0; i< ((long[])obj).length; i++){
					objList.add(convertObjectToString(new Long(((long[])obj)[i])));
				}
			}
			else if (obj instanceof short[]){
				for (int i=0; i< ((short[])obj).length; i++){
					objList.add(convertObjectToString(new Short(((short[])obj)[i])));
				}
			}
			else if (obj instanceof float[]){
				for (int i=0; i< ((float[])obj).length; i++){
					objList.add(convertObjectToString(new Float(((float[])obj)[i])));
				}
			}
			else if (obj instanceof boolean[]){
				for (int i=0; i< ((boolean[])obj).length; i++){
					objList.add(convertObjectToString(new Boolean(((boolean[])obj)[i])));
				}
			}
			else if (obj instanceof String[]){
				for (int i=0; i< ((String[])obj).length; i++){
					objList.add(((String[])obj)[i]);
				}
			}
			else if (obj instanceof char[][]){
				for (int i=0; i< ((char[][])obj).length; i++){
					objList.add(convertObjectToString(((char[][])obj)[i]));
				}
			}
			else if (obj instanceof BigInteger[]){
				for (int i=0; i< ((BigInteger[])obj).length; i++){
					objList.add(convertObjectToString(((BigInteger[])obj)[i]));
				}
			}
			else if (obj instanceof BigDecimal[]){
				for (int i=0; i< ((BigDecimal[])obj).length; i++){
					objList.add(convertObjectToString(((BigDecimal[])obj)[i]));
				}
			}
			//else -> Error; we should have exhausted all types that can be converted to a string
		}
		else // it is 2 or more-dimensional array - go recursively
		{
			for(int i= 0; i< ((Object[])obj).length; i++){
				ArrayList<Object> elementList = convertArrayToListOfStrings(((Object[])obj)[i], listDimension -1);
				objList.add(elementList);
			}
		}

		return objList;
	}
	
	/**
	 * Converts array of arrays of (arrays of ...) bytes to a list of (lists of ...) byte[]s. 
	 * The dimension of the returned list equals the dimension of the input array -1. 
	 * 
	 * @param obj 2 or more-dimensional array of bytes
	 * @return
	 */
	private ArrayList<Object> convertByteArrayToListOfByteArrays(Object obj) {
		
		ArrayList<Object> byteArrayList = new ArrayList<Object>();
			
		for(int i= 0; i< ((Object[])obj).length; i++){
			if (! (((Object[])obj)[i] instanceof byte[])){ // is it not byte[] - go recursively
					List<Object> elementList = convertByteArrayToListOfByteArrays(((Object[])obj)[i]);
					byteArrayList.add(elementList);
				}
				else{
					byteArrayList.add((((byte[])obj)[i]));
				}
			}
		
		return byteArrayList;
	}
	
	/**
	 * Converts an object into its string representation.
	 * 
	 * @param resultObject
	 * @return
	 */
	private String convertObjectToString(Object resultObject){

		if (resultObject instanceof String) {
			return (String)resultObject;
		} 
		else if (resultObject instanceof BigInteger) {
			return new String(((BigInteger)resultObject).toString());
		} 
		else if (resultObject instanceof BigDecimal) {
			return new String(((BigDecimal)resultObject).toString());
		} 
		else if (resultObject instanceof Double) {
			return new String(Double.toString((Double)resultObject));
		} 
		else if (resultObject instanceof Float) {
			return new String(Float.toString((Float)resultObject));
		}
		else if (resultObject instanceof Integer) {
				return new String(Integer.toString((Integer)resultObject));
		}
		else if (resultObject instanceof Long) {
			return new String(Long.toString((Long)resultObject));
		}
		else if (resultObject instanceof Short) {
			return new String(Short.toString((Short)resultObject));
		}
		else if (resultObject instanceof Boolean) {
			return new String(Boolean.toString((Boolean)resultObject));
		}
		if (resultObject instanceof char[]) {
			return new String((char[])resultObject);
		} 
		else{// Should not happen
			return "Error";
		}
	}
	
	/**
	 * Returns an array of the method to be invoked arguments' classes.
	 * 
	 * @return
	 * @throws Exception
	 */
	private Class<?>[] argumentClasses() throws Exception {
		// Create an array of Class objects corresponding to the arguments
		Class<?>[] inputClasses = new Class[configurationBean.getParameterNames().length];
		for (int i = 0; i < inputClasses.length; i++) {
			String className = configurationBean.getParameterTypes()[i];
			Class<?> baseClass;
			try {
				baseClass = classLoader.loadClass(className);
			} catch (ClassNotFoundException cnfe) {
				// If it is not a class, it must be a primitive then
				baseClass = classForPrimitive(className);
				if (baseClass == null) {
					throw new Exception("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "Can't locate parameter class " + configurationBean.getParameterTypes()[i] +".");
				}
			}
			if (configurationBean.getParameterDimensions()[i] > 0) {
				int[] temp = new int[configurationBean.getParameterDimensions()[i]];
				inputClasses[i] = Array.newInstance(baseClass, temp).getClass();
			} else {
				inputClasses[i] = baseClass;
			}
		}
		return inputClasses;
	}

	private Class<?> classForPrimitive(String className) {
		if ("int".equals(className)) return int.class;
		if ("long".equals(className)) return long.class;
		if ("double".equals(className)) return double.class;
		if ("float".equals(className)) return float.class;
		if ("byte".equals(className)) return byte.class;
		if ("char".equals(className)) return char.class; 
		if ("short".equals(className)) return short.class;
		if ("boolean".equals(className)) return boolean.class;
		return null;
	}
	
	/**
	 * Returns an array of objects representing the arguments of the method to be invoked.
	 * 
	 * @return
	 * @throws Exception
	 */
	private Object[] argumentObjects(Map<String, T2Reference> data,
			AsynchronousActivityCallback callback) throws Exception {
		
		ReferenceService referenceService = callback.getContext().getReferenceService();

		// Argument objects
		Object[] inputObjects = new Object[configurationBean.getParameterTypes().length];
		for (int i = 0; i < inputObjects.length; i++) {
			// Get the argument object from the reference service
			Object argument = null;

			if (canRegisterAsString(configurationBean.getParameterTypes()[i])){
				// Parameter was registered as a String with the Reference Service -
				// try to get it as String or list (of lists of ...) Strings and parse it internally 
				try{
					 argument = referenceService.renderIdentifier(data
					.get(configurationBean.getParameterNames()[i]), String.class, callback
					.getContext());
				}catch(ReferenceServiceException rse){
					throw new Exception("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "Could not fetch the input argument " + configurationBean.getParameterNames()[i]
							+ " of type " + configurationBean.getParameterTypes()[i]
							+ " from the Reference Service.");
				}
			
				if (argument == null) {
				throw new Exception("API Consumer "
						+ configurationBean.getClassName() + "."
						+ configurationBean.getMethodName() + " error: "
						+ "Required input argument " + configurationBean.getParameterNames()[i]
						+ " of type " + configurationBean.getParameterTypes()[i] + " not found.");
				}
				
				if (configurationBean.getParameterTypes()[i] == "char"){ 
					// char
					if(configurationBean.getParameterDimensions()[i] == 0){
						inputObjects[i] = new Character(((String)argument).charAt(0));
					}
					//char[]
					else if(configurationBean.getParameterDimensions()[i] == 1){
						inputObjects[i] = ((String)argument).toCharArray();
					}
					else // char[][] is returned as a list of Strings, char[][][] is returned as a list of lists of Strings etc.
					{
						// Convert the list (of lists of ...) Strings to char[][]...[]
						inputObjects[i] = createInputObject(
								argument, "char[]", configurationBean.getParameterDimensions()[i]-1);
					}
				}
				else{
					inputObjects[i] = createInputObject(
						argument, configurationBean.getParameterTypes()[i], configurationBean.getParameterDimensions()[i]);
				}
			}
			else if (configurationBean.getParameterTypes()[i] == "byte"){ 
				// Parameter was registered as byte, byte[], byte[][], etc.
				try{
					 argument = referenceService.renderIdentifier(data
					.get(configurationBean.getParameterNames()[i]), byte[].class, callback
					.getContext());
					 
				}catch(ReferenceServiceException rse){
					throw new Exception("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "Could not fetch the input argument " + configurationBean.getParameterNames()[i]
							+ " of type " + configurationBean.getParameterTypes()[i]
							+ " from the Reference Service.");
				}
				
				if (argument == null) {
					throw new Exception("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "Required input argument " + configurationBean.getParameterNames()[i]
							+ " of type " + configurationBean.getParameterTypes()[i] + " not found.");
					}
				
				if(configurationBean.getParameterDimensions()[i] == 0){
					inputObjects[i] = new Byte(((byte[])argument)[0]);
				}
				else if(configurationBean.getParameterDimensions()[i] == 1){
					inputObjects[i] = (byte[])argument;
				}
				else // byte[][] is returned as a list of byte[]s, byte[][][] is returned as a list of lists of byte[]s, etc.
				{
					// Convert the list (of lists of ...) byte[] to byte[][]...[]
					inputObjects[i] = createInputObject(
							argument, "byte[]", configurationBean.getParameterDimensions()[i]-1);
				}
			}
			else{
				// Parameter was regestered with Reference Service as object inside an VMObjectReference wrapper 
				try{
					// Get the reference set for the VMObjectReference
					ReferenceSet vmObjectReferenceSet = (ReferenceSet) referenceService
							.resolveIdentifier(data.get(configurationBean.getParameterNames()[i]), null,
									callback.getContext()); 
					// The set should contain only one external reference, i.e. VMObjectReference
					Set<ExternalReferenceSPI> externalReferences = vmObjectReferenceSet.getExternalReferences();
					for (ExternalReferenceSPI externalReference : externalReferences) {
						if (externalReference instanceof VMObjectReference){
							argument = (VMObjectReference)externalReference;
							break;
						}
					}
				}catch(ReferenceServiceException rse){
					throw new Exception("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "Could not fetch the input argument " + configurationBean.getParameterNames()[i]
							+ " of type " + configurationBean.getParameterTypes()[i]
							+ " from the Reference Service.");
				}

				if (argument == null) {
					throw new Exception("API Consumer "
							+ configurationBean.getClassName() + "."
							+ configurationBean.getMethodName() + " error: "
							+ "Required input argument " + configurationBean.getParameterNames()[i]
							+ " of type " + configurationBean.getParameterTypes()[i] + " not found.");
					}
				// Get the actual object from the wrapper
				inputObjects[i] = ((VMObjectReference)argument).getObject();
			}
		}
		return inputObjects;
	}
	
	/**
	 * Creates an object based on the dataObject returned by the Reference Service. dataObject 
	 * is either a single item or a list of (lists of ...) items that are transformed into arrays.
	 * @param dataObject
	 * @param javaType
	 * @return
	 * @throws Exception
	 */
	protected Object createInputObject(Object dataObject, String javaType, int dimension)
			throws Exception {
		if (dataObject instanceof Collection) {
			return createInputArray((Collection<?>) dataObject, javaType, dimension);
		} else {
			return createSingleObjectItem(dataObject, javaType);
		}
	}

	private Object createInputArray(Collection<?> collection, String javaType, int listDimension)
			throws Exception {		
		
		// TODO Unfortunately, this method will not work for arrays of arbitrary dimension - thus 
		// we only support up to 2-dimensional arrays.
				
		// List is dimension 1 and is of primitive type, i.e. int, float, short,...,
		// or is of type byte[] or char[]
		if (listDimension == 1){
			if (javaType.equals("int")) {
				int[]array = new int[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					String str = (String)it.next();
					array[count++]= Integer.parseInt(str);
				}
				return array;
			}
			else if (javaType.equals("long")) {
				long[]array = new long[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					String str = (String)it.next();
					array[count++]= Long.parseLong(str);
				}	
				return array;
			}
			else if (javaType.equals("short")) {
				short[]array = new short[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					String str = (String)it.next();
					array[count++]= Short.parseShort(str);
				}
				return array;
			}
			else if (javaType.equals("float")) {
				float[]array = new float[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					String str = (String)it.next();
					array[count++]= Float.parseFloat(str);
				}
				return array;
			}
			else if (javaType.equals("double")) {
				double[]array = new double[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					String str = (String)it.next();
					array[count++]= Double.parseDouble(str);
				}
				return array;
			}
			else if (javaType.equals("boolean")) {
				boolean[]array = new boolean[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					String str = (String)it.next();
					array[count++]= Boolean.parseBoolean(str);
				}
				return array;
			}
			else if (javaType.equals("byte[]")) {
				byte[][]array = new byte[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					byte[] barray = (byte[])it.next();
					System.arraycopy(barray, 0, array[count++], 0, barray.length);
				}
				return array;
			}
			else if (javaType.equals("char[]")) {
				char[][]array = new char[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					char[] barray = (char[])it.next();
					System.arraycopy(barray, 0, array[count++], 0, barray.length);
				}
				return array;
			}
			// list contains String objects
			else if (javaType.equals("java.lang.String")) {
				String[]array = new String[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (String)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.math.BigInteger")) {
				BigInteger[]array = new BigInteger[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (BigInteger)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.math.BigDecimal")) {
				BigDecimal[]array = new BigDecimal[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (BigDecimal)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.lang.Integer")) {
				Integer[]array = new Integer[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (Integer)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.lang.Long")) {
				Long[]array = new Long[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (Long)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.lang.Short")) {
				Short[]array = new Short[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (Short)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.lang.Float")) {
				Float[]array = new Float[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (Float)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.lang.Double")) {
				Double[]array = new Double[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (Double)it.next();
				}
				return array;
			}
			else if (javaType.equals("java.lang.Boolean")) {
				Boolean[]array = new Boolean[collection.size()];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					array[count++]= (Boolean)it.next();
				}
				return array;
			}
			else{ // Should not get to here
				return null;
			}
		}
		else{ //list is 2 or more-dimensional
			
			if (javaType.equals("int")) {
				int[][] array = new int[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (int[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("long")) {
				long[][] array = new long[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (long[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;			
			}
			else if (javaType.equals("short")) {
				short[][] array = new short[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (short[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;			
			}
			else if (javaType.equals("float")) {
				float[][] array = new float[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (float[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;				
			}
			else if (javaType.equals("double")) {
				double[][] array = new double[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (double[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;				
			}
			else if (javaType.equals("boolean")) {
				boolean[][] array = new boolean[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (boolean[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;				
			}
			else if (javaType.equals("byte[]")) {
				byte[][][] array = new byte[collection.size()][][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (byte[][])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;	
			}
			else if (javaType.equals("char[]")) {
				char[][][] array = new char[collection.size()][][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (char[][])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;				
			}
			else if (javaType.equals("java.lang.String")) {
				String[][] array = new String[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (String[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;			
			}
			else if (javaType.equals("java.math.BigInteger")) {
				BigInteger[][] array = new BigInteger[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (BigInteger[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;			
			}
			else if (javaType.equals("java.math.BigDecimal")) {
				BigDecimal[][] array = new BigDecimal[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (BigDecimal[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("java.lang.Integer")) {
				Integer[][] array = new Integer[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (Integer[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("java.lang.Long")) {
				Long[][] array = new Long[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (Long[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("java.lang.Short")) {
				Short[][] array = new Short[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (Short[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("java.lang.Float")) {
				Float[][] array = new Float[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (Float[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("java.lang.Double")) {
				Double[][] array = new Double[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (Double[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else if (javaType.equals("java.lang.Boolean")) {
				Boolean[][] array = new Boolean[collection.size()][];
				int count=0;
				for (Iterator<?> it = collection.iterator(); it.hasNext();) {
					Object item = it.next();
					array[count++]= (Boolean[])createInputArray((Collection<?>) item, javaType, listDimension -1);
				}
				return array;
			}
			else { // Should not realy happen
				return null;
			}
		}
				
	}

	private Object createSingleObjectItem(Object item, String javaType)
			throws Exception {
				
		if (item instanceof String) {
			if (javaType.equals("java.math.BigInteger")) {
				return new BigInteger((String) item);
			} 
			if (javaType.equals("java.math.BigDecimal")) {
				return new BigDecimal((String) item);
			}
			if (javaType.equals("int") || javaType.equals("java.lang.Integer")) {
				return new Integer((String) item);
			}
			if (javaType.equals("long") || javaType.equals("java.lang.Long")) {
				return new Long((String) item);
			}
			if (javaType.equals("float") || javaType.equals("java.lang.Float")) {
				return new Float((String) item);
			}
			if (javaType.equals("double") || javaType.equals("java.lang.Double")) {
				return new Double((String) item);
			}
			if (javaType.equals("boolean") || javaType.equals("java.lang.Boolean")) {
				return new Boolean((String) item);
			}
			if (javaType.equals("short") || javaType.equals("java.lang.Short")) {
				return new Short((String) item);
			}
			if (javaType.equals("java.lang.String")) {
				return item;
			}
			// byte[] is treated as a single item, i.e. activity stream
			if (javaType.equals("byte[]")) {
				return item;
			}
			// char[] is treated as a single item, i.e. activity string
			if (javaType.equals("char[]")) {
				return item;
			}
		}
		// Otherwise just return the object
		return item;
	}

}
