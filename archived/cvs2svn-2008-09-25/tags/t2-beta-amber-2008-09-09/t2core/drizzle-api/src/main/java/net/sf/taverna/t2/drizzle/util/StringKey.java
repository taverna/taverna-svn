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

/**
 * @author alanrw
 *
 */
public final class StringKey implements PropertyKey {

	private String key;
	
	public StringKey() {
		// Default constructor
	}
	
	/**
	 * Construct a new StringKey
	 * 
	 * @param key
	 */
	public StringKey(final String key) {
		this.key = key;
	}
	
	/**
	 * Return the String that identifies the StringKey
	 * 
	 * @return
	 */
	public String getKey() {
		return this.key;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof StringKey) {
			return ((StringKey)o).getKey().equals(this.key);
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int compareTo(PropertyKey o) {
		int result = 0;
		if (o instanceof StringKey) {
			StringKey stringArg = (StringKey) o;
			result = getKey().compareTo(stringArg.getKey());
		}
		else {
			throw new ClassCastException ("Argument is not a StringKey"); //$NON-NLS-1$
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getKey();
	}

	/**
	 * @param key the key to set
	 */
	public synchronized final void setKey(String key) {
		this.key = key;
	}
}
