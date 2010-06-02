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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A workflow result returned when an error has occurred. This is the value of a
 * {@link DataValue} if DataValue.containsError() returns true and the DataValue
 * does not contain a list.
 * 
 * @author David Withers
 */
@XmlRootElement(name = "ErrorValue")
public class ErrorValue {

	private String exceptionMessage = "";
	private String message = "";
	private List<ErrorTrace> stackTrace;

	/**
	 * Returns the exceptionMessage.
	 * 
	 * @return the value of exceptionMessage
	 */
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	/**
	 * Sets the exceptionMessage.
	 * 
	 * @param exceptionMessage
	 *            the new value for exceptionMessage
	 */
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	/**
	 * Returns the message.
	 * 
	 * @return the value of message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 * 
	 * @param message
	 *            the new value for message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the stackTrace.
	 * 
	 * @return the value of stackTrace
	 */
	public List<ErrorTrace> getStackTrace() {
		return stackTrace;
	}

	/**
	 * Sets the stackTrace.
	 * 
	 * @param stackTrace
	 *            the new value for stackTrace
	 */
	public void setStackTrace(List<ErrorTrace> stackTrace) {
		this.stackTrace = stackTrace;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ERROR");
		if (getMessage() != null) {
			sb.append(" : ");
			sb.append(getMessage());
		}
		sb.append(Resource.lineSeparator);
		if (getExceptionMessage() != null) {
			sb.append(getExceptionMessage());
			sb.append(Resource.lineSeparator);
		}
		List<ErrorTrace> stackTrace = getStackTrace();
		if (stackTrace != null) {
			for (ErrorTrace errorTrace : stackTrace) {
				sb.append("  ");
				sb.append(errorTrace);
				sb.append(Resource.lineSeparator);			
			}
		}
		return sb.toString();
	}
}
