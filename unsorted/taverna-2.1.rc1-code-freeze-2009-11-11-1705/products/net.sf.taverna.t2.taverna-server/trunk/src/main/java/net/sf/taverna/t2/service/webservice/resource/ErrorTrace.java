/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.service.webservice.resource;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ErrorTrace")
public class ErrorTrace {
	private String className, methodName, fileName;
	private int lineNumber;
	
	public ErrorTrace() {			
	}
	
	public ErrorTrace(String declaringClass, String methodName,
			String fileName, int lineNumber) {
		this.className = declaringClass;
		this.methodName = methodName;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
	
	/**
	 * Returns the className.
	 *
	 * @return the value of className
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * Sets the className.
	 *
	 * @param className the new value for className
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Returns the methodName.
	 *
	 * @return the value of methodName
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Sets the methodName.
	 *
	 * @param methodName the new value for methodName
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	/**
	 * Returns the fileName.
	 *
	 * @return the value of fileName
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Sets the fileName.
	 *
	 * @param fileName the new value for fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Returns the lineNumber.
	 *
	 * @return the value of lineNumber
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Sets the lineNumber.
	 *
	 * @param lineNumber the new value for lineNumber
	 */
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}	
	
    @Override
	public String toString() {
        return getClassName() + "." + methodName +
            (lineNumber == -2 ? "(Native Method)" :
             (fileName != null && lineNumber >= 0 ?
              "(" + fileName + ":" + lineNumber + ")" :
              (fileName != null ?  "("+fileName+")" : "(Unknown Source)")));
    }

}