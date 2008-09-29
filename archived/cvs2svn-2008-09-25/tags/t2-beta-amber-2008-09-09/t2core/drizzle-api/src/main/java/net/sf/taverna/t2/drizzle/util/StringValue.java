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
/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/util/", name = "stringValue")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/util/", name = "stringValue")
public class StringValue implements PropertyValue, Comparable<Object> {
	private String value;
	
	/**
	 * Construct a new StringValue
	 * 
	 * @param value
	 */
	public StringValue(final String value) {
		this.value = value;
	}
	
	/**
	 * Return the String that identifies the StringValue
	 * 
	 * @return
	 */
	public String getValue() {
		return this.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof StringValue) {
			return ((StringValue)o).getValue().equals(this.value);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof StringValue) {
			StringValue exampleArg = (StringValue) arg0;
			result = getValue().compareTo(exampleArg.getValue());
		}
		else {
			throw new ClassCastException ("Argument is not a StringValue"); //$NON-NLS-1$
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return getValue().hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getValue();
	}
}
