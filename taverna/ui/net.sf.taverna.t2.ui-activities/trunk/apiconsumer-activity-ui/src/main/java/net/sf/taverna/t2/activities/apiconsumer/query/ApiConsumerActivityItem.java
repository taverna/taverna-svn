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
package net.sf.taverna.t2.activities.apiconsumer.query;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivityConfigurationBean;
import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class ApiConsumerActivityItem extends AbstractActivityItem{

	/**
	 * Enclosing API consumer this method belong's to 
	 * (from the XML API consumer definition file).
	 */ 
	private String apiConsumerName;

	/**
	 * Enclosing API consumer's description 
	 * (from the XML API consumer definition file).
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
	private boolean isStatic;

	/**
	 * 	Is the method  to be invoked a contructor?
	 */
	private boolean isConstructor;
	
	
	////////////////// METHODS ///////////////////
		
	public String getType() {
		return "API Consumer";
	}

	@Override
	public String toString() {
		// Extract just the class name (from the fully qulified class name)
		String justClassName = className.substring(className.lastIndexOf(".") + 1);
		
		return justClassName + "." + methodName;
	}
	
	public Object getConfigBean() {
		ApiConsumerActivityConfigurationBean bean = new ApiConsumerActivityConfigurationBean();
		bean.setApiConsumerName(apiConsumerName);
		bean.setApiConsumerDescription(apiConsumerDescription);
		bean.setDescription(description);
		bean.setClassName(className);
		bean.setMethodName(methodName);
		bean.setParameterNames(parameterNames);
		bean.setParameterTypes(parameterTypes);
		bean.setParameterDimensions(parameterDimensions);
		bean.setReturnType(returnType);
		bean.setReturnDimension(returnDimension);
		bean.setIsMethodConstructor(isConstructor);
		bean.setIsMethodStatic(isStatic);
		return bean;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(ApiConsumerActivityItem.class.getResource("/apiconsumer.png"));
	}

	@Override
	public Activity<?> getUnconfiguredActivity() {
		return new ApiConsumerActivity();
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
	 * @param isConstructor the isConstructor to set
	 */
	public void setIsConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	/**
	 * @return the isConstructor
	 */
	public boolean isConstructor() {
		return isConstructor;
	}

	/**
	 * @param isStatic the isStatic to set
	 */
	public void setIsStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	/**
	 * @return the isStatic
	 */
	public boolean isStatic() {
		return isStatic;
	}

}
