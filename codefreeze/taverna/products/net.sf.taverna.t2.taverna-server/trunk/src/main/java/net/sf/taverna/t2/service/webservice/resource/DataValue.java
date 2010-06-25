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

@XmlRootElement(name = "DataValue")
public class DataValue {

	private Object value;

	private ErrorValue errorValue;

	private List<DataValue> list;

	private boolean containsError;

	public DataValue() {
	}

	public DataValue(Object value) {
		this.value = value;
	}

	public DataValue(ErrorValue errorValue) {
		this.errorValue = errorValue;
	}

	public DataValue(List<DataValue> list) {
		this.list = list;
	}

	/**
	 * Returns the value if depth() returns 0.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns the errorValue.
	 *
	 * @return the value of errorValue
	 */
	public ErrorValue getErrorValue() {
		return errorValue;
	}

	/**
	 * Sets the errorValue.
	 *
	 * @param errorValue the new value for errorValue
	 */
	public void setErrorValue(ErrorValue errorValue) {
		this.errorValue = errorValue;
	}

	/**
	 * Returns a list of DataValues if depth() is > 0.
	 *
	 * @return a list of DataValues
	 */
	public List<DataValue> getList() {
		return list;
	}

	public void setList(List<DataValue> list) {
		this.list = list;
	}

	/**
	 * Returns true if getValue returns an ErrorValue or getList() returns a
	 * list that contains an error.
	 * 
	 * @return true if getValue returns an ErrorValue or getList() returns a
	 *         list that contains an error
	 */
	public boolean getContainsError() {
		return containsError;
	}

	/**
	 * Sets whether this DataValue contains an error.
	 * 
	 * @param containsError
	 *            whether this DataValue contains an error
	 */
	public void setContainsError(boolean containsError) {
		this.containsError = containsError;
	}

	public int depth() {
		if (list == null) {
			return 0;
		} else if (list.isEmpty()) {
			return 1;
		} else {
			return list.get(0).depth() + 1;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (depth() > 0) {
			sb.append("List [");
			sb.append(Resource.lineSeparator);
			for (DataValue dataValue : getList()) {
				sb.append(dataValue);
				sb.append(Resource.lineSeparator);
			}
			sb.append("]");
		} else if (getContainsError()){
			sb.append(getErrorValue());
		} else {
			sb.append(getValue());
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DataValue other = (DataValue) obj;
		if (list == null) {
			if (other.list != null) {
				return false;
			}
		} else if (!list.equals(other.list)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
