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
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 *
 */
public class ExampleValue implements PropertyValue, Comparable<Object> {
	private static int valueCount = 0;
	
	private int value;
	
	/**
	 * Construct a new ExampleValue
	 */
	public ExampleValue() {
		this.value = valueCount++;
	}
	
	/**
	 * Return the integer identifying the ExampleValue
	 * 
	 * @return
	 */
	public int getValue() {
		return this.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof ExampleValue) {
			return ((ExampleValue)o).getValue() == this.value;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof ExampleValue) {
			ExampleValue exampleArg = (ExampleValue) arg0;
			result = getValue() - exampleArg.getValue();
		}
		else {
			throw new ClassCastException ("Argument is not an ExampleValue"); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Integer.toString(getValue());
	}
}
