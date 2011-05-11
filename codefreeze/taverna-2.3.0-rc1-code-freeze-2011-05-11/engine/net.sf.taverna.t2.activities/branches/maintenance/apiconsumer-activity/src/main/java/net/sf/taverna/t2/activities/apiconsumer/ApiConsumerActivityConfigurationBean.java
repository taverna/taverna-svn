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

import net.sf.taverna.t2.activities.dependencyactivity.DependencyActivityConfigurationBean;

/**
 * A configuration bean for the API Consumer activity. 
 * 
 * @author Alex Nenadic
 *
 */
public class ApiConsumerActivityConfigurationBean extends DependencyActivityConfigurationBean{
	
	/**
	 * The name of the API consumer that wrapped the method to be invoked.
	 */ 
	private String apiConsumerName;
	
	/**
	 * The description of the API consumer that wrapped the method to be invoked.
	 */ 
	private String apiConsumerDescription;
	
	/**
	 * Method description.
	 */ 
	private String description;
	
	/**
	 * The name of the class the method will be invoked on.
	 */ 
	private String className;

	/**
	 * The name of the method to invoke.
	 */ 
	private String methodName;

	/**
	 * List of parameter names.
	 */ 
	private String[] parameterNames;

	/**
	 * List of parameter dimensions (0 for single object, 1 for a list of objects, etc.)
	 */ 
	private int[] parameterDimensions;

	/**
	 * List of parameter types.
	 */
	private String[] parameterTypes;

	/**
	 * Method's return parameter type.
	 */ 
	private String returnType;

	/**
	 * Method's return parameter dimension.
	 */
	private int returnDimension;

	/**
	 * Is the method to be invoked static?
	 */
	private boolean isMethodStatic;

	/**
	 * 	Is the method  to be invoked a contructor?
	 */
	private boolean isMethodConstructor;

	/////////////////////// METHODS /////////////////////////
	
	/**
	 * @param apiConsumerDescription the apiConsumerDescription to set
	 */
	public void setApiConsumerDescription(String apiConsumerDescription) {
		this.apiConsumerDescription = apiConsumerDescription;
	}

	/**
	 * @return the apiConsumerDescription
	 */
	public String getApiConsumerDescription() {
		return apiConsumerDescription;
	}
	
	/**
	 * @param apiConsumerName the apiConsumerName to set
	 */
	public void setApiConsumerName(String apiConsumerName) {
		this.apiConsumerName = apiConsumerName;
	}

	/**
	 * @return the apiConsumerName
	 */
	public String getApiConsumerName() {
		return apiConsumerName;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param className the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param parameterNames the parameterNames to set
	 */
	public void setParameterNames(String[] pNames) {
		this.parameterNames = pNames;
	}

	/**
	 * @return the parameterNames
	 */
	public String[] getParameterNames() {
		return parameterNames;
	}

	/**
	 * @param parameterDimensions the parameterDimensions to set
	 */
	public void setParameterDimensions(int[] pDimensions) {
		this.parameterDimensions = pDimensions;
	}

	/**
	 * @return the parameterDimensions
	 */
	public int[] getParameterDimensions() {
		return parameterDimensions;
	}

	/**
	 * @param parameterTypes the parameterTypes to set
	 */
	public void setParameterTypes(String[] pTypes) {
		this.parameterTypes = pTypes;
	}

	/**
	 * @return the parameterTypes
	 */
	public String[] getParameterTypes() {
		return parameterTypes;
	}


	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @param returnDimension the returnDimension to set
	 */
	public void setReturnDimension(int returnDimension) {
		this.returnDimension = returnDimension;
	}

	/**
	 * @return the returnDimension
	 */
	public int getReturnDimension() {
		return returnDimension;
	}
	
	/**
	 * @param isMethodConstructor the isMethodConstructor to set
	 */
	public void setIsMethodConstructor(boolean isConstructor) {
		this.isMethodConstructor = isConstructor;
	}

	/**
	 * @return the isMethodConstructor
	 */
	public boolean isMethodConstructor() {
		return isMethodConstructor;
	}

	/**
	 * @param isMethodStatic the isMethodStatic to set
	 */
	public void setIsMethodStatic(boolean isStatic) {
		this.isMethodStatic = isStatic;
	}

	/**
	 * @return the isMethodStatic
	 */
	public boolean isMethodStatic() {
		return isMethodStatic;
	}
	
}
